/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package io.github.saluki.grpc.util;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.protobuf.Message;
import io.github.saluki.serializer.IProtobufSerializer;
import io.github.saluki.serializer.ProtobufSerializer;
import io.github.saluki.serializer.exception.ProtobufException;

/**
 * @author shimingliu 2016年12月15日 上午12:40:53
 * @version ThrallCommonUtil.java, v 0.0.1 2016年12月15日 上午12:40:53 shimingliu
 */
public final class SerializerUtil {

    private static final Gson                gson;

    private static final IProtobufSerializer serializer;

    static {
        serializer = new ProtobufSerializer();
        gson = new Gson();
    }

    private SerializerUtil(){
    }

    public static Message pojo2Protobuf(Object arg) throws ProtobufException {
        if (!(arg instanceof Message)) {
            Message message = (Message) serializer.toProtobuf(arg);
            arg = null;
            return message;
        }
        return (Message) arg;
    }

  public static Object protobuf2Pojo(Message arg, Class<? extends Object> returnType) throws ProtobufException {
        if (!Message.class.isAssignableFrom(returnType)) {
            return serializer.fromProtobuf(arg, returnType);
        } else {
            return arg;
        }
    }

    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    public static <T> T fromJson(String json, Type typeOfT) {
        return gson.fromJson(json, typeOfT);
    }

}
