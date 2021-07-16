/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the confidential and
 * proprietary information of Quancheng-ec.com ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Quancheng-ec.com.
 */
package io.github.saluki.grpc.client;

import io.github.saluki.common.GrpcURL;

import io.grpc.Channel;

/**
 * @author shimingliu 2016年12月14日 下午5:50:42
 * @version GrpcProtocolClient.java, v 0.0.1 2016年12月14日 下午5:50:42 shimingliu
 */
public interface GrpcProtocolClient<T> {

  public T getGrpcClient(ChannelCall channelCall, int callType, int callTimeout);

  public interface ChannelCall {

    public Channel getChannel(final GrpcURL refUrl);

  }

}
