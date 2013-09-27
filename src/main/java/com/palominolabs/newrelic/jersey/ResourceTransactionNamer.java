package com.palominolabs.newrelic.jersey;

import com.sun.jersey.api.model.AbstractResourceMethod;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Generates a useful transaction name based on metadata extracted from an {@link AbstractResourceMethod}.
 */
@ThreadSafe
public interface ResourceTransactionNamer {
    /**
     * @param am resource method
     * @return a string name used as new relic transaction name for the resource method
     */
    @Nonnull
    String getTransactionName(AbstractResourceMethod am);
}
