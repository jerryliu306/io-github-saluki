/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the confidential and
 * proprietary information of Quancheng-ec.com ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Quancheng-ec.com.
 */
package io.github.saluki.grpc.client.internal;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import io.github.saluki.common.Constants;
import io.github.saluki.common.GrpcURL;
import io.github.saluki.grpc.client.GrpcProtocolClient;
import io.github.saluki.grpc.client.GrpcRequest;
import io.github.saluki.grpc.service.GenericService;
import io.github.saluki.utils.ClassHelper;

import io.grpc.MethodDescriptor;

/**
 * @author shimingliu 2016年12月14日 下午9:50:27
 * @version GenericProxyClient.java, v 0.0.1 2016年12月14日 下午9:50:27 shimingliu
 */
public class GenericProxyClient<T> implements GrpcProtocolClient<T> {


  private final GrpcURL refUrl;

  public GenericProxyClient(GrpcURL refUrl) {
    this.refUrl = refUrl;
  }


  @SuppressWarnings("unchecked")
  @Override
  public T getGrpcClient(GrpcProtocolClient.ChannelCall channelPool, int callType,
      int callTimeout) {
    return (T) Proxy.newProxyInstance(ClassHelper.getClassLoader(),
        new Class[] {GenericService.class},
        new GenericProxyClientInvocation(channelPool, callType, callTimeout));
  }

  private class GenericProxyClientInvocation extends AbstractClientInvocation {

    private final GrpcProtocolClient.ChannelCall channelPool;
    private final int callType;
    private final int callTimeout;

    public GenericProxyClientInvocation(GrpcProtocolClient.ChannelCall channelPool, int callType,
        int callTimeout) {
      super(GenericProxyClient.this.refUrl);
      this.channelPool = channelPool;
      this.callType = callType;
      this.callTimeout = callTimeout;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected GrpcRequest buildGrpcRequest(Method method, Object[] args) {
      if (!isDynamicCall(args)) {
        GrpcURL resetRefUrl = GenericProxyClient.this.refUrl;
        resetRefUrl = resetRefUrl.setPath(getServiceName(args));
        resetRefUrl = resetRefUrl.addParameter(Constants.GROUP_KEY, getGroup(args));
        resetRefUrl = resetRefUrl.addParameter(Constants.VERSION_KEY, getVersion(args));
        GrpcRequest request = new GrpcRequest.Default(resetRefUrl, channelPool,
            this.getMethod(args), this.getArg(args), callType, callTimeout);
        return request;
      } else {
        GrpcURL resetRefUrl = GenericProxyClient.this.refUrl;
        resetRefUrl = resetRefUrl.setPath(getServiceName(args));
        resetRefUrl = resetRefUrl.addParameter(Constants.METHOD_KEY, getMethod(args));
        resetRefUrl = resetRefUrl.addParameter(Constants.GROUP_KEY, getGroup(args));
        resetRefUrl = resetRefUrl.addParameter(Constants.VERSION_KEY, getVersion(args));
        MethodDescriptor<Message, Message> methodDesc =
            (MethodDescriptor<Message, Message>) args[4];
        DynamicMessage message = (DynamicMessage) args[5];
        GrpcRequest request = new GrpcRequest.Dynamic(resetRefUrl, channelPool, methodDesc, message,
            callType, callTimeout);
        return request;
      }
    }

    private Boolean isDynamicCall(Object[] args) {
      for (Object obj : args) {
        if (obj instanceof MethodDescriptor) {
          return true;
        }
      }
      return false;
    }

    private String getServiceName(Object[] args) {
      return (String) args[0];
    }

    private String getGroup(Object[] args) {
      return (String) args[1];
    }

    private String getVersion(Object[] args) {
      return (String) args[2];
    }

    private String getMethod(Object[] args) {
      return (String) args[3];
    }

    private Object[] getArg(Object[] args) {
      Object[] param = (Object[]) args[4];
      return param;
    }

  }
}
