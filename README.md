[New Relic](http://newrelic.com/)'s built-in servlet request transaction naming doesn't do a very good job of handling JAX-RS requests, so this library provides some [Jersey 1](https://jersey.java.net/) helpers to get better New Relic transaction names. It also allows you to track both mapped and un-mapped exceptions with New Relic.

# Installation

You need to register both a `javax.servlet.Filter` ([`NewRelicUnmappedThrowableFilter`](https://github.com/palominolabs/jersey-new-relic/blob/master/src/main/java/com/palominolabs/servlet/newrelic/NewRelicUnmappedThrowableFilter.java)) and a Jersey `ResourceFilterFactory` ([`NewRelicResourceFilterFactory`](https://github.com/palominolabs/jersey-new-relic/blob/master/src/main/java/com/palominolabs/jersey/newrelic/NewRelicResourceFilterFactory.java)).

Here's how to register the servlet filter using Guice Servlet:
```
// in your ServletModule
bind(NewRelicUnmappedThrowableFilter.class);
filter("/*").through(NewRelicUnmappedThrowableFilter.class);
```

And the Jersey filter factory:
```
// in your ServletModule
Map<String, String> initParams = new HashMap<>();
initParams.put(ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES,
    NewRelicResourceFilterFactory.class.getCanonicalName());

bind(GuiceContainer.class);
serve("/*").with(GuiceContainer.class, initParams);
```

Finally, you'll also want the main module for this library:
```
// in some module
install(new JerseyNewRelicModule());
```

If you want to control the New Relic "category" used in transaction names, set the `NewRelicResourceFilterFactory.TRANSACTION_CATEGORY_PROP` property when you're setting your init params:
```
Map<String, String> initParams = new HashMap<>();
initParams.put(NewRelicResourceFilterFactory.TRANSACTION_CATEGORY_PROP, "someCategory");
initParams.put(ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES,
    NewRelicResourceFilterFactory.class.getCanonicalName());
```

If you do not specify the category, New Relic's default will be used.