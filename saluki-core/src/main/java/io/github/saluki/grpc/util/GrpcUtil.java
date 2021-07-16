/*
 * Copyright 2014-2017 the original author or authors.
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
package io.github.saluki.grpc.util;

import java.lang.reflect.Method;

import com.google.common.base.Charsets;
import com.google.protobuf.Message;
import io.github.saluki.grpc.annotation.GrpcMethodType;
import io.github.saluki.utils.ReflectUtils;
import io.github.saluki.serializer.ProtobufEntity;

import io.grpc.Metadata;

/**
 * @author liushiming
 * @version GrpcUtil.java, v 0.0.1 2017年7月19日 下午7:04:32 liushiming
 * @since JDK 1.8
 */
@SuppressWarnings("unchecked")
public class GrpcUtil {

  private GrpcUtil() {}

  public static final Metadata.Key<String> GRPC_CONTEXT_ATTACHMENTS =
      Metadata.Key.of("grpc_header_attachments-bin", utf8Marshaller());

  public static final Metadata.Key<String> GRPC_CONTEXT_VALUES =
      Metadata.Key.of("grpc_header_values-bin", utf8Marshaller());

  private static Metadata.BinaryMarshaller<String> utf8Marshaller() {
    return new Metadata.BinaryMarshaller<String>() {

      @Override
      public byte[] toBytes(String value) {
        return value.getBytes(Charsets.UTF_8);
      }

      @Override
      public String parseBytes(byte[] serialized) {
        return new String(serialized, Charsets.UTF_8);
      }
    };
  }

  public static io.grpc.MethodDescriptor<Message, Message> createMethodDescriptor(Class<?> clzz,
      Method method) {
    String clzzName = clzz.getName();
    String methodName = method.getName();
    GrpcMethodType grpcMethodType = method.getAnnotation(GrpcMethodType.class);
    Message argsReq = createDefaultInstance(grpcMethodType.requestType());
    Message argsRep = createDefaultInstance(grpcMethodType.responseType());
    return io.grpc.MethodDescriptor.<Message, Message>newBuilder()
        .setType(grpcMethodType.methodType())//
        .setFullMethodName(io.grpc.MethodDescriptor.generateFullMethodName(clzzName, methodName))//
        .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(argsReq))//
        .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(argsRep))//
        .setSafe(false)//
        .setIdempotent(false)//
        .build();
  }

  public static io.grpc.MethodDescriptor<Message, Message> createMethodDescriptor(String clzzName,
      String methodName, GrpcMethodType grpcMethodType) {
    Message argsReq = createDefaultInstance(grpcMethodType.requestType());
    Message argsRep = createDefaultInstance(grpcMethodType.responseType());
    return io.grpc.MethodDescriptor.<Message, Message>newBuilder()
        .setType(grpcMethodType.methodType())//
        .setFullMethodName(io.grpc.MethodDescriptor.generateFullMethodName(clzzName, methodName))//
        .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(argsReq))//
        .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(argsRep))//
        .setSafe(false)//
        .setIdempotent(false)//
        .build();

  }


  public static Message createDefaultInstance(Class<?> type) {
    Class<? extends Message> messageType;
    if (!Message.class.isAssignableFrom(type)) {
      ProtobufEntity entity =
          (ProtobufEntity) ReflectUtils.findAnnotationFromClass(type, ProtobufEntity.class);
      messageType = entity.value();
    } else {
      messageType = (Class<? extends Message>) type;
    }
    Object obj = ReflectUtils.classInstance(messageType);
    return (Message) obj;
  }
}
