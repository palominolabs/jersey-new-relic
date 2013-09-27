package com.palominolabs.jersey.newrelic;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.ServletModule;
import com.google.inject.util.Modules;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import com.palominolabs.config.ConfigModuleBuilder;
import com.palominolabs.servlet.newrelic.NewRelicThrowableFilter;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.annotation.Nullable;
import javax.servlet.DispatcherType;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.LogManager;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FullStackTest {

    private static final int PORT = 18080;
    private Server server;

    private AsyncHttpClient httpClient;
    private StubNewRelicWrapper wrapper;

    @Before
    public void setUp() throws Exception {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();

        wrapper = new StubNewRelicWrapper();

        httpClient = new AsyncHttpClient();
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
    }

    @Test
    public void testInvokeResourceWithCategory() throws Exception {
        final Map<String, String> initParams = new HashMap<>();
        initParams.put(NewRelicResourceFilterFactory.TRANSACTION_CATEGORY_PROP, "someCategory");
        initParams.put(ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES,
            NewRelicResourceFilterFactory.class.getCanonicalName());

        server = getServer(getInjector(initParams).getInstance(GuiceFilter.class));
        server.start();

        Response response = httpClient.prepareGet("http://localhost:" + PORT + "/foo").execute().get();

        assertEquals(200, response.getStatusCode());

        assertEquals(newArrayList("someCategory:/foo GET"), wrapper.getNames());
        assertTrue(wrapper.getThrowables().isEmpty());
    }

    @Test
    public void testInvokeResourceWithoutCategory() throws Exception {
        final Map<String, String> initParams = new HashMap<>();
        initParams.put(ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES,
            NewRelicResourceFilterFactory.class.getCanonicalName());

        server = getServer(getInjector(initParams).getInstance(GuiceFilter.class));
        server.start();

        Response response = httpClient.prepareGet("http://localhost:" + PORT + "/foo").execute().get();

        assertEquals(200, response.getStatusCode());

        assertEquals(newArrayList("/foo GET"), wrapper.getNames());
        assertTrue(wrapper.getThrowables().isEmpty());
    }

    @Test
    public void testInvokeThrowsResource() throws Exception {
        final Map<String, String> initParams = new HashMap<>();
        initParams.put(ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES,
            NewRelicResourceFilterFactory.class.getCanonicalName());

        server = getServer(getInjector(initParams).getInstance(GuiceFilter.class));
        server.start();

        Response response = httpClient.prepareGet("http://localhost:" + PORT + "/foo/throw").execute().get();

        assertEquals(500, response.getStatusCode());

        assertEquals(newArrayList("/foo/throw GET"), wrapper.getNames());
        assertEquals(1, wrapper.getThrowables().size());
        assertEquals("zomg", wrapper.getThrowables().get(0).getMessage());
    }

    @Test
    public void testInvokeThrowsMappedResource() throws Exception {
        final Map<String, String> initParams = new HashMap<>();
        initParams.put(ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES,
            NewRelicResourceFilterFactory.class.getCanonicalName());

        server = getServer(getInjector(initParams).getInstance(GuiceFilter.class));
        server.start();

        Response response = httpClient.prepareGet("http://localhost:" + PORT + "/foo/throwMapped").execute().get();

        assertEquals(javax.ws.rs.core.Response.Status.NOT_ACCEPTABLE.getStatusCode(), response.getStatusCode());

        assertEquals(newArrayList("/foo/throwMapped GET"), wrapper.getNames());
        assertEquals(1, wrapper.getThrowables().size());
        assertEquals("asdf", wrapper.getThrowables().get(0).getMessage());
    }

    private Injector getInjector(final Map<String, String> initParams) {
        return Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                binder().requireExplicitBindings();
                install(new ServletModule() {
                    @Override
                    protected void configureServlets() {
                        bind(GuiceContainer.class);
                        bind(NewRelicThrowableFilter.class);
                        serve("/*").with(GuiceContainer.class, initParams);
                        filter("/*").through(NewRelicThrowableFilter.class);
                    }
                });
                install(new JerseyServletModule());
                install(Modules.override(new JerseyNewRelicModule()).with(new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(NewRelicWrapper.class).toInstance(wrapper);
                    }
                }));
                bind(GuiceFilter.class);
                bind(SomeResource.class);
                bind(SampleExceptionMapper.class);

                install(new ConfigModuleBuilder().build());
            }
        });
    }

    private Server getServer(GuiceFilter filter) {
        Server server = new Server(PORT);
        ServletContextHandler servletHandler = new ServletContextHandler();

        servletHandler.addServlet(new ServletHolder(new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
                IOException {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.setContentType("text/plain");
                resp.setContentType("UTF-8");
                resp.getWriter().append("404");
            }
        }), "/*");

        // add guice servlet filter
        servletHandler.addFilter(new FilterHolder(filter), "/*", EnumSet.allOf(DispatcherType.class));

        server.setHandler(servletHandler);

        return server;
    }

    @Path("foo")
    public static class SomeResource {
        @GET
        public String get() {
            return "foo";
        }

        @GET
        @Path("throw")
        public String getThrow() {
            throw new RuntimeException("zomg");
        }

        @GET
        @Path("throwMapped")
        public String getThrowMapped() throws SampleException {
            throw new SampleException("asdf");
        }
    }

    static class StubNewRelicWrapper implements NewRelicWrapper {

        List<Throwable> throwables = new ArrayList<>();
        List<String> names = new ArrayList<>();

        @Override
        public synchronized void noticeError(Throwable t) {
            throwables.add(t);
        }

        @Override
        public synchronized void setTransactionName(@Nullable String category, String transactionName) {
            names.add(category == null ? transactionName : (category + ":" + transactionName));
        }

        synchronized List<String> getNames() {
            return names;
        }

        synchronized List<Throwable> getThrowables() {

            return throwables;
        }
    }

    static class SampleException extends Exception {
        public SampleException(String message) {
            super(message);
        }
    }

    @Provider
    @Singleton
    static class SampleExceptionMapper implements ExceptionMapper<SampleException> {

        @Override
        public javax.ws.rs.core.Response toResponse(SampleException exception) {
            return javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.NOT_ACCEPTABLE).build();
        }
    }
}
