package com.palominolabs.newrelic.jersey;/*
* Copyright (c) 2012 Palomino Labs, Inc.
*/

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.AbstractSubResourceLocator;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Adds resource filters to integrate New Relic into the Jersey invocation stack.
 */
@Singleton
public final class NewRelicResourceFilterFactory implements ResourceFilterFactory {

    private static final Logger logger = LoggerFactory.getLogger(NewRelicResourceFilterFactory.class);

    private final ResourceTransactionNamer namer;

    @Inject
    NewRelicResourceFilterFactory(ResourceTransactionNamer namer) {
        this.namer = namer;
    }

    @Override
    public List<ResourceFilter> create(AbstractMethod am) {
        // documented to only be AbstractSubResourceLocator, AbstractResourceMethod, or AbstractSubResourceMethod
        if (am instanceof AbstractSubResourceLocator) {
            // not actually invoked per request, nothing to do
            logger.debug("Ignoring AbstractSubResourceLocator " + am);
            return null;
        } else if (am instanceof AbstractResourceMethod) {
            String transactionName = namer.getTransactionName((AbstractResourceMethod) am);

            return Arrays.asList(new NewRelicTransactionNameResourceFilter(transactionName),
                new NewRelicMappedThrowableResourceFilter());
        } else {
            logger.warn("Got an unexpected instance of " + am.getClass().getName() + ": " + am);
            return null;
        }
    }
}

