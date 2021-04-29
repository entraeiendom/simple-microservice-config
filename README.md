# property-config
Read configuration from builder-specified sources or defaults to reading application.properties on classpath and properties override from filesystem.

Future support is planned as AWS for secret-distribution

## Property-files

1. Create application.properties in your src/main/resouces.
2. Create local_override.properties file on service root

If you are moving from local_config.properties consider making an alias to support rollback `ln -s local_override.properties local_config.properties`

## Usage on runtime

The ApplicationPropertiesBuilder creates a *singelton* with static access through `ApplicationProperties.getInstance()`.

### Initialization

As the first step in your application (or just after log-setup), run the builder as following

```java

ApplicationProperties.builderWithDefaults()               // 1.
        .expectedProperties(                          // 2. 
                MainProperties.class,                     // 3.
                ServicesProperties.class)                 // 4.  
        .buildAndSetStaticSingleton();                    // 5.

```

1. Loads application.properties from classpath and overrides with local_override.properties from filesystem
2. Define classes of your own and/or use standard classes for common properties
3. Define your own class with content like the following, and use the constants later when retrieving properties
   
```java 
public static final String SERVER_PORT = "server.port";
public static final String OPENAPI_FILTER_CLASS = "mp.openapi.filter";
```
4. Common packed class with String-field `BASE_URL` referring to common property-key 'api.baseurl'
5. Builds instance, validates that expected properties are present and sets the built and valid instance as the static singleton instance.
   Validate that the set of property-keys defined in classes exists and has a value. If it does not exist or value is not present, 
   a runtime exeption is thrown. Property key-value pairs are logged with keys containing `secret`, `token` or `password` gets obfuscated values. 
   Any additional properties are sent and usable by the application, but logged as warning. Consider adding these to the classes in 3.

### Usage

This allows us to use 
```java
ApplicationProperties.getInstance().get(MainProperties.SERVER_PORT)
```
or with default port of 8080 if not defined in ApplicationProperties instance
```java
ApplicationProperties.getInstance().get(MainProperties.SERVER_PORT, 8080)
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




## Testing 

The builder defines a `property(String key, String value)` that can be used when overriding the exisiting properties 
or setting a spesific property for a test. You can also skip the `withExpectedProperties`. When this is skipped the `build()` call does not
validate the set of existing properties, which can be handy for a partial test.  

Since this is a singleton implementation, this may affect test needing different properties on startup. To help, the `testsupport` package is
packed in main and contains a `ApplicationPropertiesTestHelper.resetApplicationProperties()` that voids the singleton allowing the builder to be 
initialized again. To help future developers, run this in teardown of tests whenever you initialize the `ApplicationProperties`. 
