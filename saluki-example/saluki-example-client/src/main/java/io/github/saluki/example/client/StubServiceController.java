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
package io.github.saluki.example.client;

import java.util.concurrent.ExecutionException;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.github.examples.model.Hello;
import io.github.examples.model.hello.HelloReply;
import io.github.examples.service.HelloServiceGrpc;
import io.github.saluki.boot.SalukiReference;
import io.github.saluki.grpc.util.SerializerUtil;
import io.github.saluki.serializer.exception.ProtobufException;

/**
 * @author liushiming
 * @version StubServiceController.java, v 0.0.1 2017年6月20日 上午11:32:42 liushiming
 * @since JDK 1.8
 */
@RestController
@RequestMapping("/stub")
public class StubServiceController {


  @SalukiReference(service = "io.github.examples.service.HelloService")
  private HelloServiceGrpc.HelloServiceFutureStub helloServiceStub;


  @RequestMapping("/hello")
  public HelloReply hello(@RequestParam("name") String name) throws InterruptedException, ExecutionException, ProtobufException {
    final Hello.HelloRequest helloRequest = Hello.HelloRequest.newBuilder().setName(name).build();
    final io.github.examples.model.Hello.HelloReply reply =
        helloServiceStub.sayHello(helloRequest).get();
    return (HelloReply) SerializerUtil.protobuf2Pojo(reply,
        io.github.examples.model.hello.HelloReply.class);
  }

}
