package com.palominolabs.newrelic.jersey;

import com.newrelic.api.agent.NewRelic;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;

import javax.annotation.concurrent.Immutable;

/**
 * Uses the name provided by {@link NewRelicResourceFilterFactory} to assign the New Relic transaction name for the
 * active request.
 */
@Immutable
final class NewRelicTransactionNameResourceFilter implements ResourceFilter, ContainerRequestFilter {

    private final String transactionName;

    /**
     * @param transactionName the transaction name that this filter will apply to all requests.
     */
    NewRelicTransactionNameResourceFilter(String transactionName) {
        this.transactionName = transactionName;
    }

    @Override
    public ContainerRequestFilter getRequestFilter() {
        return this;
    }

    @Override
    public ContainerResponseFilter getResponseFilter() {
        // don't filter responses
        return null;
    }

    @Override
    public ContainerRequest filter(ContainerRequest request) {
        // TODO
        NewRelic.setTransactionName("some category", this.transactionName);
        return request;
    }
}
