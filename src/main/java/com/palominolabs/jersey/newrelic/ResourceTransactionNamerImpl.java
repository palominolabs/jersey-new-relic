package com.palominolabs.jersey.newrelic;

import com.google.common.annotations.VisibleForTesting;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.AbstractSubResourceMethod;
import com.sun.jersey.api.model.PathValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
final class ResourceTransactionNamerImpl implements ResourceTransactionNamer {

    @Override
    @Nonnull
    public String getTransactionName(AbstractResourceMethod am) {

        String transactionName = getPathWithoutSurroundingSlashes(am.getResource().getPath());

        if (!transactionName.isEmpty()) {
            transactionName = "/" + transactionName;
        }

        String httpMethod;
        if (am instanceof AbstractSubResourceMethod) {
            // if this is a subresource, add on the subresource's path component
            AbstractSubResourceMethod asrm = (AbstractSubResourceMethod) am;
            transactionName += "/" + getPathWithoutSurroundingSlashes(asrm.getPath());
            httpMethod = asrm.getHttpMethod();
        } else {
            httpMethod = am.getHttpMethod();
        }

        if (transactionName.isEmpty()) {
            // this happens for WadlResource -- that case actually exists at "application.wadl" though
            transactionName = "(no path)";
        }

        transactionName += " " + httpMethod;

        return transactionName;
    }

    @VisibleForTesting
    @Nonnull
    static String getPathWithoutSurroundingSlashes(@Nullable PathValue pathValue) {
        if (pathValue == null) {
            return "";
        }
        String value = pathValue.getValue();
        if (value.startsWith("/")) {
            value = value.substring(1);
        }
        if (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }

        return value;
    }
}
