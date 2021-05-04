# property-config

Fast and opinionated configuration library with zero dependencies. Configuration values are configured as properties 
through property-files and may be overridden through system-properties or environment-variables. The library has 
default conventions on how to load configuration, which makes it very easy to use. Once built, internal state never 
changes, which allows the library to pre-process all properties into one immutable and fast map. There is no need for 
synchronization which makes the `ApplicationProperties` instance safe to share among threads.

The library standardizes on the use of certain files and/or properties helping the developers and operations to 
recognize how an application is configured. This is particularly useful in a microservice architecture where there are 
many services and applications.

The library first loads `application.properties` from the classpath, then `local_override.properties` from the 
current-working-directory, then system-properties, then environment-variables. Should you want to use the library with
different sources, then you can build your instance using the plain builder which requires you to specify all your 
configuration sources.

Properties from all configured sources are merged together into one immutable map upon building the 
`ApplicationProperties` instance. The sources configured will have their properties put into the map in the order they 
are configured, hence sources configured after will overwrite properties from sources that have been configured before.

### Maven

#### Configure the Cantara repository
```xml
<repository>
    <id>cantara-releases</id>
    <name>Cantara Release Repository</name>
    <url>http://mvnrepo.cantara.no/content/repositories/releases/</url>
</repository>
```
#### Include the dependency
```xml
<dependency>
    <groupId>no.cantara.config</groupId>
    <artifactId>property-config</artifactId>
    <version>0.6.0</version>
</dependency>
```


### Configuring applications

1. Create `application.properties` in your `src/main/resources` folder and put your basic configuration here.
1. Create `local_override.properties` file on service root (or where you have your current-working-directory 
   when running service). Here you will put all your properties that are specific to the environment of the application
   running where the file is located.

If you are moving from `local_config.properties` (legacy from earlier conventions) consider making an alias to support 
rollback `ln -s local_override.properties local_config.properties`

### Service initialization (main)

#### As the first step in your application (or just after log-setup), initialize the static singleton.

```java
ApplicationProperties.builderWithDefaults()
    .buildAndSetStaticSingleton();
```

The default template loads configuration from the following sources and in the following order where latter sources 
override earlier ones:
1. The first `application.properties` resource found on the classpath. If there are multiple such resources on the 
   classpath, only the first one found is loaded.
1. The file `local_override.properties` in the current-working-directory.
1. System properties as set on the command line. e.g. `java "-Dservice.prop=prod" -jar app.jar`. (*)
1. Environment variables using the escaping rules as documented in this readme. e.g. `SERVICE_PROP=prod`. (*)

(*) Note that environment properties may only override existing properties, new properties are eliminated.

#### A more advanced example with expected properties set:

You can specify properties that are expected to be present (a whitelist). Upon building the ApplicationProperties
instance the merged properties map is validated against the whitelist, and initialization will fail with a runtime
exception if any of the properties specified in the `MainProperties.class` or `ServiceProperties.class` are missing.

```java
ApplicationProperties.builderWithDefaults()
    .expectedProperties(MainProperties.class, ServiceProperties.class)  
    .buildAndSetStaticSingleton();
```

Property key-value pairs are logged with keys containing `secret`, `token` or `password` gets obfuscated values.
Any additional properties are sent and usable by the application, but logged as warning. Consider adding these to one of
the classes used as expected properties.

`MainProperties` could for example be defined as follows:

```java
class MainProperties {
   public static final String SERVER_HOST = "server.host";
   public static final String SERVER_PORT = "server.port";
   public static final String OPENAPI_FILTER_CLASS = "mp.openapi.filter";
}
```

### Usage

This allows us to use 
```java
String serverHost = ApplicationProperties.getInstance().get("server.host")
int serverPort = ApplicationProperties.getInstance().asInt("server.port")
```
or with default port of 8080 if not defined in ApplicationProperties instance
```java
String serverHost = ApplicationProperties.getInstance().get("server.host", "localhost")
int serverPort = ApplicationProperties.getInstance().asInt("server.port", 8080)
```

If the application server uses property-injection and expects a certain set of properties, the properties can be exported and forwarded 
to a config. The following example is for creating a Helidon MP-configuration allowin only our defined property-set.

```java
Config.builder()
    .disableEnvironmentVariablesSource()
    .disableSystemPropertiesSource()
    .sources(
            MapConfigSource.builder().map(applicationProperties.map()))
    .build()
```

## Unix Environment Varables

#### Expected conventions

Java and Unix/Posix has two different conventions to key's.
Java: key.name=value
Unix: KEY_NAME=value or key_name=value

Java's approach is used in properties files. 
Unix's is most often used in runtime environment. This is also default in Docker, Kubernetes and other cloud environments.

#### Escaping rules

Environment-variables as properties can be enabled through ApplicationProperties.Builder methods. If enabled without
escaping, then environment-variable names are not treated as if they are escaped and will be used as-is as properties
preserving everything including case. If enabled with escaping (default), then environment-variable names will be 
treated as escaped according to the escaping rules.

Escaping rules are applied in the following order:
1. all letters become UPPERCASE
1. `_` underscore becomes `_u_`
1. `.` dot becomes `_`
1. `-` dash becomes `_d_`

Example 1: An application is has set the property `my.property=precious` in the `application.properties` file. At runtime
the environment-variable `MY_PROPERTY=worthless` must be set in order to override the value of `my.property` within the
application.

Example 2: An application is has set the property `my-property=precious` in the `application.properties` file. At runtime
the environment-variable `MY_d_PROPERTY=worthless` must be set in order to override the value of `my-property` within the
application.

#### Advanced use of casing

If you need to use java-properties with keys that are not all lower-case, and also need to override these with
environment-variables, then you have to use the special `envVarCasing` method to set up how to configure override for 
these properties.  

E.g. assume you are using another library that uses this library for configuration and expect to be able to do
`ApplicationProperties.getInstance().get("serverPort")` in order to read the configured port. Now if you want to
override that property using environment-variables you can do as follows:

```java
ApplicationProperties.builderWithDefaults()
    .envVarCasing("serverPort")
    .buildAndSetStaticSingleton();
```

This will cause the environment-variable `SERVERPORT=9034` to be interpreted as if the java property `serverPort=9034`
was set.


##### Debugging configuration

A full dump of all properties, and the sources they come from, and optionally which other sources was overridden can be
dumped to stdout using the following statement:
```java
System.out.println(ApplicationProperties.getInstance().debugAll(true));
```

## Testing 

To allow changing configuration between tests, the library provides a mutable (but slower) ApplicationProperties
implementation. Enable the mutable static singleton before any other initialization.

```java
    @BeforeClass
    public static void enableMutableSingleton() {
        ApplicationPropertiesTestHelper.enableMutableSingleton();
    }
```

To initialize the static singleton with test-defaults, use one of the builderWithTestDefaults initialization methods.

```java
ApplicationProperties.builderWithTestDefaults()
    .buildAndSetStaticSingleton();
```

The default template loads configuration from the following sources and in the following order:
1. The first `application.properties` resource found on the classpath. If the are multiple such resources on the
   classpath, only the first one found is loaded.
1. The file `local_override.properties` in the current-working-directory.
1. System properties as set on the command line. e.g. `mvn clean install -Dserver.port=30123`

The builder defines a `property(String key, String value)` that can be used when overriding the existing properties 
or setting a specific property for a test. You can also skip the `expectedProperties`. When this is skipped the 
`build()` call does not validate the set of existing properties, which can be handy for a partial test.
