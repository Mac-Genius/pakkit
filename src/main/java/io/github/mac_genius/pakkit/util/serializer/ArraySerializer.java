package io.github.mac_genius.pakkit.util.serializer;

import io.github.mac_genius.pakkit.annotation.Serialize;
import io.github.mac_genius.pakkit.util.PacketUtils;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class ArraySerializer implements Serializer {
    @Override
    public byte[] serialize(Object object, Field field) {
        Serialize serialize = field.getAnnotation(Serialize.class);
        Object[] array = (Object[])object;
        int size = getSize(object, field);
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        byteBuffer.putInt(array.length);
        Serializer serializer = PacketUtils.getInstance().getSerializer(object.getClass().getComponentType());
        if (serializer != null) {
            for (Object o : array) {
                byteBuffer.put(serializer.serialize(o, field));
            }
        }
        return byteBuffer.array();
    }

    @Override
    public DeserializedObject deserialize(byte[] buffer, Field field) {
        Serialize serialize = field.getAnnotation(Serialize.class);
        Class clazz = field.getType().getComponentType();
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        int arraySize = byteBuffer.getInt();
        int offset = 4;
        Object[] array = new Object[arraySize];
        Serializer serializer = PacketUtils.getInstance().getSerializer(clazz);
        if (serializer != null) {
            for (int i = 0; i < arraySize; i++) {
                DeserializedObject object = serializer.deserialize(Arrays.copyOfRange(buffer, offset, buffer.length), field);
                array[i] = object.getObject();
                offset += object.getRead();
            }
        }
        return new DeserializedObject(array, offset);
    }

    @Override
    public int getSize(Object object, Field field) {
        Object[] array = (Object[])object;
        int size = 0;
        size += 4;
        Serializer serializer = PacketUtils.getInstance().getSerializer(object.getClass().getComponentType());
        if (serializer != null) {
            for (Object o : array) {
                size += serializer.getSize(o.getClass(), field);
            }
        }
        return size;
    }

    @Override
    public boolean acceptsType(Class clazz) {
        return clazz.isArray();
    }
}
