package io.github.mac_genius.pakkit.util.serializer;

import io.github.mac_genius.pakkit.annotation.Ip;
import io.github.mac_genius.pakkit.annotation.Serialize;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class StringSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object, Field field) {
        Ip ip = field.getAnnotation(Ip.class);
        if (ip != null) {
            String[] ipAddress = ((String) object).split(".");
            ByteBuffer byteBuffer = ByteBuffer.allocate(4);
            byteBuffer.put(Byte.parseByte(ipAddress[0]));
            byteBuffer.put(Byte.parseByte(ipAddress[1]));
            byteBuffer.put(Byte.parseByte(ipAddress[2]));
            byteBuffer.put(Byte.parseByte(ipAddress[3]));
            return byteBuffer.array();
        } else {
            String string = (String) object;
            byte[] bytes = string.getBytes();
            ByteBuffer byteBuffer = ByteBuffer.allocate(4 + bytes.length);
            byteBuffer.order(ByteOrder.BIG_ENDIAN);
            byteBuffer.putInt(bytes.length);
            byteBuffer.put(bytes);
            return byteBuffer.array();
        }
    }

    @Override
    public DeserializedObject deserialize(byte[] buffer, Field field) {
        Ip ip = field.getAnnotation(Ip.class);
        if (ip != null) {
            ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
            int length = byteBuffer.getInt();
            return new DeserializedObject(new String(Arrays.copyOfRange(buffer, 4, 4 + length)), length + 4);
        } else {
            String ipAddress = buffer[0] + "." + buffer[1] + "." + buffer[2] + "." + buffer[3];
            return new DeserializedObject(ipAddress, 4);
        }
    }

    @Override
    public int getSize(Object object, Field field) {
        Ip ip = field.getAnnotation(Ip.class);
        if (ip != null) {
            return 4;
        } else {
            return ((String) object).getBytes().length + 4;
        }
    }

    @Override
    public boolean acceptsType(Class clazz) {
        return String.class.equals(clazz);
    }
}
