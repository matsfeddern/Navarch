package io.github.matsfeddern.navarch.connector.selenium;

import io.github.matsfeddern.navarch.core.connector.DriverConfig;
import io.github.matsfeddern.navarch.core.connector.DriverConfigProvider;

import java.util.Map;

public class SeleniumDriverConfigProvider implements DriverConfigProvider {
    private static final Map<String, DriverConfig> CONFIGS = Map.of(
            "firefox-headless", SeleniumDriverConfigs.firefoxHeadless(),
            "chrome-headless", SeleniumDriverConfigs.chromeHeadless()
    );

    @Override
    public DriverConfig get(String name) {
        DriverConfig config = CONFIGS.get(name);
        if (config == null) {
            throw new IllegalArgumentException("No DriverConfig registered for name: " + name);
        }
        return config;
    }
}
