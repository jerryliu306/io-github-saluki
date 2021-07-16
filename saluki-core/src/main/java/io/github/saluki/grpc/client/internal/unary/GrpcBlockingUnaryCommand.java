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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Message;
import io.github.saluki.grpc.exception.RpcErrorMsgConstant;
import io.github.saluki.grpc.exception.RpcServiceException;

import io.grpc.MethodDescriptor;

/**
 * @author liushiming 2017年4月26日 下午5:56:29
 * @version $Id: GrpcBlockingUnaryCommand.java, v 0.0.1 2017年4月26日 下午5:56:29 liushiming
 */
public class GrpcBlockingUnaryCommand extends GrpcHystrixCommand {

  private static final Logger logger = LoggerFactory.getLogger(GrpcHystrixCommand.class);

  public GrpcBlockingUnaryCommand(String serviceName, String methodName,
      Boolean isEnabledFallBack) {
    super(serviceName, methodName, isEnabledFallBack);
  }

  /**
   * @see io.github.saluki.grpc.client.internal.unary.GrpcHystrixCommand#run0(com.google.protobuf.Message,
   *      io.grpc.MethodDescriptor, java.lang.Integer,
   *      io.github.saluki.grpc.client.internal.unary.GrpcUnaryClientCall)
   */
  @Override
  protected Message run0(Message req, MethodDescriptor<Message, Message> methodDesc,
      Integer timeOut, GrpcUnaryClientCall clientCall) {
    try {
      return clientCall.blockingUnaryResult(req, methodDesc);
    } catch (Throwable e) {
      logger.error(e.getMessage(), e);
      super.cacheCurrentServer();
      RpcServiceException rpcService =
          new RpcServiceException(e, RpcErrorMsgConstant.BIZ_DEFAULT_EXCEPTION);
      throw rpcService;
    }
  }

}
