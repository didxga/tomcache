package org.didxga.tomcache;

import javax.servlet.ServletResponse;
import javax.servlet.ServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;

public class TomcacheResponse extends ServletResponseWrapper {
    /**
     * Creates a ServletResponse adaptor wrapping the given response object.
     *
     * @param response the {@link ServletResponse} to be wrapped
     * @throws IllegalArgumentException if the response is null.
     */
    public TomcacheResponse(ServletResponse response) {
        super(response);
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return  new TomcacheWriter(super.getWriter());
    }
}
