package io.github.mac_genius.pakkit.util.serializer;

import io.github.mac_genius.pakkit.annotation.Serialize;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class PrimitiveSerializer implements Serializer {
    private static List<Class> types;

    static {
        types = new ArrayList<>();
        types.add(byte.class);
        types.add(Byte.class);
        types.add(short.class);
        types.add(Short.class);
        types.add(int.class);
        types.add(Integer.class);
        types.add(long.class);
        types.add(Long.class);
    }

    @Override
    public byte[] serialize(Object object, Serialize serialize) {
        int serializeTo = 0;
        if (object.getClass().equals(byte.class) || object.getClass().equals(Byte.class)) {
            serializeTo = 1;
        } else if (object.getClass().equals(short.class) || object.getClass().equals(Short.class)) {
            if (serialize.size() == 1) {
                serializeTo = 1;
            } else {
                serializeTo = 2;
            }
        } else if (object.getClass().equals(int.class) || object.getClass().equals(Integer.class)) {
            if (serialize.size() == 1) {
                serializeTo = 1;
            } else if (serialize.size() == 2) {
                serializeTo = 2;
            } else {
                serializeTo = 4;
            }
        } else if (object.getClass().equals(long.class) || object.getClass().equals(Long.class)) {
            if (serialize.size() == 1) {
                serializeTo = 1;
            } else if (serialize.size() == 2) {
                serializeTo = 2;
            } else if (serialize.size() == 4) {
                serializeTo = 4;
            } else {
                serializeTo = 8;
            }
        } else if (object.getClass().equals(boolean.class) || object.getClass().equals(Boolean.class)) {
            serializeTo = 1;
        }

        if (serializeTo == 1) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(1);
            byteBuffer.order(ByteOrder.BIG_ENDIAN);
            if (object.getClass().equals(short.class) || object.getClass().equals(Short.class)) {
                short temp = (short) object;
                byteBuffer.put((byte)temp);
            } else if (object.getClass().equals(int.class) || object.getClass().equals(Integer.class)) {
                int temp = (int) object;
                byteBuffer.put((byte)temp);
            } else if (object.getClass().equals(long.class) || object.getClass().equals(Long.class)) {
                long temp = (long) object;
                byteBuffer.put((byte)temp);
            } else if (object.getClass().equals(boolean.class) || object.getClass().equals(Boolean.class)) {
                byteBuffer.put((boolean)object ? (byte) 1 : (byte) 0);
            } else {
                byteBuffer.put((byte)object);
            }
            return byteBuffer.array();
        } else if (serializeTo == 2) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(2);
            byteBuffer.order(ByteOrder.BIG_ENDIAN);
            if (object.getClass().equals(byte.class) || object.getClass().equals(Byte.class)) {
                byte temp = (byte) object;
                byteBuffer.putShort((short)temp);
            } else if (object.getClass().equals(int.class) || object.getClass().equals(Integer.class)) {
                int temp = (int) object;
                byteBuffer.putShort((short)temp);
            } else if (object.getClass().equals(long.class) || object.getClass().equals(Long.class)) {
                long temp = (long) object;
                byteBuffer.putShort((short)temp);
            } else {
                byteBuffer.putShort((short)object);
            }
            return byteBuffer.array();
        } else if (serializeTo == 4) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(4);
            byteBuffer.order(ByteOrder.BIG_ENDIAN);
            if (object.getClass().equals(byte.class) || object.getClass().equals(Byte.class)) {
                byte temp = (byte) object;
                byteBuffer.putInt((int)temp);
            } else if (object.getClass().equals(short.class) || object.getClass().equals(Short.class)) {
                short temp = (short) object;
                byteBuffer.putInt((int)temp);
            } else if (object.getClass().equals(long.class) || object.getClass().equals(Long.class)) {
                long temp = (long) object;
                byteBuffer.putInt((int)temp);
            } else {
                byteBuffer.putInt((int)object);
            }
            return byteBuffer.array();
        } else if (serializeTo == 8) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(8);
            byteBuffer.order(serialize.byteOrder().getByteOrder());
            if (object.getClass().equals(byte.class) || object.getClass().equals(Byte.class)) {
                byte temp = (byte) object;
                byteBuffer.putLong((long)temp);
            } else if (object.getClass().equals(short.class) || object.getClass().equals(Short.class)) {
                short temp = (short) object;
                byteBuffer.putLong((long)temp);
            } else if (object.getClass().equals(int.class) || object.getClass().equals(Integer.class)) {
                int temp = (int) object;
                byteBuffer.putLong((long)temp);
            } else {
                byteBuffer.putLong((long)object);
            }
            return byteBuffer.array();
        }
        return new byte[0];
    }

    @Override
    public DeserializedObject deserialize(byte[] buffer, Serialize serialize, Class clazz) {
        int deserializeTo = 0;
        if (clazz.equals(byte.class) || clazz.equals(Byte.class)) {
            deserializeTo = 1;
        } else if (clazz.equals(short.class) || clazz.equals(Short.class)) {
            if (serialize.size() == 1) {
                deserializeTo = 1;
            } else {
                deserializeTo = 2;
            }
        } else if (clazz.equals(int.class) || clazz.equals(Integer.class)) {
            if (serialize.size() == 1) {
                deserializeTo = 1;
            } else if (serialize.size() == 2) {
                deserializeTo = 2;
            } else {
                deserializeTo = 4;
            }
        } else if (clazz.equals(long.class) || clazz.equals(Long.class)) {
            if (serialize.size() == 1) {
                deserializeTo = 1;
            } else if (serialize.size() == 2) {
                deserializeTo = 2;
            } else if (serialize.size() == 4) {
                deserializeTo = 4;
            } else {
               deserializeTo = 8;
            }
        } else if (clazz.equals(boolean.class) || clazz.equals(Boolean.class)) {
            deserializeTo = 1;
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        byteBuffer.order(serialize.byteOrder().getByteOrder());
        if (deserializeTo == 1) {
            if (clazz.equals(boolean.class) || clazz.equals(Boolean.class)) {
                return new DeserializedObject(!(byteBuffer.get() == 0), 1);
            } else {
                return new DeserializedObject(byteBuffer.get(), 1);
            }
        } else if (deserializeTo == 2) {
            return new DeserializedObject(byteBuffer.getShort(), 2);
        } else if (deserializeTo == 4) {
            return new DeserializedObject(byteBuffer.getInt(), 4);
        } else if (deserializeTo == 8) {
            return new DeserializedObject(byteBuffer.getLong(), 8);
        }
        return null;
    }

    @Override
    public int getSize(Object object, Serialize serialize) {
        Class clazz = object.getClass();
        if (clazz.equals(byte.class) || clazz.equals(Byte.class)) {
            return 1;
        } else if (clazz.equals(short.class) || clazz.equals(Short.class)) {
            if (serialize.size() == 1) {
                return 1;
            } else {
                return 2;
            }
        } else if (clazz.equals(int.class) || clazz.equals(Integer.class)) {
            if (serialize.size() == 1) {
                return 1;
            } else if (serialize.size() == 2) {
                return 2;
            } else {
                return 4;
            }
        } else if (clazz.equals(long.class) || clazz.equals(Long.class)) {
            if (serialize.size() == 1) {
                return 1;
            } else if (serialize.size() == 2) {
                return 2;
            } else if (serialize.size() == 4) {
                return 4;
            } else {
                return 8;
            }
        }
        return 0;
    }

    @Override
    public boolean acceptsType(Class clazz) {
        return types.contains(clazz);
    }
}
