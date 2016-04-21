package Pipeline;

import java.util.HashMap;

/**
 * Created by Rune on 29-03-2016.
 */
public class BiMap<K,V> {

    HashMap<K,V> map = new HashMap<K, V>();
    HashMap<V,K> inversedMap = new HashMap<V, K>();


    void put(K k, V v) {
        map.put(k, v);
        inversedMap.put(v, k);
    }

    V get(K k) {
        return map.get(k);
    }

    K getKey(V v) {
        return inversedMap.get(v);
    }

}
