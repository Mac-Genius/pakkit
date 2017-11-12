package io.github.mac_genius.pakkit.io;

import io.github.mac_genius.pakkit.packet.Packet;

import java.io.InputStream;

public interface PacketReader {
    Packet readPacket(InputStream inputStream);
}
