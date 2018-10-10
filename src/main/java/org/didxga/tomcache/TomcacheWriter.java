package org.didxga.tomcache;

import java.io.*;

public class TomcacheWriter extends PrintWriter {

    private String copy;

    public TomcacheWriter(Writer out) {
        super(out);
    }

    public TomcacheWriter(Writer out, boolean autoFlush) {
        super(out, autoFlush);
    }

    public TomcacheWriter(OutputStream out) {
        super(out);
    }

    public TomcacheWriter(OutputStream out, boolean autoFlush) {
        super(out, autoFlush);
    }

    public TomcacheWriter(String fileName) throws FileNotFoundException {
        super(fileName);
    }

    public TomcacheWriter(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        super(fileName, csn);
    }

    public TomcacheWriter(File file) throws FileNotFoundException {
        super(file);
    }

    public TomcacheWriter(File file, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        super(file, csn);
    }

    @Override
    public void write(String s) {
        copy = s;
        super.write(s);
    }

    public String getCopy() {
        return this.copy;
    }
}
