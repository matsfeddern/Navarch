package io.github.matsfeddern.navarch.core.driver;

import io.github.matsfeddern.navarch.core.connector.DriverConfig;

public final class TestDriverConfigs {

    private TestDriverConfigs() {
    }

    public static DriverConfig labeled(String label) {
        return new TestDriverConfig(label);
    }
}
