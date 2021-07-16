/*
 * Copyright 1999-2012 DianRong.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.github.saluki.grpc.client.internal.unary;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.protobuf.Message;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import io.github.saluki.common.Constants;
import io.github.saluki.common.GrpcURL;
import io.github.saluki.common.NamedThreadFactory;
import io.github.saluki.common.RpcContext;
import io.github.saluki.grpc.client.GrpcRequest;
import io.github.saluki.grpc.client.GrpcResponse;
import io.github.saluki.grpc.client.internal.GrpcCallOptions;
import io.github.saluki.grpc.exception.RpcFrameworkException;
import io.github.saluki.grpc.service.ClientServerMonitor;
import io.github.saluki.grpc.service.MonitorService;
import io.github.saluki.grpc.util.GrpcUtil;
import io.github.saluki.grpc.util.SerializerUtil;
import io.github.saluki.serializer.exception.ProtobufException;

import io.grpc.MethodDescriptor;

/**
 * @author liushiming 2017年4月26日 下午6:16:32
 * @version $Id: GrpcHystrixObservableCommand.java, v 0.0.1 2017年4月26日 下午6:16:32 liushiming
 */
@SuppressWarnings("rawtypes")
public abstract class GrpcHystrixCommand extends HystrixCommand<Object> {

  private static final Logger logger = LoggerFactory.getLogger(GrpcHystrixCommand.class);

  private static final ConcurrentMap<String, AtomicInteger> concurrents = Maps.newConcurrentMap();

  private final String serviceName;

  private final String methodName;

  private final long start;

  private final Triple<Map<String, String>, Map<String, Object>, Set<Class>> rpcContext;

  private GrpcRequest request;

  private GrpcUnaryClientCall clientCall;

  private ClientServerMonitor clientServerMonitor;

  private static final ExecutorService collectLogExecutor =
      Executors.newSingleThreadExecutor(new NamedThreadFactory("salukiCollectTask", true));


  public GrpcHystrixCommand(String serviceName, String methodName, Boolean isEnabledFallBack) {
    super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(serviceName))//
        .andCommandKey(HystrixCommandKey.Factory.asKey(serviceName + ":" + methodName))//
        .andCommandPropertiesDefaults(
            HystrixCommandProperties.Setter().withCircuitBreakerRequestVolumeThreshold(20)// 10秒钟内至少19此请求失败，熔断器才发挥起作用
                .withCircuitBreakerSleepWindowInMilliseconds(30000)// 熔断器中断请求30秒后会进入半打开状态,放部分流量过去重试
                .withCircuitBreakerErrorThresholdPercentage(50)// 错误率达到50开启熔断保护
                .withExecutionTimeoutEnabled(false)// 禁用这里的超时
                .withFallbackEnabled(isEnabledFallBack))//
        .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter().withCoreSize(100)
            .withAllowMaximumSizeToDivergeFromCoreSize(true).withMaximumSize(Integer.MAX_VALUE)));
    this.serviceName = serviceName;
    this.methodName = methodName;
    this.start = System.currentTimeMillis();
    this.rpcContext = new ImmutableTriple<Map<String, String>, Map<String, Object>, Set<Class>>(
        RpcContext.getContext().getAttachments(), RpcContext.getContext().get(),
        RpcContext.getContext().getHoldenGroups());
    RpcContext.removeContext();
  }

  public void setRequest(GrpcRequest request) {
    this.request = request;
  }

  public void setClientCall(GrpcUnaryClientCall clientCall) {
    this.clientCall = clientCall;
  }

  public void setClientServerMonitor(ClientServerMonitor clientServerMonitor) {
    this.clientServerMonitor = clientServerMonitor;
  }

  @Override
  public Object execute() {
    try {
      currentConcurrent(this.serviceName, this.methodName).incrementAndGet();
      return super.execute();
    } finally {
      currentConcurrent(this.serviceName, this.methodName).decrementAndGet();
    }
  }

  @Override
  protected Object run() throws Exception {
    try {
      RpcContext.getContext().setAttachments(rpcContext.getLeft());
      RpcContext.getContext().set(rpcContext.getMiddle());
      RpcContext.getContext().setHoldenGroups(rpcContext.getRight());
      MethodDescriptor<Message, Message> methodDesc = this.request.getMethodDescriptor();
      Integer timeOut = this.request.getCallTimeout();
      Message request = getRequestMessage();
      Message response = this.run0(request, methodDesc, timeOut, clientCall);
      Object obj = this.transformMessage(response);
      collectLogExecutor.execute(new Runnable() {

        @Override
        public void run() {
          collect(serviceName, methodName, request, response, false);
        }
      });
      return obj;
    } finally {
      RpcContext.removeContext();
    }
  }

  @Override
  protected Object getFallback() {
    Class<?> responseType = this.request.getResponseType();
    Message response = GrpcUtil.createDefaultInstance(responseType);
    Object obj = this.transformMessage(response);
    collectLogExecutor.execute(new Runnable() {

      @Override
      public void run() {
        collect(serviceName, methodName, getRequestMessage(), response, true);
      }
    });
    return obj;
  }

  protected AtomicInteger currentConcurrent(String serviceName, String methodName) {
    String key = serviceName + ":" + methodName;
    AtomicInteger concurrent = concurrents.get(key);
    if (concurrent == null) {
      concurrents.putIfAbsent(key, new AtomicInteger());
      concurrent = concurrents.get(key);
    }
    return concurrent;
  }

  private Message getRequestMessage() {
    try {
      Object param = this.request.getRequestParam();
      return SerializerUtil.pojo2Protobuf(param);
    } catch (ProtobufException e) {
      RpcFrameworkException rpcFramwork = new RpcFrameworkException(e);
      throw rpcFramwork;
    }
  }

  private Object transformMessage(Message message) {
    Class<?> respPojoType = request.getResponseType();
    GrpcResponse response = new GrpcResponse.Default(message, respPojoType);
    try {
      return response.getResponseArg();
    } catch (ProtobufException e) {
      RpcFrameworkException rpcFramwork = new RpcFrameworkException(e);
      throw rpcFramwork;
    }
  }

  private void collect(String serviceName, String methodName, Message request, Message response,
      boolean error) {
    try {
      InetSocketAddress provider = (InetSocketAddress) GrpcCallOptions
          .getAffinity(this.request.getRefUrl()).get(GrpcCallOptions.GRPC_CURRENT_ADDR_KEY);
      if (request == null || response == null || provider == null) {
        return;
      }
      long elapsed = System.currentTimeMillis() - this.start; // 计算调用耗时
      int concurrent = this.currentConcurrent(serviceName, methodName).get(); // 当前并发数
      String service = serviceName; // 获取服务名称
      String method = methodName; // 获取方法名
      GrpcURL refUrl = this.request.getRefUrl();
      String host = refUrl.getHost();
      Integer port = refUrl.getPort();
      clientServerMonitor.collect(new GrpcURL(Constants.MONITOR_PROTOCOL, host, port, //
          service + "/" + method, //
          MonitorService.TIMESTAMP, String.valueOf(start), //
          MonitorService.APPLICATION, refUrl.getParameter(Constants.APPLICATION_NAME), //
          MonitorService.INTERFACE, service, //
          MonitorService.METHOD, method, //
          MonitorService.PROVIDER, provider.getHostName(), //
          error ? MonitorService.FAILURE : MonitorService.SUCCESS, "1", //
          MonitorService.ELAPSED, String.valueOf(elapsed), //
          MonitorService.CONCURRENT, String.valueOf(concurrent), //
          MonitorService.INPUT, String.valueOf(request.getSerializedSize()), //
          MonitorService.OUTPUT, String.valueOf(response.getSerializedSize())));
    } catch (Throwable t) {
      logger.warn("Failed to monitor count service " + serviceName + ", cause: " + t.getMessage());
    }
  }

  protected abstract Message run0(Message req, MethodDescriptor<Message, Message> methodDesc,
      Integer timeOut, GrpcUnaryClientCall clientCall);

  protected void cacheCurrentServer() {
    Object obj = GrpcCallOptions.getAffinity(this.request.getRefUrl())
        .get(GrpcCallOptions.GRPC_CURRENT_ADDR_KEY);
    if (obj != null) {
      InetSocketAddress currentServer = (InetSocketAddress) obj;
      RpcContext.getContext().setAttachment(Constants.REMOTE_ADDRESS, currentServer.getHostName());
    }
  }
}
