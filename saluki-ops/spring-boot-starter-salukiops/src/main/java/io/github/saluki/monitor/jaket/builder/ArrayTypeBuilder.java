package io.github.saluki.monitor.jaket.builder;

import java.lang.reflect.Type;
import java.util.Map;

import io.github.saluki.monitor.jaket.JaketTypeBuilder;
import io.github.saluki.monitor.jaket.model.TypeDefinition;

/**
 * Created by huangsheng.hs on 2015/1/27.
 */
public class ArrayTypeBuilder implements TypeBuilder {

    @Override
    public boolean accept(Type type, Class<?> clazz) {
        if (clazz == null) {
            return false;
        }

        if (clazz.isArray()) {
            return true;
        }

        return false;
    }

    @Override
    public TypeDefinition build(Type type, Class<?> clazz, Map<Class<?>, TypeDefinition> typeCache) {
        // Process the component type of an array.
        Class<?> componentType = clazz.getComponentType();
        JaketTypeBuilder.build(componentType, componentType, typeCache);

        final String canonicalName = clazz.getCanonicalName();
        TypeDefinition td = new TypeDefinition(canonicalName);
        return td;
    }

}
