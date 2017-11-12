package io.github.mac_genius.pakkit.util.serializer;

import io.github.mac_genius.pakkit.annotation.Serialize;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class StringSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object, Serialize serialize) {
        String string = (String) object;
        byte[] bytes = string.getBytes();
        ByteBuffer byteBuffer = ByteBuffer.allocate(4 + bytes.length);
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        byteBuffer.putInt(bytes.length);
        byteBuffer.put(bytes);
        return byteBuffer.array();
    }

    @Override
    public DeserializedObject deserialize(byte[] buffer, Serialize serialize, Class clazz) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        int length = byteBuffer.getInt();
        return new DeserializedObject(new String(Arrays.copyOfRange(buffer, 4, 4 + length)), length + 4);
    }

    @Override
    public int getSize(Object object, Serialize serialize) {
        return ((String)object).getBytes().length + 4;
    }

    @Override
    public boolean acceptsType(Class clazz) {
        return String.class.equals(clazz);
    }
}
