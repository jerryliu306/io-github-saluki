/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the confidential and
 * proprietary information of Quancheng-ec.com ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Quancheng-ec.com.
 */
package io.github.saluki.grpc.client.internal;

import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;

import io.github.saluki.common.Constants;
import io.github.saluki.common.GrpcURL;
import io.github.saluki.grpc.client.GrpcProtocolClient;
import io.github.saluki.utils.ReflectUtils;

import io.grpc.Channel;

/**
 * @author shimingliu 2016年12月14日 下午9:50:40
 * @version GrpcStubClient.java, v 0.0.1 2016年12月14日 下午9:50:40 shimingliu
 */
public class GrpcStubClient<AbstractStub> implements GrpcProtocolClient<AbstractStub> {

  private final Class<? extends AbstractStub> stubClass;

  private final GrpcURL refUrl;

  public GrpcStubClient(Class<? extends AbstractStub> stubClass, GrpcURL refUrl) {
    this.stubClass = stubClass;
    this.refUrl = refUrl;
  }

  public String getStubClassName() {
    return this.stubClass.getName();
  }

  @SuppressWarnings("unchecked")
  @Override
  public AbstractStub getGrpcClient(ChannelCall channelPool, int callType, int callTimeout) {
    String stubClassName = GrpcStubClient.this.getStubClassName();
    Channel channel = null;
    if (StringUtils.contains(stubClassName, "$")) {
      try {
        String parentName = StringUtils.substringBefore(stubClassName, "$");
        Class<?> clzz = ReflectUtils.name2class(parentName);
        Method method;
        switch (callType) {
          case Constants.RPCTYPE_ASYNC:
            method = clzz.getMethod("newFutureStub", io.grpc.Channel.class);
            break;
          case Constants.RPCTYPE_BLOCKING:
            method = clzz.getMethod("newBlockingStub", io.grpc.Channel.class);
            break;
          default:
            method = clzz.getMethod("newFutureStub", io.grpc.Channel.class);
            break;
        }
        channel = channelPool.getChannel(refUrl);
        AbstractStub stubInstance = (AbstractStub) method.invoke(null, channel);
        return stubInstance;
      } catch (Exception e) {
        throw new IllegalArgumentException(
            "stub definition not correct，do not edit proto generat file", e);
      }
    } else {
      throw new IllegalArgumentException(
          "stub definition not correct，do not edit proto generat file");
    }
  }
}
