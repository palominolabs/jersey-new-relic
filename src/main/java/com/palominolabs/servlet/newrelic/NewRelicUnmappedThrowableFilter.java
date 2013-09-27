package com.palominolabs.servlet.newrelic;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.palominolabs.jersey.newrelic.NewRelicWrapper;

import javax.annotation.concurrent.ThreadSafe;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * Informs New Relic about throwables that propagate up through the servlet layers (instead of, for instance, being
 * handled by Jersey).
 */
@ThreadSafe
@Singleton
public final class NewRelicUnmappedThrowableFilter implements Filter {

    private final NewRelicWrapper newRelicWrapper;

    @Inject
    NewRelicUnmappedThrowableFilter(NewRelicWrapper newRelicWrapper) {
        this.newRelicWrapper = newRelicWrapper;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // no op
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws
        IOException, ServletException {

        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (Throwable t) {
            newRelicWrapper.noticeError(t);
            throw t;
        }
    }

    @Override
    public void destroy() {
        // no op
    }
}
