package softart.task.talk;

import java.util.HashMap;

class BlockedMap<T> {
    private HashMap<String, T> container = new HashMap<>();

    public void put(String key, T value) {
        synchronized (this) {
            container.put(key, value);
        }
    }

    public T get(String key) {
        synchronized (this) {
            return container.get(key);
        }
    }

    public void remove(String key) {
        synchronized (this) {
            container.remove(key);
        }
    }
}
