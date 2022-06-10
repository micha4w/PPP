package com.Practice.Useful;

import java.util.ArrayList;

public class UUIDList extends ArrayList<Boolean> {
    public UUIDList () {
        super();
    }

    public int get () {
        int j;

        for ( j = 0; j < this.size(); j++ ) {
            if ( this.get(j) ) {
                this.set(j, false);
                return j;
            }
        }

        this.add(false);
        return j;
    }

    public void add ( int pos ) {
        while ( this.size() <= pos ) {
            this.add(true);
        }
        this.set(pos, false);
    }

    public Boolean remove (int i) {
        this.set(i, false);
        return true;
    }
}
