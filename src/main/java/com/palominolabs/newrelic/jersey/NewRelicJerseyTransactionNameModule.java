package com.palominolabs.newrelic.jersey;

import com.google.inject.AbstractModule;

public final class NewRelicJerseyTransactionNameModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ResourceTransactionNamer.class).to(ResourceTransactionNamerImpl.class);
        bind(NewRelicResourceFilterFactory.class);
    }
}