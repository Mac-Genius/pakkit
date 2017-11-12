package io.github.mac_genius.pakkit.util.serializer;


import io.github.mac_genius.pakkit.annotation.Serialize;

public interface Serializer {
    byte[] serialize(Object object, Serialize serialize);

    DeserializedObject deserialize(byte[] buffer, Serialize serialize, Class clazz);

    int getSize(Object object, Serialize serialize);

    boolean acceptsType(Class clazz);
}
