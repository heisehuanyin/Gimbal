package softart.basictype;

public class SWPair<K,V> {
    private final K key;
    private final V value;

    public SWPair(K key, V value){
        this.key = key;
        this.value = value;
    }

    public K getKey(){
        return this.key;
    }

    public V getValue(){
        return this.value;
    }
}
