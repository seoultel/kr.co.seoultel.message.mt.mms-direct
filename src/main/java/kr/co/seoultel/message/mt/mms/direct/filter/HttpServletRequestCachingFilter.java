package kr.co.seoultel.message.mt.mms.direct.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Slf4j
@Component
public class HttpServletRequestCachingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        CachedHttpServletRequest cachedHttpServletRequest = new CachedHttpServletRequest((HttpServletRequest) request);
        chain.doFilter(cachedHttpServletRequest, response);
    }
}