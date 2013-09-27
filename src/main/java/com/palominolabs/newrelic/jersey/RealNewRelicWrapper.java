package com.palominolabs.newrelic.jersey;

import com.palominolabs.newrelic.NewRelicWrapper;

import javax.annotation.Nullable;

final class RealNewRelicWrapper implements NewRelicWrapper {
    @Override
    public void noticeError(Throwable t) {
        // TODO
    }

    @Override
    public void setTransactionName(@Nullable String category, String transactionName) {
        // TODO
    }
}
