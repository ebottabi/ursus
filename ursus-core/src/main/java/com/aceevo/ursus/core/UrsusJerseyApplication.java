/**

 Copyright 2013 Ray Jenkins ray@memoization.com

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 */
package com.aceevo.ursus.core;

import com.aceevo.ursus.config.UrsusJerseyApplicationConfiguration;
import com.aceevo.ursus.websockets.GrizzlyServerFilter;
import com.aceevo.ursus.websockets.TyrusAddOn;
import com.aceevo.ursus.websockets.UrsusTyrusServerContainer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Service;
import org.glassfish.grizzly.http.CompressionConfig;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpContainer;
import org.glassfish.jersey.server.ContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;
import javax.ws.rs.ext.ExceptionMapper;
import java.lang.reflect.ParameterizedType;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * UrsusJerseyApplication is the base class for our application
 *
 * @param <T> our type of configuration class
 */
public abstract class UrsusJerseyApplication<T extends UrsusJerseyApplicationConfiguration> extends ResourceConfig {

    private final HttpServer httpServer = new HttpServer();
    private Class exceptionMapperClass = UrsusExceptionMapper.class;
    private String configurationFile;
    private T configuration;
    private final Set<Service> managedServices = new HashSet<Service>();
    private final Set<ServerEndpointConfig> serverEndpointConfigs = new HashSet<ServerEndpointConfig>();
    private final UrsusApplicationHelper<T> ursusApplicationHelper = new UrsusApplicationHelper<>();
    private final Class<T> configurationClass;

    private static final Logger LOGGER = LoggerFactory.getLogger(UrsusJerseyApplication.class);

    protected UrsusJerseyApplication(String[] args) {

        // Bridge j.u.l in Grizzly with SLF4J
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        // Use reflection to find our UrsusJerseyApplicationConfiguration Class
        this.configurationClass = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];

        parseArguments(args);
    }

    /**
     * Parse command line arguments for handling
     */
    private void parseArguments(String[] args) {
        if (args == null || args.length == 0)
            return;

        switch (args[0]) {
            case "server":
                if (args.length >= 2) {
                    configurationFile = args[1];
                }

                configurationFile = configurationFile != null ? configurationFile : getClass().getSimpleName().toLowerCase() + ".yml";
                this.configuration = ursusApplicationHelper.parseConfiguration(configurationFile, configurationClass);

                registerInstances(new UrsusApplicationBinder(this.configuration));
                ursusApplicationHelper.configureLogging(configuration);
                boostrap(configuration);
                run(initializeServer());
                break;
            case "db":
                ursusApplicationHelper.handleDbCommand(args, configurationClass);
                break;
            default:
                System.err.printf("Usage: %s <server|db> [args...]%n", getClass().getName());
                System.exit(1);
                break;
        }
    }

    /**
     * Bootstrap method for configuring resources required for this application.
     *
     * @param t an instance of our @{link UrsusJerseyApplicationConfiguration<T>} type
     */
    protected abstract void boostrap(T t);

    /**
     * Hands an UrsusJerseyApplication a fully initialized and configured @{link HttpServer} instance for any additional
     * programmatic configuration user may wish to perform prior to starting.
     *
     * @param httpServer a fully initialized @{link HttpServer} instance with our applications configuration.
     */
    protected abstract void run(HttpServer httpServer);

    /**
     * Provide support for defining a custom {@link ExceptionMapper}
     *
     * @param clazz Class of our custom @{link ExceptionMapper}
     * @param <T>   a type that extends @{link ExceptionMapper}
     */
    protected <T extends ExceptionMapper> void setExceptionMapper(Class<T> clazz) {
        exceptionMapperClass = clazz;
    }

    /**
     * Provide support for registering instances of {@link Service} whose lifecycle
     * will be tied to this Grizzly HTTP Server
     *
     * @param service @{link Service} to register with this Grizzly HTTP Server instance
     */
    protected void register(Service service) {
        managedServices.add(service);
    }

    /**
     * @param serverEndpointConfig
     */
    public void registerEndpoint(ServerEndpointConfig serverEndpointConfig) {
        serverEndpointConfigs.add(serverEndpointConfig);
    }

    /**
     * Register a WebSocket endpoint passing in a path and a {@link Map} of UserProperties
     *
     * @param clazz          the websocket endpoint
     * @param path           the path
     * @param userProperties out user properties.
     */
    public void registerEndpoint(Class<? extends Endpoint> clazz, String path, Map<String, Object> userProperties) {
        ServerEndpointConfig serverEndpointConfig = ServerEndpointConfig.Builder.create(clazz, path).build();
        serverEndpointConfig.getUserProperties().putAll(userProperties);
        registerEndpoint(serverEndpointConfig);
    }

    public void registerEndpoint(Class<? extends Endpoint> clazz, String path, String key, Object userObject) {
        registerEndpoint(clazz, path, ImmutableMap.of(key, userObject));
    }


    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources and UrsusWebSocketApplication defined
     * in this application.
     */
    private HttpServer initializeServer() {

        //Register Jackson support
        registerJacksonSupport();

        //Add ExceptionMapper and create Container
        register(exceptionMapperClass);
        GrizzlyHttpContainer grizzlyHttpContainer = ContainerFactory.createContainer(GrizzlyHttpContainer.class, this);

        // Set our ServerConfiguration options
        final ServerConfiguration config = httpServer.getServerConfiguration();
        config.addHttpHandler(grizzlyHttpContainer, configuration.getHttpServer().getRootContext());
        config.setPassTraceRequest(configuration.getHttpServer().isPassTraceRequest());
        config.setTraceEnabled(configuration.getHttpServer().isTraceEnabled());
        config.setJmxEnabled(configuration.getHttpServer().isJmxEnabled());

        //Configure static resource handler if required
        if (configuration.getHttpServer().getStaticResourceDirectory() != null &&
                configuration.getHttpServer().getStaticResourceContextRoot() != null)
            config.addHttpHandler(new StaticHttpHandler(configuration.getHttpServer().getStaticResourceDirectory()),
                    configuration.getHttpServer().getStaticResourceContextRoot());

        // Now an HttpServer and NetworkListener
        final NetworkListener listener = new NetworkListener("grizzly",
                configuration.getHttpServer().getHost(), configuration.getHttpServer().getPort());

        configureListener(listener);
        configureTyrus(configuration, listener);

        httpServer.addListener(listener);

        return httpServer;
    }

    protected void registerJacksonSupport() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.registerModule(new GuavaModule());

        // create JsonProvider to provide custom ObjectMapper
        JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
        provider.setMapper(mapper);

        register(provider);
    }

    /**
     * Convenience method for starting this Grizzly HttpServer
     *
     * @param httpServer
     */
    protected void startWithShutdownHook(final HttpServer httpServer) {

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                LOGGER.info("Stopping Grizzly HttpServer...");
                httpServer.shutdownNow();
                LOGGER.info("Stopping all managed services...");
                for (Service service : managedServices) {
                    service.stopAsync();
                }
            }
        }, "shutdownHook"));

        try {
            LOGGER.info("Starting all managed services...");
            for (Service service : managedServices) {
                service.startAsync();
            }
            httpServer.start();
            ursusApplicationHelper.printBanner(LOGGER, getClass().getSimpleName());
            LOGGER.info("Press CTRL^C to exit..");
            Thread.currentThread().join();
        } catch (Exception e) {
            LOGGER.error("There was an error while starting Grizzly HTTP server.", e);
        }
    }


    /**
     * Configures the NetworkListener from properties defined in the UrsusJerseyApplicationConfiguration
     *
     * @param listener
     */
    private void configureListener(NetworkListener listener) {

        // Fetch our UrsusJerseyApplicationConfiguration for NetworkListener and configure
        UrsusJerseyApplicationConfiguration.NetworkListener networkListenerConfig = configuration.getHttpServer().getNetworkListener();

        if (networkListenerConfig != null) {
            listener.setAuthPassThroughEnabled(networkListenerConfig.isAuthPassThroughEnabled());
            listener.setMaxFormPostSize(networkListenerConfig.getMaxFormPostSize());
            listener.setMaxBufferedPostSize(networkListenerConfig.getMaxBufferedPostSize());
            listener.setChunkingEnabled(networkListenerConfig.isChunkingEnabled());
            listener.setTransactionTimeout(networkListenerConfig.getTransactionTimeout());
            listener.setMaxHttpHeaderSize(networkListenerConfig.getMaxHttpHeaderSize());

            //KeepAlive
            listener.getKeepAlive().setIdleTimeoutInSeconds(networkListenerConfig.getIdleTimeout());
            listener.getKeepAlive().setMaxRequestsCount(networkListenerConfig.getMaxRequests());

            // Handle SSL Configuration
            if (networkListenerConfig.isSecure()) {
                configureSSLForListener(networkListenerConfig, listener);
            }

            // Handle Compression Configuration
            if (networkListenerConfig.getCompression() != null) {
                configureCompressionForListener(networkListenerConfig, listener);
            }
        }
    }

    private void configureCompressionForListener(UrsusJerseyApplicationConfiguration.NetworkListener networkListenerConfig, NetworkListener listener) {
        UrsusJerseyApplicationConfiguration.Compression compression = networkListenerConfig.getCompression();
        CompressionConfig compressionConfig = listener.getCompressionConfig();

        compressionConfig.setCompressionMode(CompressionConfig.CompressionMode.fromString(compression.getCompressionMode()));
        compressionConfig.setCompressionMinSize(compression.getCompressionMinSize());
        compressionConfig.setCompressableMimeTypes(new HashSet<>(compression.getCompressableMimeTypes()));
        compressionConfig.setNoCompressionUserAgents(new HashSet<>(compression.getNoCompressionUserAgents()));
    }

    private void configureSSLForListener(UrsusJerseyApplicationConfiguration.NetworkListener networkListenerConfig, NetworkListener listener) {

        if (networkListenerConfig.getSslContext() == null) {
            throw new RuntimeException("Ursus Application Configuration error: secure set to true and SSLContext is null");
        }

        SSLContextConfigurator sslContext = new SSLContextConfigurator();
        sslContext.setKeyStoreFile(networkListenerConfig.getSslContext().getKeyStoreFile());
        sslContext.setKeyStorePass(networkListenerConfig.getSslContext().getKeyStorePass());
        sslContext.setTrustStoreFile(networkListenerConfig.getSslContext().getTrustStoreFile());
        sslContext.setTrustStorePass(networkListenerConfig.getSslContext().getTrustStorePass());

        listener.setSecure(true);

        if (networkListenerConfig.getSslEngine() == null) {
            // Use defaults
            listener.setSSLEngineConfig(new SSLEngineConfigurator(sslContext));
        } else {
            UrsusJerseyApplicationConfiguration.SSLEngine sslEngine =
                    networkListenerConfig.getSslEngine();

            SSLEngineConfigurator sslEngineConfigurator = new SSLEngineConfigurator(sslContext);

            sslEngineConfigurator.setEnabledCipherSuites(sslEngine.getEnabledCipherSuites());
            sslEngineConfigurator.setEnabledProtocols(sslEngine.getEnabledProtocols());
            sslEngineConfigurator.setCipherConfigured(sslEngine.isCipherConfigured());
            sslEngineConfigurator.setProtocolConfigured(sslEngine.isProtocolConfigured());
            sslEngineConfigurator.setClientMode(sslEngine.isClientMode());
            sslEngineConfigurator.setNeedClientAuth(sslEngine.isNeedClientAuth());
            sslEngineConfigurator.setWantClientAuth(sslEngine.isWantClientAuth());

            listener.setSSLEngineConfig(sslEngineConfigurator);
        }
    }

    private void configureTyrus(T configuration, NetworkListener listener) {

        Set<Class<?>> classes = getClasses();

        Set<Class<?>> tyrusEndpoints = new HashSet<>();
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(ServerEndpoint.class)) {
                tyrusEndpoints.add(clazz);
            }
        }

        UrsusTyrusServerContainer ursusTyrusServerContainer;
        try {
            ursusTyrusServerContainer = new UrsusTyrusServerContainer(tyrusEndpoints, serverEndpointConfigs,
                    configuration.getHttpServer().getRootContext(), configuration.getTyrus().getIncomingBufferSize());
            GrizzlyServerFilter grizzlyServerFilter = new GrizzlyServerFilter(ursusTyrusServerContainer);
            listener.registerAddOn(new TyrusAddOn(grizzlyServerFilter));
        } catch (DeploymentException e) {
            throw new RuntimeException("Unable to deploy WebSocket endpoints", e);
        }
    }
}
