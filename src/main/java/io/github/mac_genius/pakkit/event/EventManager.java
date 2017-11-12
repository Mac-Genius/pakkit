package io.github.mac_genius.pakkit.event;

import io.github.mac_genius.pakkit.annotation.EventHandler;
import io.github.mac_genius.pakkit.listener.Listener;
import io.github.mac_genius.pakkit.packet.Packet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class EventManager {
    private HashMap<Class<? extends Packet>, PriorityQueue<SortableListener>> map;
    private ScheduledThreadPoolExecutor threadPoolExecutor;

    public EventManager() {
        this.map = new HashMap<>();
        threadPoolExecutor = new ScheduledThreadPoolExecutor(4);
    }

    public void registerListener(Listener listener) {
        Method[] methods = listener.getClass().getDeclaredMethods();
        for (Method method : methods) {
            EventHandler handler = method.getDeclaredAnnotation(EventHandler.class);
            if (handler != null) {
                for (Class clazz : method.getParameterTypes()) {
                    if (Packet.class.isAssignableFrom(clazz)) {
                        if (!map.containsKey(clazz)) {
                            map.put(clazz, new PriorityQueue<>());
                        }
                        map.get(clazz).add(new SortableListener(method, listener, handler.priority()));
                        break;
                    }
                }
            }
        }
    }

    public void removeListener(Listener listener) {
        HashMap<Class<? extends Packet>, PriorityQueue<SortableListener>> toRemove = new HashMap<>();
        for (Class<? extends Packet> packet : map.keySet()) {
            PriorityQueue<SortableListener> listeners = map.get(packet);
            for (SortableListener listener1 : listeners) {
                if (listener1.listener.equals(listener)) {
                    if (toRemove.containsKey(packet)) {
                        toRemove.put(packet, new PriorityQueue<>());
                    }
                    toRemove.get(packet).add(listener1);
                }
            }
        }
        for (Class<? extends Packet> packet : toRemove.keySet()) {
            PriorityQueue<SortableListener> listeners = toRemove.get(packet);
            for (SortableListener listener1 : listeners) {
                map.get(packet).remove(listener1);
            }
        }
    }

    public void handlePacket(Packet packet, Socket socket) throws IOException {
        PriorityQueue<SortableListener> listeners = map.get(packet.getClass());
        for (SortableListener listener : listeners) {
            Class[] classes = listener.method.getParameterTypes();
            Object[] params = new Object[classes.length];
            boolean execute = false;
            for (int i = 0; i < classes.length; i++) {
                if (Packet.class.isAssignableFrom(classes[i]) && classes[i].equals(packet.getClass())) {
                    params[i] = packet;
                    execute = true;
                } else if (InputStream.class.equals(classes[i])) {
                    params[i] = socket.getInputStream();
                } else if (OutputStream.class.equals(classes[i])) {
                    params[i] = socket.getOutputStream();
                } else if (Socket.class.equals(classes[i])) {
                    params[i] = socket;
                } else {
                    params[i] = null;
                }
            }
            if (execute) {
                threadPoolExecutor.execute(new PacketHandler(listener.method, listener.listener, params));
            }
        }
    }

    private class SortableListener implements Comparable<SortableListener> {
        private Method method;
        private Listener listener;
        private EventPriority priority;

        SortableListener(Method method, Listener listener, EventPriority priority) {
            this.method = method;
            this.listener = listener;
            this.priority = priority;
        }

        @Override
        public int compareTo(SortableListener o) {
            return this.priority.getLevel() - o.priority.getLevel();
        }

        @Override
        public boolean equals(Object object) {
            if (object instanceof SortableListener) {
                if (method.equals(((SortableListener) object).method)
                        && listener.equals(((SortableListener) object).listener)
                        && priority.getLevel() == ((SortableListener) object).priority.getLevel()) {
                    return true;
                }
            }
            return false;
        }
    }

    private class PacketHandler extends TimerTask {
        private Object[] params;
        private Method method;
        private Listener listener;

        public PacketHandler(Method method, Listener listener, Object...params) {
            this.method = method;
            this.listener = listener;
            this.params = params;
        }

        @Override
        public void run() {
            try {
                this.method.invoke(listener, params);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void clear() {
        for (Class<? extends Packet> clazz : map.keySet()) {
            map.get(clazz).clear();
        }
        map.clear();
    }
}
