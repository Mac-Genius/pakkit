package io.github.mac_genius.pakkit.io;

import io.github.mac_genius.pakkit.event.EventManager;
import io.github.mac_genius.pakkit.listener.Listener;
import io.github.mac_genius.pakkit.packet.Packet;
import io.github.mac_genius.pakkit.util.PacketUtils;

import java.io.IOException;
import java.net.Socket;
import java.util.TimerTask;

public class SocketManager extends TimerTask {
    private Socket socket;
    private EventManager eventManager;
    private PacketReader packetReader;

    public SocketManager(PacketReader packetReader) {
        eventManager = new EventManager();
        this.packetReader = packetReader;
    }

    public SocketManager(PacketReader packetReader, String url, int port) {
        this.packetReader = packetReader;
        try {
            socket = new Socket(url, port);
        } catch (IOException e) {
            e.printStackTrace();
            socket = null;
        }
        eventManager = new EventManager();
    }

    public SocketManager(Socket socket) {
        this.socket = socket;
        eventManager = new EventManager();
    }

    @Override
    public void run() {
        try {
            while (socket != null && !socket.isClosed() && !socket.isInputShutdown()) {
                Packet packet = packetReader.readPacket(socket.getInputStream());
                eventManager.handlePacket(packet, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean openConnection(String url, int port) {
        try {
            socket = new Socket(url, port);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean close() {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                return false;
            }
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
        if (socket != null && !socket.isClosed() && !socket.isOutputShutdown()) {
            try {
                socket.getOutputStream().write(utils.serializePacket(packet));
                socket.getOutputStream().flush();
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
            return packetReader.readPacket(socket.getInputStream());
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
