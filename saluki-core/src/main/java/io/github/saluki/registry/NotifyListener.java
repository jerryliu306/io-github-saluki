/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the confidential and
 * proprietary information of Quancheng-ec.com ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Quancheng-ec.com.
 */
package io.github.saluki.registry;

import java.util.List;

import io.github.saluki.common.GrpcURL;

/**
 * @author shimingliu 2016年12月14日 下午1:47:55
 * @version NotifyListener.java, v 0.0.1 2016年12月14日 下午1:47:55 shimingliu
 */
public interface NotifyListener {

  public interface NotifyServiceListener {

    void notify(GrpcURL subscribeUrl, List<GrpcURL> urls);
  }

  public interface NotifyRouterListener {

    void notify(GrpcURL subscribeUrl, String routerCondition);
  }

}
