package io.github.mac_genius.pakkit.util.serializer;

import io.github.mac_genius.pakkit.annotation.Serialize;
import io.github.mac_genius.pakkit.util.PacketUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collection;

public class CollectionSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object, Serialize serialize) {
        Collection collection = (Collection) object;
        int size = getSize(object, serialize);
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        byteBuffer.putInt(collection.size());
        Serializer serializer = PacketUtils.getInstance().getSerializer(object.getClass().getComponentType());
        if (serializer != null) {
            for (Object o : collection) {
                byteBuffer.put(serializer.serialize(o, serialize));
            }
        }
        return byteBuffer.array();
    }

    @Override
    public DeserializedObject deserialize(byte[] buffer, Serialize serialize, Class clazz) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        int arraySize = byteBuffer.getInt();
        int offset = 4;
        try {
            Collection array = null;
            for (Constructor c : clazz.getDeclaredConstructors()) {
                if (c.isAccessible() && c.getParameterCount() == 0) {
                    array = (Collection) c.newInstance();
                    break;
                }
            }
            if (array != null) {
                Serializer serializer = PacketUtils.getInstance().getSerializer(clazz.getComponentType());
                if (serializer != null) {
                    for (int i = 0; i < arraySize; i++) {
                        DeserializedObject object = serializer.deserialize(Arrays.copyOfRange(buffer, offset, buffer.length), serialize, clazz.getComponentType());
                        array.add(object.getObject());
                        offset += object.getRead();
                    }
                }
            }
            return new DeserializedObject(array, offset);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }

    @Override
    public int getSize(Object object, Serialize serialize) {
        Collection collection = (Collection) object;
        int size = 0;
        size += 4;
        Serializer serializer = PacketUtils.getInstance().getSerializer(object.getClass().getComponentType());
        if (serializer != null) {
            for (Object o : collection) {
                size += serializer.getSize(o.getClass(), serialize);
            }
        }
        return size;
    }

    @Override
    public boolean acceptsType(Class clazz) {
        return Collection.class.isAssignableFrom(clazz);
    }
}
