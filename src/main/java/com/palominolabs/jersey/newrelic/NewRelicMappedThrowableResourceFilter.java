package com.palominolabs.jersey.newrelic;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Informs New Relic about mapped throwables that are being handled by Jersey rather than propagated up the servlet
 * handling chain.
 */
@ThreadSafe
final class NewRelicMappedThrowableResourceFilter implements ResourceFilter, ContainerResponseFilter {

    private final NewRelicWrapper newRelicWrapper;

    NewRelicMappedThrowableResourceFilter(NewRelicWrapper newRelicWrapper) {
        this.newRelicWrapper = newRelicWrapper;
    }

    @Override
    public ContainerRequestFilter getRequestFilter() {
        // don't filter requests
        return null;
    }

    @Override
    public ContainerResponseFilter getResponseFilter() {
        return this;
    }

    @Override
    public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
        Throwable mappedThrowable = response.getMappedThrowable();
        if (mappedThrowable != null) {
            newRelicWrapper.noticeError(mappedThrowable);
        }
        return response;
    }
}
