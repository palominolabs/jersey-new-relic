package com.palominolabs.jersey.newrelic;

import com.newrelic.api.agent.NewRelic;

import javax.annotation.Nullable;

final class RealNewRelicWrapper implements NewRelicWrapper {

    @Override
    public void noticeError(Throwable t) {
        NewRelic.noticeError(t);
    }

    @Override
    public void setTransactionName(@Nullable String category, String transactionName) {
        NewRelic.setTransactionName(category, transactionName);
    }
}
