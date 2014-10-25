#Ursus

<h1 id="overview">Overview</h1>

Ursus is a [Dropwizard](http://dropwizard.codahale.com/) inspired framework for developing lightweight
web services and NIO applications on the JVM and [Grizzly](https://grizzly.java.net/). Ursus combines Grizzly's
NIO/Event Driven framework and popular libraries such as [Jersey 2.7](https://jersey.java.net/) and
[Project Tyrus - JSR 356: Java API for WebSocket - Reference Implementation](https://tyrus.java.net/) with
the simple conventions found in Dropwizard, allowing you to focus on developing your services rather
than managing their accompanying dependencies, reducing time-to-market and maintenance.

<h2 id="grizzly">Grizzly HTTP Container</h2>

Grizzly is a multi-protocol framework built on top of Java NIO. Grizzly aims to simplify development of
robust and scalable servers. Jersey provides a container extension module that enables support for using
Grizzly as a plain vanilla HTTP container that runs JAX-RS applications. Starting a Grizzly server to
run a JAX-RS or Jersey application is one of the most lightweight and easy ways how to expose a
functional RESTful services application.

<h2 id="jersey">REST / Jersey</h2>

Jersey is an open source, production quality, framework for developing RESTful Web Services in Java that
provides support for JAX-RS APIs and serves as a JAX-RS (JSR 311 & JSR 339) Reference Implementation.
Developing RESTful Web services with Jersey is a snap and the latest Jersey implementation included in
Ursus provides support for both asynchronous clients and server side services with [AsyncResponse](https://jax-rs-spec.java.net/nonav/2.0/apidocs/javax/ws/rs/container/AsyncResponse.html)

<h2 id="tyrus">WebSockets / Tyrus</h2>

Tyrus is the open source [JSR 356](https://java.net/projects/websocket-spec) Java reference implementation of
WebSockets. The WebSocket protocol provides full-duplex communications between server and remote hosts.
WebSocket is designed to be implemented in web browsers and web servers, but it can be used by any client or server application.
The WebSocket protocol makes possible more interaction between a browser and a web site, facilitating live content
and the creation of real-time applications and services.

<h2 id="additional libraries">Additional Libraries</h2>

Ursus also includes many libraries found in Dropwizard and other frameworks to help speed up development including,

* [Guava](https://code.google.com/p/guava-libraries/)
* [Joda time](www.joda.org/joda-time/)
* [Hibernate Validator](http://hibernate.org/validator/)
* [Jackson](http://jackson.codehaus.org/) for YAML and JSON support
* [Logback](http://logback.qos.ch/) and [SLF4J](http://www.slf4j.org/) for bridging of Grizzly's java.util.logging
* [Tomcat JDBC Connection Pool](https://people.apache.org/~fhanik/jdbc-pool/jdbc-pool.html) for JDBC connection pooling.
* [Liquibase](http://www.liquibase.org/) for Database refactoring.
* An [Apache HTTPClient](http://hc.apache.org/httpclient-3.x/) and a [Jersey 2.7 client](https://jersey.java.net/documentation/latest/user-guide.html#client)

<h2 id="using">Using Ursus</h2>
Ursus releases artifacts are hosted on the central repository, to get started with Ursus simply add the ```ursus-core```
module to your pom.xml as a dependency.

```xml
 <dependencies>
    <dependency>
        <groupId>com.aceevo.ursus</groupId>
        <artifactId>ursus-core</artifactId>
        <version>0.2.15</version>
    </dependency>
 </dependencies>
```

<h1 id="jersey quickstart">Jersey Application Quick Start</h1>
Let's take a look at the ```ursus-example-application``` project a simple HelloWorld service. It's fully implemented, but we're going
to walk through the steps it took to build this project. If it helps you might want to clone the Ursus repro, [https://github.com/rjenkins/ursus.git](https://github.com/rjenkins/ursus.git)
and modify ```ursus-example-application``` as we progress through the quick start building up the application yourself.

Ursus Jersey applications require 2 classes, a subclass of ```UrsusJerseyApplication``` and a subclass of ```UrsusJerseyApplicationConfiguration```. The com.aceevo.ursus.example package contains both.
Let's start by looking at the ```ExampleApplicationConfiguration``` class.

```java
package com.aceevo.ursus.example;

import com.aceevo.ursus.config.UrsusJerseyApplicationConfiguration;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class ExampleApplicationConfiguration extends UrsusJerseyApplicationConfiguration {

    @NotEmpty
    @JsonProperty
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
```
Just like Dropwizard we will specify our environment specific configuration parameters in a [YAML](http://www.yaml.org/) configuration
file and these parameters will be deserialized by Jackson and validated. By default Ursus will look for a file whose name is the class name of
your application with a ```.yml``` extension, for this example application that file is named ```exampleapplication.yml```. If you wish to specify
a different location for you configuration file you can pass that as a command line argument.

```java -jar <path to jar file> server <path to your YAML file>```

In addition to your own environment specific configuration parameters [UrsusJerseyApplicationConfiguration](https://github.com/rjenkins/ursus/blob/master/ursus-config/src/main/java/com/aceevo/ursus/config/UrsusJerseyApplicationConfiguration.java)
defines a large set of configuration properties that allow you to modify all of the granular configuration options available with Grizzly and many of the other
included libraries simply by adding lines of YAML (more on that later).

<h3 id="jersey creating yaml">Creating our YAML File</h3>

Let's allow our ExampleApplication the ability to specify who it should say Hello to. For this we'll use the name property we created in
```ExampleApplicationConfiguration```. Now we can define this parameter in ```exampleapplication.yml```

```yaml
httpServer:
  host: localhost
name: Ray
```

Great! Let's move on to creating our first application.

<h3 id="creating ursusjerseyapplication">A First UrsusJerseyApplication</h3>

We'll name our application ```ExampleApplication``` and subclass ```UrsusJerseyApplication``` we also need to parameterize
ExampleApplication with the type of our ```UrsusJerseyApplicationConfiguration``` class, that's named ```ExampleApplicationConfiguration```.
If you're using an IDE like IntelliJ you can select implement methods and you'll get the skeleton of our application. We also need to add a ```public static void main(String[] args)``` method and instantiate an instance of ExampleApplication.

```java
package com.aceevo.ursus.example;

import com.aceevo.ursus.core.UrsusJerseyApplication;
import org.glassfish.grizzly.http.server.HttpServer;

public class ExampleApplication extends UrsusJerseyApplication<ExampleApplicationConfiguration> {

    protected ExampleApplication(String[] args) {
        super(args);
    }

    public static void main(String[] args) throws Exception {
        new ExampleApplication(args);
    }

    @Override
    protected void boostrap(ExampleApplicationConfiguration exampleApplicationConfiguration) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void run(HttpServer httpServer) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
```

ExampleApplication in the ```ursus-example-application``` project is filled out and has more data than this, but let's ignore that for now.
If you like you can always overwrite ```ExampleApplication``` with the above and follow along.

<h3 id="setting up logging">Setting up Logging</h3>

Ursus includes [Logback](http://logback.qos.ch/) for logging, we need to setup a ```logback.xml``` file for our project and because
we're using maven we'll put this file in ```src/main/resources```. Our logback.xml should include an appender named ```FILE```.

```xml
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <outputPatternAsHeader>false</outputPatternAsHeader>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    <appender name="FILE" class="ch.qos.logback.core.FileAppender">
        <file>./ursus-example-application.log</file>
        <encoder>
            <outputPatternAsHeader>false</outputPatternAsHeader>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    <root level="INFO">
        <appender-ref ref="FILE" />
        <appender-ref ref="STDOUT"/>
    </root>
    <contextListener class="ch.qos.logback.classic.jul.LevelChangePropagator">
        <resetJUL>true</resetJUL>
    </contextListener>
</configuration>
```

Now we can override logging parameters like level and fileName in our exampleapplication.yml

```yaml
httpServer:
  host: localhost
logging:
  level: INFO
  fileName: ray.out
name: Ray
```


<h3 id="building first artifact">Building our First Artifact.</h3>

Like Dropwizard Ursus recommends building a "fat" jar that contains all the ```.class``` files needed to run our service.
Here's the relevant parts of our configuration.

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>2.1</version>
            <configuration>
                <createDependencyReducedPom>true</createDependencyReducedPom>
                <filters>
                    <filter>
                        <artifact>*:*</artifact>
                        <excludes>
                            <exclude>META-INF/*.SF</exclude>
                            <exclude>META-INF/*.DSA</exclude>
                            <exclude>META-INF/*.RSA</exclude>
                        </excludes>
                    </filter>
                </filters>
            </configuration>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                    <configuration>
                        <transformers>
                            <transformer implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer" />
                            <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                <mainClass>com.aceevo.ursus.example.ExampleApplication</mainClass>
                            </transformer>
                        </transformers>
                    </configuration>
                </execution>
            </executions>
        </plugin>
            ...
```

If you've cloned the ursus git repo and have modified the ```ursus-example-application``` you should be able to build and test
the project by running ```mvn -q clean compile -P run-ursus-example-application``` from the ursus directory.

Unfortunately our application starts and then quickly exits, what's up?

```
$ mvn -q compile -P run-ursus-example-application
19:16:17.488 [main] INFO  o.h.validator.internal.util.Version - HV000001: Hibernate Validator 5.0.1.Final
19:16:18.009 [main] INFO  o.g.jersey.server.ApplicationHandler - Initiating Jersey application, version Jersey: 2.5 2013-12-18 14:27:29...
$
```

<h3 id="starting http">Starting the HttpServer</h3>

We need to start our HttpServer instance to run our application. ```protected void run(HttpServer httpServer)``` method passes us
a configured HttpServer (after bootstrap has been called) allowing us to make any additional modifications to the Grizzly Http Server before
we start our application. When we're ready to start our application Ursus provides a helper method ```protected void startWithShutdownHook(final HttpServer httpServer)```
that handles starting the HttpServer and registering a shutdown hook for our application.

```java
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
            httpServer.stop();
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
        printBanner(getClass().getSimpleName());
        LOGGER.info("Press CTRL^C to exit..");
        Thread.currentThread().join();
    } catch (Exception e) {
        LOGGER.error("There was an error while starting Grizzly HTTP server.", e);
    }
}
```

Let's add that to our ExampleApplication run method, build again and retry.

```java
...

@Override
protected void run(HttpServer httpServer) {
    startWithShutdownHook(httpServer);
}

...
```

```
$ mvn -q compile -P run-ursus-example-application
19:32:04.643 [main] INFO  o.h.validator.internal.util.Version - HV000001: Hibernate Validator 5.0.1.Final
19:32:05.150 [main] INFO  o.g.jersey.server.ApplicationHandler - Initiating Jersey application, version Jersey: 2.5 2013-12-18 14:27:29...
19:32:05.504 [main] INFO  c.aceevo.ursus.core.UrsusJerseyApplication - Starting all managed services...
19:32:05.557 [main] INFO  o.g.g.http.server.NetworkListener - Started listener bound to [localhost:8080]
19:32:05.561 [main] INFO  o.g.grizzly.http.server.HttpServer - [HttpServer] Started.
19:32:05.570 [main] INFO  c.aceevo.ursus.core.UrsusJerseyApplication - Starting ExampleApplication
 __  __   ______    ______   __  __   ______
/_/\/_/\ /_____/\  /_____/\ /_/\/_/\ /_____/\
\:\ \:\ \\:::_ \ \ \::::_\/_\:\ \:\ \\::::_\/_
 \:\ \:\ \\:(_) ) )_\:\/___/\\:\ \:\ \\:\/___/\
  \:\ \:\ \\: __ `\ \\_::._\:\\:\ \:\ \\_::._\:\
   \:\_\:\ \\ \ `\ \ \ /____\:\\:\_\:\ \ /____\:\
    \_____\/ \_\/ \_\/ \_____\/ \_____\/ \_____\/

19:32:05.570 [main] INFO  c.aceevo.ursus.core.UrsusJerseyApplication - Press CTRL^C to exit..
```

Success! Now it's time to move on to implementing our service.

<h3 id="representations and resources">Representations and Resources</h3>
We've got our application started but we need to provide some resources and some representations. As previously mentioned
this Hello World application is going to support specifying exactly who we're going to say hello to. Let's create a model
class to represent a Hello.

```java
package com.aceevo.ursus.example.model;

import org.codehaus.jackson.annotate.JsonProperty;

public class Hello {

    @JsonProperty
    private String name;

    public Hello() {

    }

    public Hello(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }
}
```

Now that we've created a representation we can create a Jersey Resource class for serving this representation.

<h3 id="creating resources">Creating Resources</h3>

Let's create a simple Jersey resource that serves our Hello representation. In this Resource we'll add support for the
[GET](http://tools.ietf.org/search/rfc2616#section-9.3) and [POST](http://tools.ietf.org/search/rfc2616#section-9.5) methods as well as support for
an [AsyncResponse](https://jersey.java.net/apidocs/latest/jersey/javax/ws/rs/container/AsyncResponse.html) GET.

```java
package com.aceevo.ursus.example.api;

import com.aceevo.ursus.example.ExampleApplicationConfiguration;
import com.aceevo.ursus.example.model.Hello;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Path("hello")
@Produces(MediaType.APPLICATION_JSON)
public class HelloWorldResource {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Inject
    ExampleApplicationConfiguration exampleApplicationConfiguration;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response hello() {
        return Response.ok(new Hello(exampleApplicationConfiguration.getName())).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response createHello(@Valid Hello hello, @Context UriInfo uriInfo) {
        URI uri = UriBuilder.fromUri(uriInfo.getRequestUri()).path(hello.getName()).build();
        return Response.created(uri).build();
    }

    @GET
    @Path("/async")
    @Produces(MediaType.APPLICATION_JSON)
    public void sayHelloAsync(final @Suspended AsyncResponse asyncResponse) throws Exception {

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.currentThread().sleep(5000);
                    asyncResponse.resume(Response.ok(new Hello(exampleApplicationConfiguration.getName())).build());
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        });

    }
}
```

There's a few things going on in this resource, so let's walk through them step by step.

* We define the path and media type at the class level with ```@Path("hello") and @Produces(MediaType.APPLICATION_JSON)```
* We create an ExecutorService for our AsyncResponse resource with ```java private final ExecutorService executorService = Executors.newSingleThreadExecutor();```
* We @Inject an instance of  ```ExampleApplicationConfiguration```

The first ```@GET``` and ```@POST``` methods are pretty standard, let's take a look at the ```@PATH("/async") sayHelloAsync``` resource method. This method
uses the ```@Suspend``` annotation and allows us to suspend the current request releasing the thread responsible for handling the request and resume
asynchronously once we're ready to response.

<h3 id="registering resources">Registering Resources</h3>

Now that we've created our first resource we need to register them with our Jersey ResourceConfig, there are many ways to do this (more on this later, you can also
register an instance of a Resource class with ```register(Object components)```). For our example application let's use Jersey's built-in resource discovery and
specify the package where our resources exist. We'll do this in the bootstrap method of ```ExampleApplication```.

```java
package com.aceevo.ursus.example;

import com.aceevo.ursus.core.UrsusJerseyApplication;
import com.aceevo.ursus.example.api.EchoEndpointResource;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.glassfish.grizzly.http.server.HttpServer;

import javax.websocket.server.ServerEndpointConfig;

public class ExampleApplication extends UrsusJerseyApplication<ExampleApplicationConfiguration> {

    ...

    @Override
    protected void boostrap(ExampleApplicationConfiguration exampleApplicationConfiguration) {
        packages("com.aceevo.ursus.example.api");
    }

    @Override
    protected void run(HttpServer httpServer) {
        startWithShutdownHook(httpServer);
    }
}
```
Now we're ready to build and try out our resources.

```
$ mvn -q compile -P run-ursus-example-application
21:27:58.901 [main] INFO  o.h.validator.internal.util.Version - HV000001: Hibernate Validator 5.0.1.Final
21:27:59.901 [main] INFO  o.g.jersey.server.ApplicationHandler - Initiating Jersey application, version Jersey: 2.5 2013-12-18 14:27:29...
21:28:00.290 [main] INFO  c.aceevo.ursus.core.UrsusJerseyApplication - Starting all managed services...
21:28:00.342 [main] INFO  o.g.g.http.server.NetworkListener - Started listener bound to [localhost:8080]
21:28:00.357 [main] INFO  o.g.grizzly.http.server.HttpServer - [HttpServer] Started.
21:28:00.366 [main] INFO  c.aceevo.ursus.core.UrsusJerseyApplication - Starting ExampleApplication
 __  __   ______    ______   __  __   ______
/_/\/_/\ /_____/\  /_____/\ /_/\/_/\ /_____/\
\:\ \:\ \\:::_ \ \ \::::_\/_\:\ \:\ \\::::_\/_
 \:\ \:\ \\:(_) ) )_\:\/___/\\:\ \:\ \\:\/___/\
  \:\ \:\ \\: __ `\ \\_::._\:\\:\ \:\ \\_::._\:\
   \:\_\:\ \\ \ `\ \ \ /____\:\\:\_\:\ \ /____\:\
    \_____\/ \_\/ \_\/ \_____\/ \_____\/ \_____\/

21:28:00.366 [main] INFO  c.aceevo.ursus.core.UrsusJerseyApplication - Press CTRL^C to exit..
^Z
[1]+  Stopped                 mvn -q compile -P run-ursus-example-application
$ bg
[1]+ mvn -q compile -P run-ursus-example-application &
```
```
$ curl -v -X GET -H "Accept: application/json" http://localhost:8080/hello
* About to connect() to localhost port 8080 (#0)
*   Trying ::1...
* Connection refused
*   Trying 127.0.0.1...
* connected
* Connected to localhost (127.0.0.1) port 8080 (#0)
> GET /hello HTTP/1.1
> User-Agent: curl/7.24.0 (x86_64-apple-darwin12.0) libcurl/7.24.0 OpenSSL/0.9.8y zlib/1.2.5
> Host: localhost:8080
> Accept: application/json
>
< HTTP/1.1 200 OK
< Content-Type: application/json
< Date: Thu, 16 Jan 2014 05:28:07 GMT
< Content-Length: 14
<
* Connection #0 to host localhost left intact
{"name":"Ray"}* Closing connection #0
```

```
$ curl -v -H "Content-Type: application/json" -X POST http://localhost:8080/hello -d '{ "name" : "Andrea" }'
* About to connect() to localhost port 8080 (#0)
*   Trying ::1...
* Connection refused
*   Trying 127.0.0.1...
* connected
* Connected to localhost (127.0.0.1) port 8080 (#0)
> POST /hello HTTP/1.1
> User-Agent: curl/7.24.0 (x86_64-apple-darwin12.0) libcurl/7.24.0 OpenSSL/0.9.8y zlib/1.2.5
> Host: localhost:8080
> Accept: */*
> Content-Type: application/json
> Content-Length: 21
>
* upload completely sent off: 21 out of 21 bytes
< HTTP/1.1 201 Created
< Location: http://localhost:8080/hello/Andrea
< Date: Thu, 16 Jan 2014 05:28:11 GMT
< Content-Length: 0
<
* Connection #0 to host localhost left intact
* Closing connection #0
```

```
$ curl -v -X GET -H "Accept: application/json" http://localhost:8080/hello/async
* About to connect() to localhost port 8080 (#0)
*   Trying ::1...
* Connection refused
*   Trying 127.0.0.1...
* connected
* Connected to localhost (127.0.0.1) port 8080 (#0)
> GET /hello/async HTTP/1.1
> User-Agent: curl/7.24.0 (x86_64-apple-darwin12.0) libcurl/7.24.0 OpenSSL/0.9.8y zlib/1.2.5
> Host: localhost:8080
> Accept: application/json
>
< HTTP/1.1 200 OK
< Content-Type: application/json
< Date: Thu, 16 Jan 2014 05:28:53 GMT
< Content-Length: 14
<
* Connection #0 to host localhost left intact
{"name":"Ray"}* Closing connection #0
```

Ursus supports full content negotiation via Jersey, so if your resource class produces ```application/json``` but the client accepts ```text/plain```,
Jersey will reply with a ```406 Not Acceptable```.

```
$ curl -v -H "Accept: text/plain" -X GET http://localhost:8080/hello
* About to connect() to localhost port 8080 (#0)
*   Trying ::1...
* Connection refused
*   Trying 127.0.0.1...
* connected
* Connected to localhost (127.0.0.1) port 8080 (#0)
> GET /hello HTTP/1.1
> User-Agent: curl/7.24.0 (x86_64-apple-darwin12.0) libcurl/7.24.0 OpenSSL/0.9.8y zlib/1.2.5
> Host: localhost:8080
> Accept: vext/plain
>
< HTTP/1.1 406 Not Acceptable
< Content-Type: text/plain
< Date: Thu, 16 Jan 2014 07:56:36 GMT
< Content-Length: 2122
<
javax.ws.rs.NotAcceptableException: HTTP 406 Not Acceptable
	at org.glassfish.jersey.server.internal.routing.MethodSelectingRouter.getMethodRouter(MethodSelectingRouter.java:573)
	at org.glassfish.jersey.server.internal.routing.MethodSelectingRouter.access$400(MethodSelectingRouter.java:97)
	at org.glassfish.jersey.server.internal.routing.MethodSelectingRouter$4.apply(MethodSelectingRouter.java:813)
	at org.glassfish.jersey.server.internal.routing.MethodSelectingRouter.apply(MethodSelectingRouter.java:420)
	at org.glassfish.jersey.server.internal.routing.RoutingStage._apply(RoutingStage.java:128)
	at org.glassfish.jersey.server.internal.routing.RoutingStage._apply(RoutingStage.java:131)
	at org.glassfish.jersey.server.internal.routing.RoutingStage._apply(RoutingStage.java:131)
	at org.glassfish.jersey.server.internal.routing.RoutingStage.apply(RoutingStage.java:110)
	at org.glassfish.jersey.server.internal.routing.RoutingStage.apply(RoutingStage.java:65)
	at org.glassfish.jersey.process.internal.Stages.process(Stages.java:197)
	at org.glassfish.jersey.server.ServerRuntime$1.run(ServerRuntime.java:250)
	at org.glassfish.jersey.internal.Errors$1.call(Errors.java:271)
	at org.glassfish.jersey.internal.Errors$1.call(Errors.java:267)
	at org.glassfish.jersey.internal.Errors.process(Errors.java:315)
	at org.glassfish.jersey.internal.Errors.process(Errors.java:297)
	at org.glassfish.jersey.internal.Errors.process(Errors.java:267)
	at org.glassfish.jersey.process.internal.RequestScope.runInScope(RequestScope.java:318)
	at org.glassfish.jersey.server.ServerRuntime.process(ServerRuntime.java:236)
	at org.glassfish.jersey.server.ApplicationHandler.handle(ApplicationHandler.java:1010)
	at org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpContainer.service(GrizzlyHttpContainer.java:363)
	at org.glassfish.grizzly.http.server.HttpHandler$1.run(HttpHandler.java:217)
	at org.glassfish.grizzly.threadpool.AbstractThreadPool$Worker.doWork(AbstractThreadPool.java:565)
	at org.glassfish.grizzly.threadpool.AbstractThreadPool$Worker.run(AbstractThreadPool.java:545)
	at java.lang.Thread.run(Thread.java:744)
* Connection #0 to host localhost left intact
* Closing connection #0
```

<h3 id="resource tests">Writing Resource Tests</h3>

Jersey comes with a testframework provider for Grizzly, let's add that to our pom.xml so we can write some tests.

```xml
<dependency>
    <groupId>org.glassfish.jersey.test-framework.providers</groupId>
    <artifactId>jersey-test-framework-provider-grizzly2</artifactId>
    <version>2.5</version>
</dependency>
<dependency>
    <groupId>org.glassfish.jersey.test-framework</groupId>
    <artifactId>jersey-test-framework-core</artifactId>
    <version>2.5</version>
    <exclusions>
        <exclusion>
            <groupId>com.google.guava</groupId>
            <artifactId>guava</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

We can extend JerseyTest and override the configure method to create a new [Jersey test environment](https://jersey.java.net/documentation/latest/test-framework.html). We'll
create a dummy ```ExampleApplicationConfiguration``` class and register an UrsusApplicationBinder to support injection
of our configuration as well as register our HelloWordResource class.

```java
package com.aceevo.ursus.example.api;

import com.aceevo.ursus.core.UrsusApplicationBinder;
import com.aceevo.ursus.example.ExampleApplicationConfiguration;
import com.aceevo.ursus.example.model.Hello;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static junit.framework.Assert.assertEquals;

public class HelloWorldTest extends JerseyTest {

    @Override
    protected Application configure() {
        ResourceConfig resourceConfig = new ResourceConfig();
        ExampleApplicationConfiguration exampleApplicationConfiguration = new ExampleApplicationConfiguration();
        exampleApplicationConfiguration.setName("Ray");
        resourceConfig.registerInstances(new UrsusApplicationBinder(exampleApplicationConfiguration));
        resourceConfig.register(HelloWorldResource.class);
        return resourceConfig;
    }

    @Test
    public void helloTest() {
        Response response =  target("/hello").request().get();
        assertEquals(200, response.getStatus());
        assertEquals("Ray", response.readEntity(Hello.class).getName());
    }

    @Test
    public void sayHello() {
        Entity<Hello> helloEntity = Entity.entity(new Hello("Bob"), MediaType.APPLICATION_JSON_TYPE);
        Response response = target("/hello").request().post(helloEntity);
        assertEquals(201, response.getStatus());
        assertEquals("http://localhost:9998/hello/Bob", response.getLocation().toString());
    }

    @Test
    public void helloAsyncTest() throws Exception {
        Response response = target("/hello").request().async().get().get();
        assertEquals(200, response.getStatus());
        assertEquals("Ray", response.readEntity(Hello.class).getName());
    }
}
```

Now let's run ```mvn test``` and verify our resource class works as intended

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running com.aceevo.ursus.example.api.HelloWorldTest
Jan 19, 2014 8:36:39 PM org.glassfish.jersey.server.ApplicationHandler initialize
INFO: Initiating Jersey application, version Jersey: 2.5 2013-12-18 14:27:29...
Jan 19, 2014 8:36:40 PM org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory$GrizzlyTestContainer start
INFO: Starting GrizzlyTestContainer...
Jan 19, 2014 8:36:40 PM org.glassfish.grizzly.http.server.NetworkListener start
INFO: Started listener bound to [localhost:9998]
Jan 19, 2014 8:36:40 PM org.glassfish.grizzly.http.server.HttpServer start
INFO: [HttpServer] Started.
Jan 19, 2014 8:36:40 PM org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory$GrizzlyTestContainer stop
INFO: Stopping GrizzlyTestContainer...
Jan 19, 2014 8:36:40 PM org.glassfish.grizzly.http.server.NetworkListener shutdownNow
INFO: Stopped listener bound to [localhost:9998]
Jan 19, 2014 8:36:40 PM org.glassfish.jersey.server.ApplicationHandler initialize
INFO: Initiating Jersey application, version Jersey: 2.5 2013-12-18 14:27:29...
Jan 19, 2014 8:36:40 PM org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory$GrizzlyTestContainer start
INFO: Starting GrizzlyTestContainer...
Jan 19, 2014 8:36:40 PM org.glassfish.grizzly.http.server.NetworkListener start
INFO: Started listener bound to [localhost:9998]
Jan 19, 2014 8:36:40 PM org.glassfish.grizzly.http.server.HttpServer start
INFO: [HttpServer-1] Started.
Jan 19, 2014 8:36:40 PM org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory$GrizzlyTestContainer stop
INFO: Stopping GrizzlyTestContainer...
Jan 19, 2014 8:36:40 PM org.glassfish.grizzly.http.server.NetworkListener shutdownNow
INFO: Stopped listener bound to [localhost:9998]
Jan 19, 2014 8:36:40 PM org.glassfish.jersey.server.ApplicationHandler initialize
INFO: Initiating Jersey application, version Jersey: 2.5 2013-12-18 14:27:29...
Jan 19, 2014 8:36:40 PM org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory$GrizzlyTestContainer start
INFO: Starting GrizzlyTestContainer...
Jan 19, 2014 8:36:40 PM org.glassfish.grizzly.http.server.NetworkListener start
INFO: Started listener bound to [localhost:9998]
Jan 19, 2014 8:36:40 PM org.glassfish.grizzly.http.server.HttpServer start
INFO: [HttpServer-2] Started.
Jan 19, 2014 8:36:40 PM org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory$GrizzlyTestContainer stop
INFO: Stopping GrizzlyTestContainer...
Jan 19, 2014 8:36:40 PM org.glassfish.grizzly.http.server.NetworkListener shutdownNow
INFO: Stopped listener bound to [localhost:9998]
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.628 sec

Results :

Tests run: 3, Failures: 0, Errors: 0, Skipped: 0

[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 5.103s
[INFO] Finished at: Sun Jan 19 20:36:40 CST 2014
[INFO] Final Memory: 17M/439M
[INFO] ------------------------------------------------------------------------
```

<h3 id="static assets">Serving Static Assets</h3>

With Ursus you can serve static assets such as HTML or Javascript files. Simply add the staticResourceDirectory and staticResourceContextRoot options
to the ```httpServer``` section of the ```exampleapplication.yml``` file.

```yaml
httpServer:
  host: localhost
  staticResourceDirectory: assets
  staticResourceContextRoot: /ursus
logging:
  level: INFO
name: Ray
```

If we add an index.html file to the ```assets``` directory of our application.

```html
<!DOCTYPE html>
<html>
<head></head>
<body>
<p>Hello from Ursus!</p>
</body>
</html>
```

We can access static resources from the browser.

```
$ curl -X GET http://localhost:8080/ursus/index.html
<!DOCTYPE html>
<html>
<head></head>
<body>
<p>Hello from Ursus!</p>
</body>
$
```

<h3 id="websocket">WebSocket EndPoints</h3>

In addition to Jersey, Ursus includes [Tyrus](https://tyrus.java.net/) the open source JSR 356 - Java API for WebSocket
reference implementation for easy development of WebSocket applications. You can create Annotated EndPoints and register
them within your Ursus application or create them programatically. If you need to pass dependencies to EndPoints you'll
need to do that programmatically and make use of the EndPoint's [UserProperties](http://docs.oracle.com/javaee/7/api/javax/websocket/EndpointConfig.html#getUserProperties)

Let's create a simple programmatic EndPoint for ```ursus-example-application```. We'll pass in our ```ExampleApplicationConfiguration```
in the onOpen method. Now we can use our ```name``` property to respond to clients.

```java
package com.aceevo.ursus.example.api;

import com.aceevo.ursus.example.ExampleApplicationConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import java.io.IOException;

public class EchoEndpointResource extends Endpoint {

    private final Logger logger = LoggerFactory.getLogger(EchoEndpointResource.class);
    private ExampleApplicationConfiguration exampleApplicationConfiguration;

    @OnOpen
    public void onOpen(final Session session, EndpointConfig config) {
        exampleApplicationConfiguration = (ExampleApplicationConfiguration) config.getUserProperties().get("exampleApplicationConfiguration");
        session.addMessageHandler(new MessageHandler.Whole<String>() {

            @Override
            public void onMessage(String message) {
                logger.info("Received message from client: " + message);
                try {
                    session.getBasicRemote().sendText("Hello " + exampleApplicationConfiguration.getName());
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        });
    }
}
```

<h3 id="registering endpoints">Registering EndPoints</h3>

Ursus provides some short cuts that allow you to quickly register your endpoints and pass any dependencies to your endpoint.
Let's modify our bootstrap method and use the ```registerEndPoint``` method to add ```EchoEndPointResouce```. We simply need
to pass our endpoint class, the path, and a key for our UserProperties, that key will be "exampleApplicationConfiguration"
and the value is the instance of our ```ExampleApplicationConfiguration``` class.

```java
public class ExampleApplication extends UrsusJerseyApplication<ExampleApplicationConfiguration> {

    ...

    @Override
    protected void boostrap(ExampleApplicationConfiguration exampleApplicationConfiguration) {
        packages("com.aceevo.ursus.example.api");
        registerEndpoint(EchoEndpointResource.class, "/echo", "exampleApplicationConfiguration", exampleApplicationConfiguration);
    }

    @Override
    protected void run(HttpServer httpServer) {
        startWithShutdownHook(httpServer);
    }
}
```

<h3 id="tyrus annotated">Tyrus Annotated EndPoints</h3>

Tyrus also provides support for annotated EndPoints using the ```@OnOpen``` ```@OnClose``` ```@OnError``` ```@OnMessage```. You
can't pass arguments into these endpoints as the lifecycle of these resources are controlled by Tyrus (though you can control that
by overriding a class, more on that later). Let's create an annotation based ```EndPoint``` and register it with our application.

```java

package com.aceevo.ursus.example.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/annotatedEcho")
public class AnnotatedEndPointResource {

    private final Logger LOGGER = LoggerFactory.getLogger(AnnotatedEndPointResource.class);

    @OnMessage
    public String sayHello(String message, Session session) {
        LOGGER.info("Received message from client: " + message);
        return message;
    }
}

```

```java

public class ExampleApplication extends UrsusJerseyApplication<ExampleApplicationConfiguration> {

   ...

    @Override
    protected void boostrap(ExampleApplicationConfiguration exampleApplicationConfiguration) {
        packages("com.aceevo.ursus.example.api");
        register(AnnotatedEndPointResource.class);
        registerEndpoint(EchoEndpointResource.class, "/echo", "exampleApplicationConfiguration", exampleApplicationConfiguration);
    }

    @Override
    protected void run(HttpServer httpServer) {
        startWithShutdownHook(httpServer);
    }
}
```

<h3 id="websocket client">Creating WebSocket Clients</h3>

The ```ursus-example-websocket-client``` project provides an example of how to create a Java based WebSocket client. Our client
accepts a command line argument, if the argument ```annotated``` is passed we'll call the annotation based EndPoint, otherwise
we'll call the programmatically created ```EchoEndpointResource```.

```java

package com.aceevo.ursus.example.websocketclient;

import org.glassfish.tyrus.client.ClientManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SimpleWebSocketClient {
    private CountDownLatch messageLatch;
    private static final String SENT_MESSAGE = "Hello WebSocket";

    final Logger logger = LoggerFactory.getLogger(SimpleWebSocketClient.class);

    public SimpleWebSocketClient() {

        // Bridge j.u.l in Grizzly with SLF4J
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        logger.info("starting");
    }

    public static void main(String[] args) {
        SimpleWebSocketClient simpleWebSocketClient = new SimpleWebSocketClient();

        if (args.length > 0 && "annotated".equals(args[0]))
            simpleWebSocketClient.run("ws://localhost:8080/annotatedEcho");
        else
            simpleWebSocketClient.run("ws://localhost:8080/echo");
    }

    public void run(String uri) {
        try {
            messageLatch = new CountDownLatch(1);

            final ClientEndpointConfig cec = ClientEndpointConfig.Builder.create().build();
            ClientManager client = ClientManager.createClient();
            client.connectToServer(new Endpoint() {

                @Override
                public void onOpen(final Session session, EndpointConfig config) {
                    try {
                        session.addMessageHandler(new MessageHandler.Whole<String>() {
                            @Override
                            public void onMessage(String message) {
                                logger.info("Server replied with : " + message);
                                try {
                                    session.close();
                                    messageLatch.countDown();
                                } catch (IOException ex) {
                                    logger.debug(ex.getMessage(), ex);
                                }

                            }
                        });
                        session.getBasicRemote().sendText(SENT_MESSAGE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, cec, new URI(uri));
            messageLatch.await(100, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

<h3 id="testing websocket client">Testing WebSocket Clients</h3>

Now that we've created a WebSocket Endpoint we can test our WebSocket Client. Let's start ExampleApplication with the following
command ```mvn -q compile -P run-ursus-example-application```

```
$ mvn -q compile -P run-ursus-example-application
21:01:39.778 [main] INFO  o.h.validator.internal.util.Version - HV000001: Hibernate Validator 5.0.1.Final
21:01:40.711 [main] INFO  o.g.jersey.server.ApplicationHandler - Initiating Jersey application, version Jersey: 2.5 2013-12-18 14:27:29...
21:01:40.815 [main] WARN  o.g.jersey.internal.inject.Providers - A provider com.aceevo.ursus.example.api.AnnotatedEndPointResource registered in SERVER runtime does not implement any provider interfaces applicable in the SERVER runtime. Due to constraint configuration problems the provider com.aceevo.ursus.example.api.AnnotatedEndPointResource will be ignored.
21:01:41.110 [main] INFO  c.a.u.core.UrsusJerseyApplication - Starting all managed services...
21:01:41.162 [main] INFO  o.g.g.http.server.NetworkListener - Started listener bound to [localhost:8080]
21:01:41.166 [main] INFO  o.g.grizzly.http.server.HttpServer - [HttpServer] Started.
21:01:41.174 [main] INFO  c.a.u.core.UrsusJerseyApplication - Starting ExampleApplication
 __  __   ______    ______   __  __   ______
/_/\/_/\ /_____/\  /_____/\ /_/\/_/\ /_____/\
\:\ \:\ \\:::_ \ \ \::::_\/_\:\ \:\ \\::::_\/_
 \:\ \:\ \\:(_) ) )_\:\/___/\\:\ \:\ \\:\/___/\
  \:\ \:\ \\: __ `\ \\_::._\:\\:\ \:\ \\_::._\:\
   \:\_\:\ \\ \ `\ \ \ /____\:\\:\_\:\ \ /____\:\
    \_____\/ \_\/ \_\/ \_____\/ \_____\/ \_____\/

21:01:41.174 [main] INFO  c.a.u.core.UrsusJerseyApplication - Press CTRL^C to exit..
```

Now we can launch our client ```mvn -q compile -P run-ursus-example-websocket-client```

```
21:02:39.943 [main] INFO  c.a.u.e.w.SimpleWebSocketClient - starting
21:02:40.228 [Grizzly(2) SelectorRunner] INFO  c.a.u.e.api.EchoEndpointResource - Received message from client: Hello WebSocket
21:02:40.232 [Grizzly(2)] INFO  c.a.u.e.w.SimpleWebSocketClient - Server replied with : Hello Ray
```

And with the annotated command line argument

```
$ mvn -q compile -P run-ursus-example-websocket-client -Dexec.args="annotated"
01:09:20.213 [main] INFO  c.a.u.e.w.SimpleWebSocketClient - starting
01:09:20.569 [Grizzly(2) SelectorRunner] INFO  c.a.u.e.a.AnnotatedEndPointResource - Received message from client: Hello WebSocket
01:09:20.573 [Grizzly(2)] INFO  c.a.u.e.w.SimpleWebSocketClient - Server replied with : Hello WebSocket
$
```

We can see the server side ```EchoEndpointResource``` received the message ```Hello WebSocket``` from our WebSocket Client and replied with
the message ```Hello Ray``` which was received by ```SimpleWebSocketClient```.

<h1 id="niotransport quick start">NIOTransport Applications Quick Start</h1>

In addition to Jersey based Web Services Ursus allows you to rapidly create NIO based applications on the JVM, after all [Grizzly’s](https://grizzly.java.net/)
goal is to help developers to build scalable and robust servers using NIO as well as offering extended framework components: like HTTP and HTTPS.

Creating NIOTransport applications in Ursus is easy and uses the same configuration and deployment semantics as Ursus Jersey based applications. Let's take a look
at the ```ursus-example-nio-application``` and the ```NIOExampleApplication``` to get started on our first NIOTransport based service.

```java
package com.aceevo.ursus.example;

import com.aceevo.ursus.core.UrsusTCPNIOApplication;
import org.glassfish.grizzly.filterchain.*;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.utils.StringFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

public class NIOExampleApplication extends UrsusTCPNIOApplication<NIOExampleConfiguration> {

    public static void main(String[] args) {
        new NIOExampleApplication(args);
    }

    protected NIOExampleApplication(String[] args) {
        super(args);
    }


    @Override
    protected FilterChain boostrap(NIOExampleConfiguration nioExampleConfiguration, FilterChainBuilder filterChainBuilder) {
        return filterChainBuilder.add(new TransportFilter())
                .add(new StringFilter(Charset.forName("UTF-8")))
                .add(new HelloFilter()).build();
    }

    @Override
    protected void run(TCPNIOTransport transport) {
        startWithShutdownHook(transport);
    }

    private static class HelloFilter extends BaseFilter {

        private Logger LOGGER = LoggerFactory.getLogger(HelloFilter.class);

        public NextAction handleRead(final FilterChainContext context) {

            try {
                String message = context.getMessage();
                LOGGER.info("received message " + message);

                // Echo back to client
                context.write(message);
            } catch (Exception ex) {
                LOGGER.debug("exception handle read", ex);
            }

            return context.getStopAction();
        }
    }
}
```

Just like our ExampleApplication ```NIOExampleApplication``` extends a base application class. When creating an ```UrsusNIOApplication``` you have two choices
for which base class you'd like to extend. ```UrsusTCPNIOApplication``` or ```UrsusUDPNIOApplication``` and like UrsusJeryseyApplication we
parameterize our application with a type that extends a ```Configuration``` class. For UrsusNIOApplications our base configuration class is ```UrsusNIOApplicationConfiguration```.

```java
package com.aceevo.ursus.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UrsusNIOApplicationConfiguration extends UrsusConfiguration {

    @JsonProperty(required = true)
    private Server server;

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public static class Server {

        @JsonProperty(required = true)
        private String host;

        @JsonProperty(required = false)
        private int port = 8080;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }
}
```

<h3 id="nio bootstrap and run">NIOTransport Bootstrap and Run</h3>

UrsusNIOApplications follow the same semantics as ```UrsusJerseyApplications``` we create a constructor and call ```super(args)``` and creates a ```public static void main``` method
which instantiates our ```UrsusNIOApplication``` and passes our command line arguments. But the bootstrap and run methods differ in the arguments they take. Let's inspect
these more closely.

```java
    protected FilterChain boostrap(NIOExampleConfiguration nioExampleConfiguration, FilterChainBuilder filterChainBuilder) {
        return filterChainBuilder.add(new TransportFilter())
                .add(new StringFilter(Charset.forName("UTF-8")))
                .add(new HelloFilter()).build();
    }

    @Override
    protected void run(TCPNIOTransport transport) {
        startWithShutdownHook(transport);
    }
```

The bootstrap method takes an instance of ```NIOExampleConfiguration``` and a ```FilterChainBuilder``` for arguments. In addition the bootstrap method returns
an instance of ```FilterChain```. The bootstrap method creates a new FilterChain with a single ```StringFilter``` followed by our custom ```HelloFilter```.

The run method takes a ```TCPNIOTransport``` as an argument and like our ```UrsusJerseyApplication``` we'll call ```startWithShutdownHook``` if we do not have any
more programmatic changes to make to our ```TCPNIOTransport```.

<h3 id="a basic filter">Implementing a BasicFilter</h3>

[Grizzly](https://grizzly.java.net/) provides several [examples](https://grizzly.java.net/quickstart.html) of how to use FilterChains and [Filters](https://grizzly.java.net/docs/2.3/apidocs/org/glassfish/grizzly/filterchain/Filter.html)
to build NIO applications (more on that later). The ```Filter``` interface is as follows

```java

    void  exceptionOccurred(FilterChainContext ctx, Throwable error)
    NextAction	handleAccept(FilterChainContext ctx)
    NextAction	handleClose(FilterChainContext ctx)
    NextAction	handleConnect(FilterChainContext ctx)
    NextAction	handleEvent(FilterChainContext ctx, FilterChainEvent event)
    NextAction	handleRead(FilterChainContext ctx)
    NextAction	handleWrite(FilterChainContext ctx)
    void  onAdded(FilterChain filterChain)
    void  onFilterChainChanged(FilterChain filterChain)
    void  onRemoved(FilterChain filterChain)
```

For our simple application ```HelloFilter``` will extend ```BasicFilter``` and override the ```handleRead``` method. We will use the ```FilterChainContext```
to retrieve the message sent from the client and write that message back to the ```FilterChainContext``` while returning a   ```NextAction```.

```java

    private static class HelloFilter extends BaseFilter {

        private Logger LOGGER = LoggerFactory.getLogger(HelloFilter.class);

        public NextAction handleRead(final FilterChainContext context) {

            try {
                String message = context.getMessage();
                LOGGER.info("received message " + message);

                // Echo back to client
                context.write(message);
            } catch (Exception ex) {
                LOGGER.debug("exception handle read", ex);
            }

            return context.getStopAction();
        }
    }
```

<h3 id="nio clients">Implementing Ursus NIO Clients</h3>

Implementing a Grizzly NIO client is very similar to implementing a server. We will need to build a [FilterChain](https://grizzly.java.net/docs/2.3/apidocs/org/glassfish/grizzly/filterchain/FilterChain.html)
and create a [TCPNIOTransport](https://grizzly.java.net/docs/2.3/apidocs/org/glassfish/grizzly/nio/transport/jmx/TCPNIOTransport.html) to use our ```FilterChain```. Let's take a look at
the ursus-example-nio-client project and the ```NIOExampleClient``` class.

```java
package com.aceevo.ursus.example;

import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.filterchain.*;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.utils.StringFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

public class NIOExampleClient {

    private Logger LOGGER = LoggerFactory.getLogger(NIOExampleClient.class);
    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    public NIOExampleClient() {
        Thread clientRunnerThread = new Thread(new ClientRunner());
        clientRunnerThread.start();
    }

    public static void main(String[] args) {
        new NIOExampleClient();
    }

    private class ClientRunner implements Runnable {

        @Override
        public void run() {
            Connection connection = null;
            try {

                final FilterChainBuilder clientFilterBuilder = FilterChainBuilder.stateless()
                        .add(new TransportFilter())
                        .add(new StringFilter(Charset.forName("UTF-8")))
                        .add(new HelloClientFilter(countDownLatch));


                final TCPNIOTransport transport = TCPNIOTransportBuilder.newInstance()
                        .setProcessor(clientFilterBuilder.build())
                        .build();

                try {
                    transport.start();
                    connection = transport.connect("localhost", 20389).get();
                    connection.write("Hello Ursus");
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

                countDownLatch.await();
                if (connection != null)
                    connection.close();

                try {
                    transport.stop();
                } catch (IOException e) {
                    LOGGER.debug("can't close connect", e);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (ExecutionException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    private static class HelloClientFilter extends BaseFilter {

        private final CountDownLatch countDownLatch;
        private Logger LOGGER = LoggerFactory.getLogger(HelloClientFilter.class);

        public HelloClientFilter(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        public NextAction handleRead(final FilterChainContext context) {
            try {
                String message = context.getMessage();
                LOGGER.info("received message from server: " + message);
                countDownLatch.countDown();
            } catch (Exception ex) {
                LOGGER.debug("exception handle read", ex);
            }

            return context.getStopAction();
        }

    }
}
```

<h3 id="client filters">Writing ClientFilter</h3>

This is a basic echo application so our ```HelloClientFilter``` essentially mirrors the ```HelloFilter```. It's job is to receive responses
from the server [NIOTransport](https://grizzly.java.net/docs/2.3/apidocs/org/glassfish/grizzly/nio/NIOTransport.html) filter and log the response. Our
client initiates the conversation in the ```ClientRunner``` class by writing to our connection.

```java
    try {
        transport.start();
        connection = transport.connect("localhost", 20389).get();
        connection.write("Hello Ursus");
    } catch (IOException e) {
        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }

    ...

    private static class HelloClientFilter extends BaseFilter {

        private final CountDownLatch countDownLatch;
        private Logger LOGGER = LoggerFactory.getLogger(HelloClientFilter.class);

        public HelloClientFilter(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        public NextAction handleRead(final FilterChainContext context) {
            try {
                String message = context.getMessage();
                LOGGER.info("received message from server: " + message);
                countDownLatch.countDown();
            } catch (Exception ex) {
                LOGGER.debug("exception handle read", ex);
            }

            return context.getStopAction();
        }
    }
```

<h3 id="running nio application">Running NIOTransport applications</h3>

Let's use the ```ursus-example-nio-application``` project to start our server. Once we've started our server we can launch the ```ursus-example-nio-client```
and watch it connect to our [NIOTransport](https://grizzly.java.net/docs/2.3/apidocs/org/glassfish/grizzly/nio/NIOTransport.html) application, say "Hello Ursus" and
verify that the server has responded.

```
$ mvn -q compile -P run-ursus-example-nio-application
23:21:27.332 [main] INFO  o.h.validator.internal.util.Version - HV000001: Hibernate Validator 5.0.1.Final
23:21:27.638 [main] INFO  c.a.ursus.core.UrsusNIOApplication - Starting all managed services...
23:21:27.682 [main] INFO  c.a.ursus.core.UrsusNIOApplication - Starting NIOExampleApplication
 __  __   ______    ______   __  __   ______
/_/\/_/\ /_____/\  /_____/\ /_/\/_/\ /_____/\
\:\ \:\ \\:::_ \ \ \::::_\/_\:\ \:\ \\::::_\/_
 \:\ \:\ \\:(_) ) )_\:\/___/\\:\ \:\ \\:\/___/\
  \:\ \:\ \\: __ `\ \\_::._\:\\:\ \:\ \\_::._\:\
   \:\_\:\ \\ \ `\ \ \ /____\:\\:\_\:\ \ /____\:\
    \_____\/ \_\/ \_\/ \_____\/ \_____\/ \_____\/

23:21:27.682 [main] INFO  c.a.ursus.core.UrsusNIOApplication - Press CTRL^C to exit..
^Z
[1]+  Stopped                 mvn -q compile -P run-ursus-example-application
$ bg
[1]+ mvn -q compile -P run-ursus-example-application &
$ mvn -q compile -P run-ursus-example-nio-client
23:21:49.485 [Grizzly-worker(1)] INFO  c.a.u.e.NIOExampleApplication$HelloFilter - received message Hello Ursus
23:21:49.493 [Grizzly-worker(1)] INFO  c.a.u.e.NIOExampleClient$HelloClientFilter - received message from server: Hello Ursus
$

 ```

