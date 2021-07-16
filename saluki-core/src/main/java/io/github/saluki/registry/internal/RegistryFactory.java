/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package io.github.saluki.registry.internal;

import io.github.saluki.common.GrpcURL;
import io.github.saluki.registry.Registry;

import io.grpc.Internal;

/**
 * @author shimingliu 2016年12月14日 下午1:49:12
 * @version RegistryFactory.java, v 0.0.1 2016年12月14日 下午1:49:12 shimingliu
 */
@Internal
public abstract class RegistryFactory {

    public abstract Registry newRegistry(GrpcURL url);
}
