# property-config
Read configuration from builder-specified sources or use one of the default templates to get sources as defined by 
some conventions.

Future support is planned as AWS for secret-distribution

## Property-files

1. Create `application.properties` in your `src/main/resources` folder.
1. Create `local_override.properties` file on service root (or where you have your current-working-directory when running service)

If you are moving from `local_config.properties` consider making an alias to support rollback `ln -s local_override.properties local_config.properties`

### Service initialization (main)

#### As the first step in your application (or just after log-setup), run the builder as following to initialize with defaults

```java
ApplicationProperties.builderWithDefaults()
    .buildAndSetStaticSingleton();
```

The default template loads configuration from the following sources and in the following order where latter sources override earlier ones:
1. The first `application.properties` resource found on the classpath. If the are multiple such resources on the 
   classpath, only the first one found is loaded.
1. The file `local_override.properties` in the current-working-directory.
1. System properties as set on the command line. e.g. `java "-Dservice.prop=prod" -jar app.jar`
1. Environment variables using the escaping rules as documented in this readme.

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
String serverHost = ApplicationProperties.getInstance().get(MainProperties.SERVER_HOST)
int serverPort = ApplicationProperties.getInstance().asInt(MainProperties.SERVER_PORT)
```
or with default port of 8080 if not defined in ApplicationProperties instance
```java
String serverHost = ApplicationProperties.getInstance().get(MainProperties.SERVER_HOST, "localhost")
int serverPort = ApplicationProperties.getInstance().asInt(MainProperties.SERVER_PORT, 8080)
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




## Testing 

If you are creating configuration to be used for unit-testing you should use the following initialization

```java
ApplicationProperties.builderWithTestDefaults()
    .buildAndSetStaticSingleton();
```

The default template loads configuration from the following sources and in the following order:
1. The first `application.properties` resource found on the classpath. If the are multiple such resources on the
   classpath, only the first one found is loaded.
1. The file `local_override.properties` in the current-working-directory.
1. System properties as set on the command line. e.g. `mvn clean install -Dserver.port=30123`

The builder defines a `property(String key, String value)` that can be used when overriding the exisiting properties 
or setting a spesific property for a test. You can also skip the `withExpectedProperties`. When this is skipped the 
`build()` call does not validate the set of existing properties, which can be handy for a partial test.  

If you use the `buildAndSetStaticSingleton()` method, this may affect test needing different properties on startup. 
To help, the `testsupport` package is packed in main and contains a 
`ApplicationPropertiesTestHelper.resetApplicationProperties()` that voids the singleton allowing the builder to be 
initialized again. To help future developers, run this in teardown of tests whenever you initialize the 
`ApplicationProperties`. 

