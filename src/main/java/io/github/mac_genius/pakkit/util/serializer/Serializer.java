package io.github.mac_genius.pakkit.util.serializer;


import java.lang.reflect.Field;

public interface Serializer {
    byte[] serialize(Object object, Field field);

    DeserializedObject deserialize(byte[] buffer, Field field);

    int getSize(Object object, Field field);

    boolean acceptsType(Class clazz);
}
