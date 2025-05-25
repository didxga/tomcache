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

    private CacheRepository cacheRepository = new RedisCacheRepository();
    private StaleDataWatcher staleDataWatcher;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // TODO: StaleDataWatcher currently expects a MemoryCacheRepository.
        // This will be addressed in a subsequent step to make it compatible with CacheRepository interface
        // or specifically with RedisCacheRepository.
        if (cacheRepository instanceof MemoryCacheRepository) {
            staleDataWatcher = new StaleDataWatcher((MemoryCacheRepository) cacheRepository);
        } else {
            System.err.println("TomcacheFilter: StaleDataWatcher is not compatible with the current CacheRepository implementation (" + cacheRepository.getClass().getName() + ") and will not be initialized.");
            staleDataWatcher = null;
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        String uri = Util.getURI(httpServletRequest);
        Key key = Key.generateKey(uri);
        Value value;
        if(cacheRepository.has(key)) {
            //when hit cache, we get the cache from cache repository and break the filter chain by return directly

            value =  cacheRepository.retrieve(key);
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
            cacheRepository.store(key, value);
        }

    }

    @Override
    public void destroy() {
        if (staleDataWatcher != null) {
            staleDataWatcher.shutdown();
        }
        if (cacheRepository instanceof RedisCacheRepository) {
            ((RedisCacheRepository) cacheRepository).close();
        }
        // If cacheRepository could be other implementations,
        // and a generic clear is needed, CacheRepository interface should define clear().
    }
}
