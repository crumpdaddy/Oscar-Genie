import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

public class Node<K, T> {
    private HashMap<K, T> children;

    public Node() {
        children = new HashMap<>();
    }

    public T get(K key) throws Exception {
        if (children.containsKey(key)) {
            return children.get(key);
        }
        else {
            throw new Exception("Object does not exist");
        }
    }

    public Set<K> keySet() {
        if (children.isEmpty()) {
            return Collections.<K>emptySet();
        }
        else {
            return children.keySet();
        }
    }

    public void add(K key, T data) throws Exception {
        if (key == null) {
            throw new Exception("Key cannot be null");
        }
        if (data == null) {
            throw new Exception("Data cannot be null");
        }
        children.put(key, data);
    }

    public void remove(K key) throws Exception {
        if (key == null) {
            throw new Exception("Key cannot be null");
        }
        if (!children.containsKey(key)) {
            throw new Exception("Object does not exist");
        }
        children.remove(key);
    }

}
