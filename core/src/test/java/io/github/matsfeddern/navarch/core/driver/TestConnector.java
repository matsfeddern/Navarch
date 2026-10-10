package io.github.matsfeddern.navarch.core.driver;

import io.github.matsfeddern.navarch.core.connector.Connector;
import io.github.matsfeddern.navarch.core.connector.DriverConfig;

import java.util.ArrayList;
import java.util.List;

public class TestConnector implements Connector<TestNativeDriver> {

    private final List<TestNativeDriver> destroyedInstances = new ArrayList<>();

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
        destroyedInstances.add(instance);
    }

    public List<TestNativeDriver> destroyedInstances() {
        return destroyedInstances;
    }
}
