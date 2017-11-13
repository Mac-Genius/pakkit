package io.github.mac_genius.pakkit.util;

import io.github.mac_genius.pakkit.annotation.Length;
import io.github.mac_genius.pakkit.annotation.Serialize;
import io.github.mac_genius.pakkit.io.PacketReader;
import io.github.mac_genius.pakkit.packet.Packet;
import io.github.mac_genius.pakkit.util.serializer.*;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

public class PacketUtils {
    private static PacketUtils instance;
    private List<Serializer> serializerList;

    public PacketUtils() {
        this.serializerList = new ArrayList<>();
        this.serializerList.add(new PrimitiveSerializer());
        this.serializerList.add(new StringSerializer());
        this.serializerList.add(new ArraySerializer());
        this.serializerList.add(new CollectionSerializer());
    }

    public void addSerializer(Serializer serializer) {
        this.serializerList.add(serializer);
    }

    public Serializer getSerializer(Class clazz) {
        for (Serializer s : serializerList) {
            if (s.acceptsType(clazz)) {
                return s;
            }
        }
        return null;
    }

    private byte calculate(byte[] message, boolean compliment) {
        int sum = 0;
        for (int i = 0; i < message.length; i++) {
            int temp = message[i];
            temp = temp & 0xFF;
            sum += temp;
            if ((sum & 0xFFFFFF00) > 0) {
                sum = (sum & 0xFF);
                sum += 1;
            }
        }
        if (compliment) {
            sum = ~sum;
            sum = sum & 0xFF;
        }
        return (byte)sum;
    }

    public byte calculateSum(byte[] array) {
        return calculate(array, false);
    }

    public byte calculateChecksum(byte[] array) {
        return calculate(array, true);
    }

    public byte[] serializePacket(Packet packet) {
        try {
            // Get all serializable
            ArrayList<Field> fields = getFields(packet.getClass(), Serialize.class);

            // Calculate packet length and set length fields
            int packetLength = getPacketLength(packet);
            ArrayList<Field> lengthFields = getFields(packet.getClass(), Length.class);
            for (Field f : lengthFields) {
                boolean isAccessible = f.isAccessible();
                f.setAccessible(true);
                if (f.getType().equals(byte.class) || f.getType().equals(Byte.class)) {
                    f.set(packet, (byte) packetLength);
                } else if (f.getType().equals(short.class) || f.getType().equals(Short.class)) {
                    f.set(packet, (short) packetLength);
                } else if (f.getType().equals(long.class) || f.getType().equals(Long.class)) {
                    f.set(packet, (long) packetLength);
                } else {
                    f.set(packet, packetLength);
                }
                f.setAccessible(isAccessible);
            }


            ByteBuffer byteBuffer = ByteBuffer.allocate(0);
            for (Field f : fields) {
                boolean isAccessible = f.isAccessible();
                f.setAccessible(true);
                Object object = f.get(packet);
                if (object != null) {
                    Serializer serializer = getSerializer(object.getClass());
                    if (serializer != null) {
                        byte[] data = serializer.serialize(object, f);
                        byte[] temp = byteBuffer.array();
                        byteBuffer = ByteBuffer.allocate(temp.length + data.length);
                        byteBuffer.put(temp);
                        byteBuffer.put(data);
                    }
                }
                f.setAccessible(isAccessible);
            }
            return byteBuffer.array();
        } catch (IOException | IllegalAccessException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    public int getPacketLength(Packet packet) {
        try {
            ArrayList<Field> fields = getFields(packet.getClass(), Serialize.class);
            int length = 0;
            for (Field f : fields) {
                boolean isAccessible = f.isAccessible();
                f.setAccessible(true);
                Object object = f.get(packet);
                if (object != null) {
                    Serializer serializer = getSerializer(object.getClass());
                    if (serializer != null) {
                        if (f.getAnnotation(Length.class) != null) {
                            if (f.getAnnotation(Length.class).includeLength()) {
                                length += serializer.getSize(object, f);
                            }
                        } else {
                            length += serializer.getSize(object, f);
                        }
                    }
                }
                f.setAccessible(isAccessible);
            }
            return length;
        } catch (IOException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void deserializePacket(byte[] buffer, Packet packet) {
        try {
            ArrayList<Field> fields = getFields(packet.getClass(), Serialize.class);
            int offset = 0;
            for (Field f : fields) {
                if (offset >= buffer.length) {
                    break;
                }
                boolean isAccessible = f.isAccessible();
                f.setAccessible(true);
                Serializer serializer = getSerializer(f.getType());
                if (serializer != null) {
                    DeserializedObject object = serializer.deserialize(Arrays.copyOfRange(buffer, offset, buffer.length), f);
                    f.set(packet, object.getObject());
                    offset += object.getRead();
                }
                f.setAccessible(isAccessible);
            }
        } catch (IOException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<Field> getFields(Class clazz, Class annotationClass) throws IOException, IllegalAccessException {
        if (!clazz.equals(Object.class)) {
            ArrayList<Field> fieldList = getFields(clazz.getSuperclass(), annotationClass);
            TreeSet<FieldWrapper> moreFields = new TreeSet<>();
            Field[] fields = clazz.getDeclaredFields();
            for (Field f : fields) {
                Annotation ann = f.getAnnotation(annotationClass);
                if (ann != null) {
                    if (ann.equals(Serialize.class)) {
                        moreFields.add(new FieldWrapper(f, ((Serialize)ann).order()));
                    } else {
                        fieldList.add(f);
                    }
                }
            }
            for (FieldWrapper fw : moreFields) {
                fieldList.add(fw.f);
            }
            return fieldList;
        } else {
            return new ArrayList<>();
        }
    }

    private class FieldWrapper implements Comparable<FieldWrapper> {
        Field f;
        int x;

        public FieldWrapper(Field f, int x) {
            this.f = f;
            this.x = x;
        }

        public int compareTo(FieldWrapper wrapper) {
            return this.x - wrapper.x;
        }
    }

    public static PacketUtils getInstance() {
        if (instance == null) {
            instance = new PacketUtils();
        }
        return instance;
    }

    public byte[] getVarInt(int number) {
        int temp = (number << 1) ^ (number >> 31);
        int total = 0;
        ByteBuffer byteBuffer = ByteBuffer.allocate(100);
        while (temp != 0) {
            byte current = (byte)(temp & 0x7F);
            current |= temp >>> 7 != 0 ? 0x80 : 0x00;
            byteBuffer.put(current);
            temp = temp >>> 7;
            total++;
        }
        return Arrays.copyOfRange(byteBuffer.array(), 0, total);
    }

    public int fromVarInt(byte[] bytes) {
        int out = 0;
        for (int i = bytes.length - 1; i >= 0; i--) {
            out = out << 7;
            out |= bytes[i] & 0x7F;
        }
        out = ((out << 31) >> 31) ^ (out >>> 1);
        return out;
    }

    private byte[] readVarInt(InputStream inputStream) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        ByteBuffer byteBuffer = ByteBuffer.allocate(100);
        byte current = dataInputStream.readByte();
        int count = 0;
        while ((current & 0x80) == 0x80) {
            byteBuffer.put(current);
            count++;
            current = dataInputStream.readByte();
        }
        byteBuffer.put(current);
        count++;
        return Arrays.copyOfRange(byteBuffer.array(), 0, count);
    }
}
