package com.palominolabs.jersey.newrelic;

import com.google.inject.AbstractModule;

public final class JerseyNewRelicModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ResourceTransactionNamer.class).to(ResourceTransactionNamerImpl.class);
        bind(NewRelicResourceFilterFactory.class);
        bind(NewRelicWrapper.class).to(RealNewRelicWrapper.class);
    }
}