package io.github.matsfeddern.navarch.junit5;

import io.github.matsfeddern.navarch.core.connector.DriverConfig;
import io.github.matsfeddern.navarch.core.connector.DriverConfigProvider;

import java.util.Map;

public class TestDriverConfigProvider implements DriverConfigProvider {
    private static final Map<String, DriverConfig> CONFIGS = Map.of(
            "special", TestDriverConfigs.labeled("special"),
            "other", TestDriverConfigs.labeled("other")
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
