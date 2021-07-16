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
package io.github.saluki.grpc.stream;

import com.google.protobuf.Message;
import io.github.saluki.grpc.util.SerializerUtil;
import io.github.saluki.serializer.exception.ProtobufException;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.netty.util.internal.ThrowableUtil;

/**
 * @author liushiming
 * @version ClientCallStreamObserverWrap.java, v 0.0.1 2017年8月15日 下午12:58:08 liushiming
 * @since JDK 1.8
 */
public class Proto2PoJoStreamObserver implements StreamObserver<Message> {


  private final StreamObserver<Object> streamObserver;

  private final Class<?> poJoType;

  private Proto2PoJoStreamObserver(StreamObserver<Object> streamObserver, Class<?> poJoType) {
    this.streamObserver = streamObserver;
    this.poJoType = poJoType;
  }

  public static Proto2PoJoStreamObserver newObserverWrap(StreamObserver<Object> streamObserver,
      Class<?> returnType) {
    return new Proto2PoJoStreamObserver(streamObserver, returnType);
  }

  @Override
  public void onNext(Message value) {
    try {
      Object respPoJo = SerializerUtil.protobuf2Pojo(value, poJoType);
      streamObserver.onNext(respPoJo);
    } catch (ProtobufException e) {
      String stackTrace = ThrowableUtil.stackTraceToString(e);
      StatusRuntimeException statusException =
          Status.UNAVAILABLE.withDescription(stackTrace).asRuntimeException();
      streamObserver.onError(statusException);
    }
  }


  @Override
  public void onError(Throwable t) {
    streamObserver.onError(t);;
  }


  @Override
  public void onCompleted() {
    streamObserver.onCompleted();
  }

}
