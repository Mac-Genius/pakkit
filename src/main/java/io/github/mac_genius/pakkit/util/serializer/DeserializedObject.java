package io.github.mac_genius.pakkit.util.serializer;

public class DeserializedObject {
    private Object object;
    private int read;

    public DeserializedObject(Object object, int read) {
        this.object = object;
        this.read = read;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public int getRead() {
        return read;
    }

    public void setRead(int read) {
        this.read = read;
    }
}
