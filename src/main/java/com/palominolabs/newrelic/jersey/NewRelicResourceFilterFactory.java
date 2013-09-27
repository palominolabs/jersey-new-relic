package com.palominolabs.newrelic.jersey;
/*
* Copyright (c) 2012 Palomino Labs, Inc.
*/

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.palominolabs.newrelic.NewRelicWrapper;
import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.AbstractSubResourceLocator;
import com.sun.jersey.core.util.FeaturesAndProperties;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Adds resource filters to integrate New Relic into the Jersey invocation stack.
 */
@Singleton
public final class NewRelicResourceFilterFactory implements ResourceFilterFactory {

    private static final Logger logger = LoggerFactory.getLogger(NewRelicResourceFilterFactory.class);

    /**
     * Jersey property to set to control transaction category name. Leave unset to use the New Relic default.
     */
    public static final String TRANSACTION_CATEGORY_PROP = "com.palominolabs.jersey.newrelic.transaction.category";

    private final ResourceTransactionNamer namer;

    private final String category;

    private final NewRelicWrapper newRelicWrapper;

    @Inject
    NewRelicResourceFilterFactory(ResourceTransactionNamer namer, FeaturesAndProperties featuresAndProperties,
        NewRelicWrapper newRelicWrapper) {
        this.namer = namer;
        this.newRelicWrapper = newRelicWrapper;

        Map<String, Object> props = featuresAndProperties.getProperties();
        if (props.containsKey(TRANSACTION_CATEGORY_PROP)) {
            this.category = (String) props.get(TRANSACTION_CATEGORY_PROP);
        } else {
            this.category = null;
        }
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

            return Arrays.asList(new NewRelicTransactionNameResourceFilter(newRelicWrapper, category, transactionName),
                new NewRelicMappedThrowableResourceFilter(newRelicWrapper));
        } else {
            logger.warn("Got an unexpected instance of " + am.getClass().getName() + ": " + am);
            return null;
        }
    }
}

