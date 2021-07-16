/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the confidential and proprietary
 * information of Quancheng-ec.com ("Confidential Information"). You shall not disclose such Confidential Information
 * and shall use it only in accordance with the terms of the license agreement you entered into with Quancheng-ec.com.
 */
package io.github.saluki.boot.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;

/**
 * @author shimingliu 2016年12月17日 上午12:14:31
 * @version WebAppAutoConfiguration.java, v 0.0.1 2016年12月17日 上午12:14:31 shimingliu
 */

@Configuration
@ConditionalOnBean(value = GrpcProperties.class)
public class WebAppAutoConfiguration implements ApplicationListener<WebServerInitializedEvent> {

    private final GrpcProperties grpcProperties;

    public WebAppAutoConfiguration(GrpcProperties thrallProperties) {
        this.grpcProperties = thrallProperties;
    }

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        int httpPort = event.getWebServer().getPort();
        if (grpcProperties.getRegistryHttpPort() == 0) {
            if (httpPort != 0) {
                grpcProperties.setRegistryHttpPort(httpPort);
            }
        }
    }

}
