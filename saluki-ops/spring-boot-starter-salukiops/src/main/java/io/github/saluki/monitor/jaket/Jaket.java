package io.github.saluki.monitor.jaket;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.Gson;
import io.github.saluki.monitor.jaket.model.MethodDefinition;
import io.github.saluki.monitor.jaket.model.ServiceDefinition;
import io.github.saluki.monitor.jaket.model.TypeDefinition;
import io.github.saluki.monitor.jaket.util.ClassUtils;

/**
 * Created by huangsheng.hs on 2015/1/27.
 */
public final class Jaket {

    /**
     * Describe a Java interface in {@link io.github.saluki.monitor.jaket.model.ServiceDefinition}.
     *
     * @return Service description
     */
    public static ServiceDefinition build(final Class<?> interfaceClass) {
        ServiceDefinition sd = new ServiceDefinition();
        sd.setCanonicalName(interfaceClass.getCanonicalName());
        sd.setCodeSource(ClassUtils.getCodeSource(interfaceClass));

        JaketTypeBuilder builder = new JaketTypeBuilder();
        List<Method> methods = ClassUtils.getPublicNonStaticMethods(interfaceClass);
        for (Method method : methods) {
            MethodDefinition md = new MethodDefinition();
            md.setName(method.getName());

            // Process parameter types.
            Class<?>[] paramTypes = method.getParameterTypes();
            Type[] genericParamTypes = method.getGenericParameterTypes();

            String[] parameterTypes = new String[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                TypeDefinition td = builder.build(genericParamTypes[i], paramTypes[i]);
                parameterTypes[i] = td.getType();
            }
            md.setParameterTypes(parameterTypes);

            // Process return type.
            TypeDefinition td = builder.build(method.getGenericReturnType(), method.getReturnType());
            md.setReturnType(td.getType());

            sd.getMethods().add(md);
        }

        sd.setTypes(builder.getTypeDefinitions());
        return sd;
    }

    /**
     * Describe a Java interface in Json schema.
     *
     * @return Service description
     */
    public static String schema(final Class<?> clazz) {
        ServiceDefinition sd = build(clazz);
        Gson gson = new Gson();
        return gson.toJson(sd);
    }

    private Jaket(){
    }
}
