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

import java.util.Arrays;
import java.util.HashSet;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.saluki.example.model.First;
import com.saluki.example.model.Second;

import io.github.examples.model.hello.HelloReply;
import io.github.examples.model.hello.HelloRequest;
import io.github.saluki.boot.SalukiReference;
import io.github.saluki.common.RpcContext;
import io.github.saluki.grpc.service.GenericService;
import io.grpc.stub.StreamObserver;

/**
 * @author liushiming
 * @version GenricServiceController.java, v 0.0.1 2017年7月15日 上午11:09:11 liushiming
 * @since JDK 1.8
 */
@RestController
@RequestMapping("/genric")
public class GenricServiceController {

  @SalukiReference
  private GenericService genricService;

  @RequestMapping("/hello")
  public HelloReply view(@RequestParam(value = "name", required = false) String name) {
    String serviceName = "io.github.examples.service.HelloService";
    String method = "sayHello";
    HelloRequest request = new HelloRequest();
    request.setName(name);
    Object[] args = new Object[] {request};
    HelloReply reply =
        (HelloReply) genricService.$invoke(serviceName, "example", "1.0.0", method, args);
    return reply;
  }

  @RequestMapping("/hellostream")
  public HelloReply hellostream(@RequestParam(value = "name", required = false) String name) {
    String serviceName = "io.github.examples.service.HelloService";
    String method = "sayHelloServerStream";
    HelloRequest request = new HelloRequest();
    request.setName(name);
    Object[] args = new Object[] {request, responseObserver()};
    HelloReply reply =
        (HelloReply) genricService.$invoke(serviceName, "example", "1.0.0", method, args);
    return reply;
  }

  private StreamObserver<io.github.examples.model.hello.HelloReply> responseObserver() {
    StreamObserver<io.github.examples.model.hello.HelloReply> responseObserver =
        new StreamObserver<io.github.examples.model.hello.HelloReply>() {
          @Override
          public void onNext(io.github.examples.model.hello.HelloReply summary) {
            System.out.println(summary.getMessage());
          }

          @Override
          public void onError(Throwable t) {
            t.printStackTrace();
          }

          @Override
          public void onCompleted() {

        }
        };
    return responseObserver;
  }

  @RequestMapping("/hello_1")
  public HelloReply view1(@RequestParam(value = "name", required = false) String name) {
    String serviceName = "io.github.examples.service.HelloService";
    String method = "sayHello";
    HelloRequest request = new HelloRequest();
    request.setName(name);
    Object[] args = new Object[] {request};
    RpcContext.getContext().setHoldenGroups(new HashSet<>(Arrays.asList(First.class)));
    HelloReply reply =
        (HelloReply) genricService.$invoke(serviceName, "example", "1.0.0", method, args);
    return reply;
  }

  @RequestMapping("/hello_2")
  public HelloReply view2(@RequestParam(value = "name", required = false) String name) {
    String serviceName = "io.github.examples.service.HelloService";
    String method = "sayHello";
    HelloRequest request = new HelloRequest();
    request.setName(name);
    Object[] args = new Object[] {request};
    RpcContext.getContext().setHoldenGroups(new HashSet<>(Arrays.asList(Second.class)));
    HelloReply reply =
        (HelloReply) genricService.$invoke(serviceName, "example", "1.0.0", method, args);
    return reply;
  }
}
