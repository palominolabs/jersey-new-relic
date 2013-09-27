package com.palominolabs.jersey.newrelic;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * New Relic's java API uses all static calls, which makes testing hard. Thus, this wrapper.
 */
@ThreadSafe
public interface NewRelicWrapper {
    void noticeError(Throwable t);

    void setTransactionName(@Nullable String category, String transactionName);
}
