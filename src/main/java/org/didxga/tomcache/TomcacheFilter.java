package org.didxga.tomcache;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

@SuppressWarnings("unused")
public class TomcacheFilter implements Filter {

    private MemoryCacheRepository cacheRepository = new MemoryCacheRepository();
    private StaleDataWatcher staleDataWatcher;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        staleDataWatcher = new StaleDataWatcher(cacheRepository);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        String uri = Util.getURI(httpServletRequest);
        Key key = Key.generateKey(uri);
        Value value;
        if(cacheRepository.cache.containsKey(key)) {
            //when hit cache, we get the cache from cache repository and break the filter chain by return directly

            value =  cacheRepository.cache.get(key);
            if(value == null) {
                //check to make sure cache is not removed upon the time we getting it from repository
                return;
            }
            httpServletResponse.getWriter().write(value.body);
            if(value.headers !=null && value.headers.size() > 0) {
                Iterator<String> keys = value.headers.keySet().iterator();
                while(keys.hasNext()) {
                    String k = keys.next();
                    httpServletResponse.setHeader(k, value.headers.get(k));
                }
            }
            return;
        } else {
            //when cache is missing, we pass the request to the filter chain and cache later when process return from filter chain
            TomcacheResponse tomcacheResponse = new TomcacheResponse(servletResponse);
            filterChain.doFilter(servletRequest, tomcacheResponse);

            Date expireDate = Util.getExpirationDate(httpServletResponse);
            key = key.generateKey(uri, expireDate);
            value = new Value();
            value.body = ((TomcacheWriter)tomcacheResponse.getWriter()).getCopy();
            Collection<String> headerNames =  httpServletResponse.getHeaderNames();
            if (headerNames !=null && headerNames.size() > 0) {
                value.headers = new HashMap<>(headerNames.size());
                for(String headerName : headerNames) {
                    value.headers.put(headerName, httpServletResponse.getHeader(headerName));
                }
            }
            cacheRepository.cache.put(key, value);
        }

    }

    @Override
    public void destroy() {
        staleDataWatcher.shutdown();
        cacheRepository.cache.clear();
    }
}
