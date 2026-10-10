package io.github.matsfeddern.navarch.junit5;

import io.github.matsfeddern.navarch.core.connector.DriverConfig;

public final class TestDriverConfigs {

    private TestDriverConfigs() {
    }

    public static DriverConfig labeled(String label) {
        return new TestDriverConfig(label);
    }
}
