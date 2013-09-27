package com.palominolabs.newrelic.jersey;

import com.google.inject.AbstractModule;
import com.palominolabs.newrelic.NewRelicWrapper;

public final class JerseyNewRelicModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ResourceTransactionNamer.class).to(ResourceTransactionNamerImpl.class);
        bind(NewRelicResourceFilterFactory.class);
        bind(NewRelicWrapper.class).to(RealNewRelicWrapper.class);
    }
}