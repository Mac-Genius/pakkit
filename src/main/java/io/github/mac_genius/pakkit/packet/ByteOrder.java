package io.github.mac_genius.pakkit.packet;

public enum ByteOrder {
    BIG_ENDIAN(java.nio.ByteOrder.BIG_ENDIAN),
    LITTLE_ENDIAN(java.nio.ByteOrder.LITTLE_ENDIAN);

    private java.nio.ByteOrder byteOrder;

    ByteOrder(java.nio.ByteOrder byteOrder) {
        this.byteOrder = byteOrder;
    }

    public java.nio.ByteOrder getByteOrder() {
        return byteOrder;
    }
}
