package com.Practice.Useful;

import java.util.HashMap;

public class UUIDMap<T> extends HashMap<Integer, T> {
    public UUIDMap() {
        super();
    }

    public UUIDMap(T t) {
        super();
        this.put(0, t);
    }

    public int add(T t) {
        int expectedK = 0;
        for ( int currentK : this.keySet() ) {
            if ( expectedK < currentK ) {
                break;
            }
            expectedK = currentK + 1;
        }
        this.put(expectedK, t);
        return expectedK;
    }
}