package io.github.matsfeddern.navarch.connector.selenium;

import io.github.matsfeddern.navarch.core.connector.Connector;
import io.github.matsfeddern.navarch.core.connector.DriverConfig;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.WebDriverInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceLoader;

public class SeleniumConnector implements Connector<WebDriver> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SeleniumConnector.class);

    @Override
    public Class<WebDriver> nativeType() {
        return WebDriver.class;
    }

    @Override
    public WebDriver create(DriverConfig config) {
        SeleniumDriverConfig seleniumConfig = asSeleniumConfig(config);
        Capabilities capabilities = seleniumConfig.capabilities();

        if (seleniumConfig.remoteAddress() != null) {
            LOGGER.debug("Creating RemoteWebDriver for {} with capabilities {}", seleniumConfig.remoteAddress(), capabilities);
            return new RemoteWebDriver(seleniumConfig.remoteAddress(), capabilities);
        }

        LOGGER.debug("Creating local WebDriver for capabilities {}", capabilities);
        return ServiceLoader.load(WebDriverInfo.class).stream()
                .map(ServiceLoader.Provider::get)
                .filter(info -> info.isSupporting(capabilities) && info.isAvailable())
                .findFirst()
                .flatMap(info -> info.createDriver(capabilities))
                .orElseThrow(() -> new IllegalStateException(
                        "No available WebDriverInfo supports capabilities: " + capabilities));
    }

    @Override
    public void destroy(WebDriver instance) {
        LOGGER.debug("Quitting WebDriver {}", instance);
        instance.quit();
    }

    private static SeleniumDriverConfig asSeleniumConfig(DriverConfig config) {
        if (config instanceof SeleniumDriverConfig seleniumDriverConfig) {
            return seleniumDriverConfig;
        }
        throw new IllegalArgumentException("SeleniumConnector requires a SeleniumDriverConfig, got: " + config);
    }
}
