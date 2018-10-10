package org.didxga.tomcache;

import java.util.Date;
import java.util.Objects;

public class Key {

    protected Date dueDate;
    private String uri;

    public static Key generateKey(String uri, Date expiration) {
        Key key = new Key();
        key.dueDate = expiration;
        key.uri = uri;
        return key;
    }

    public static Key generateKey(String uri) {
        Key key = new Key();
        key.uri = uri;
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Key key = (Key) o;
        return Objects.equals(uri, key.uri);
    }

    @Override
    public int hashCode() {

        return Objects.hash(uri);
    }
}