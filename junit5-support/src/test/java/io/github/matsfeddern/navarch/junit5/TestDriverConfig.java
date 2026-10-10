package io.github.matsfeddern.navarch.junit5;

import io.github.matsfeddern.navarch.core.connector.DriverConfig;

public class TestDriverConfig implements DriverConfig {
    private final String label;

    public TestDriverConfig(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
