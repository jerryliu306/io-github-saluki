/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the confidential and
 * proprietary information of Quancheng-ec.com ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Quancheng-ec.com.
 */
package io.github.saluki.grpc.interceptor;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.saluki.common.RpcContext;
import io.github.saluki.grpc.util.GrpcUtil;
import io.github.saluki.grpc.util.SerializerUtil;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall;
import io.grpc.ForwardingClientCallListener.SimpleForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

/**
 * @author shimingliu 2016年12月14日 下午10:29:17
 * @version HeaderClientInterceptor.java, v 0.0.1 2016年12月14日 下午10:29:17 shimingliu
 */
public class HeaderClientInterceptor implements ClientInterceptor {

  private static final Logger log = LoggerFactory.getLogger(HeaderClientInterceptor.class);


  public static ClientInterceptor instance() {
    return new HeaderClientInterceptor();
  }

  private HeaderClientInterceptor() {

  }

  @Override
  public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
      CallOptions callOptions, Channel next) {
    return new SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {

      @Override
      public void start(Listener<RespT> responseListener, Metadata headers) {
        copyThreadLocalToMetadata(headers);
        super.start(new SimpleForwardingClientCallListener<RespT>(responseListener) {

          @Override
          public void onHeaders(Metadata headers) {
            super.onHeaders(headers);
          }
        }, headers);
      }
    };
  }

  private void copyThreadLocalToMetadata(Metadata headers) {
    Map<String, String> attachments = RpcContext.getContext().getAttachments();
    Map<String, Object> values = RpcContext.getContext().get();
    try {
      if (!attachments.isEmpty()) {
        headers.put(GrpcUtil.GRPC_CONTEXT_ATTACHMENTS, SerializerUtil.toJson(attachments));
      }
      if (!values.isEmpty()) {
        headers.put(GrpcUtil.GRPC_CONTEXT_VALUES, SerializerUtil.toJson(values));
      }
    } catch (Throwable e) {
      log.error(e.getMessage(), e);
    } finally {
      RpcContext.removeContext();
    }
  }
}
