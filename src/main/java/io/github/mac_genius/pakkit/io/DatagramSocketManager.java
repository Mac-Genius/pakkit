package io.github.mac_genius.pakkit.io;

import io.github.mac_genius.pakkit.event.EventManager;
import io.github.mac_genius.pakkit.listener.Listener;
import io.github.mac_genius.pakkit.packet.Packet;
import io.github.mac_genius.pakkit.util.PacketUtils;

import java.io.IOException;
import java.net.*;
import java.util.TimerTask;

public class DatagramSocketManager extends TimerTask {
    private DatagramSocket socket;
    private EventManager eventManager;
    private PacketReader packetReader;

    public DatagramSocketManager(PacketReader packetReader) {
        eventManager = new EventManager();
        this.packetReader = packetReader;
    }

    public DatagramSocketManager(PacketReader packetReader, int bindPort, String url, int port) {
        this.packetReader = packetReader;
        try {
            socket = new DatagramSocket(bindPort);
            socket.connect(InetAddress.getByName(url), port);
        } catch (IOException e) {
            e.printStackTrace();
            socket = null;
        }
        eventManager = new EventManager();
    }

    public DatagramSocketManager(DatagramSocket socket, String url, int port) throws IOException {
        this.socket = socket;
        socket.connect(InetAddress.getByName(url), port);
        eventManager = new EventManager();
    }

    public DatagramSocketManager(DatagramSocket socket) {
        this.socket = socket;
        eventManager = new EventManager();
    }

    public boolean connect(String url, int port) {
        if (socket != null) {
            try {
                socket.connect(InetAddress.getByName(url), port);
                return true;
            } catch (UnknownHostException e) {
                return false;
            }
        }
        return false;
    }

    @Override
    public void run() {
        try {
            while (socket != null && !socket.isClosed()) {
                DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
                socket.receive(packet);
                Packet inputPacket = packetReader.readPacket(packet.getData());
                eventManager.handlePacket(inputPacket, socket, packet);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean close() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        return true;
    }

    public void clear() {
        eventManager.clear();
    }

    public static SocketManager manageSocket(Socket socket) {
        return new SocketManager(socket);
    }

    public boolean sendPacket(Packet packet) {
        PacketUtils utils = PacketUtils.getInstance();
        if (socket != null && !socket.isClosed()) {
            byte[] data = utils.serializePacket(packet);
            try {
                DatagramPacket outputPacket = new DatagramPacket(data, data.length);
                socket.send(outputPacket);
                return true;
            } catch (IOException e) {
                // May want to throw custom exception so I can get error?
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean sendPacket(Packet packet, String url, int port) {
        PacketUtils utils = PacketUtils.getInstance();
        if (socket != null) {
            byte[] data = utils.serializePacket(packet);
            try {
                DatagramPacket outputPacket = new DatagramPacket(data, data.length, InetAddress.getByName(url), port);
                socket.send(outputPacket);
                return true;
            } catch (IOException e) {
                // May want to throw custom exception so I can get error?
                return false;
            }
        } else {
            return false;
        }
    }

    public Packet readPacket() {
        try {
            DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
            socket.receive(packet);
            return packetReader.readPacket(packet.getData());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void registerListener(Listener listener) {
        eventManager.registerListener(listener);
    }

    public void removeListener(Listener listener) {
        eventManager.removeListener(listener);
    }

    public PacketReader getPacketReader() {
        return packetReader;
    }
}
