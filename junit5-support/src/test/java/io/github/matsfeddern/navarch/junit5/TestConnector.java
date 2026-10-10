package io.github.matsfeddern.navarch.junit5;

import io.github.matsfeddern.navarch.core.connector.Connector;
import io.github.matsfeddern.navarch.core.connector.DriverConfig;

public class TestConnector implements Connector<TestNativeDriver> {

    @Override
    public Class<TestNativeDriver> nativeType() {
        return TestNativeDriver.class;
    }

    @Override
    public TestNativeDriver create(DriverConfig config) {
        String label = config instanceof TestDriverConfig testDriverConfig ? testDriverConfig.label() : "default";
        return new TestNativeDriver(label);
    }

    @Override
    public void destroy(TestNativeDriver instance) {
    }
}
