# Navarch
Navarch is a lifecycle manager for browser drivers.

Navarch allows creation of fleets of drivers and execution of commands simultaneously on all drivers in the fleet. The central module of Navarch, navarch-core, is driver-independent. Driver-specific behavior comes through the use of connectors, which allow integration of specific driver frameworks. Thus, Navarch is heavily extensible with new or existing browser automation frameworks that are not yet supported. The navarch-selenium-connector is such a connector and allows the usage of Selenium WebDriver with Navarch.

Navarch is also not bound to a specific testing framework. Testing framework integration is built on top of navarch-core, like the navarch-junit5-support module, which provides a JUnit 5 extension.

## Usage
Navarch's modules are published on Maven Central.

In order to use Navarch, you have to import navarch-core and exactly one connector (e.g. navarch-selenium-connector). Importing multiple connectors will result in a hard fail:
```xml
<dependencies>
    <dependency>
        <groupId>io.github.matsfeddern</groupId>
        <artifactId>navarch-core</artifactId>
        <version>0.1.0</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>io.github.matsfeddern</groupId>
        <artifactId>navarch-selenium-connector</artifactId>
        <version>0.1.0</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

Depending on the connector, you may have to import the browser automation library explicitly. In order to use navarch-selenium-connector, selenium-java has to be imported:
```xml
<dependencies>
    <dependency>
        <groupId>org.seleniumhq.selenium</groupId>
        <artifactId>selenium-java</artifactId>
        <version>4.49.0</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

Navarch uses SLF4J as its logging library. Therefore, it is recommended to add a corresponding implementation to your
project if not already present (e.g. log4j):
```xml
<dependencies>
    <dependency>
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-slf4j2-impl</artifactId>
        <version>2.26.1</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-core</artifactId>
        <version>2.26.1</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

Drivers for use with Navarch can be defined by writing exactly one implementation of `io.github.matsfeddern.navarch.core.connector.DriverConfigProvider` and loading it by using Java's SPI mechanism. An implementation of `DriverConfigProvider` is the [`SeleniumDriverConfigProvider`](https://github.com/matsfeddern/Navarch/blob/master/selenium-connector/src/test/java/io/github/matsfeddern/navarch/connector/selenium/SeleniumDriverConfigProvider.java). It is loaded with the SPI mechanism through [a file with the name of the interface and the implementation name as content](https://github.com/matsfeddern/Navarch/blob/master/selenium-connector/src/test/resources/META-INF/services/io.github.matsfeddern.navarch.core.connector.DriverConfigProvider) in the [META-INF/services](https://github.com/matsfeddern/Navarch/tree/master/selenium-connector/src/test/resources/META-INF/services) folder.

Afterward, the named drivers can be used for creating a [`DriverFleet`](https://github.com/matsfeddern/Navarch/blob/master/core/src/main/java/io/github/matsfeddern/navarch/core/driver/DriverFleet.java), which has to be prepared using [`DriverFleet#prepareForTest`](https://github.com/matsfeddern/Navarch/blob/master/core/src/main/java/io/github/matsfeddern/navarch/core/driver/DriverFleet.java#L53) and can then be used for executing commands simultaneously on the drivers:
```java
try (DriverFleet<WebDriver> fleet = new DriverFleet<>(List.of("chrome-driver", "firefox-driver"))) {
    fleet.prepareForTest();
    List<String> result = fleet.executeAll(driver -> {
            driver.get("https://example.com");
            return driver.getTitle();
        }).join()
        .toList();
    assertThat(result).containsExactly("Example Domain", "Example Domain");
}
```
DriverFleets can be reused by calling `prepareForTest` on the fleet for each test.
Closing fleets also terminates associated drivers.