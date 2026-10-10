package io.github.matsfeddern.navarch.junit5;

import io.github.matsfeddern.navarch.core.connector.Connector;
import io.github.matsfeddern.navarch.core.connector.ConnectorLookup;
import io.github.matsfeddern.navarch.core.driver.DriverFleet;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public class NavarchExtension implements ParameterResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(NavarchExtension.class);
    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(NavarchExtension.class);
    private static final String DRIVER_FLEET_KEY = "driverFleet";

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        Class<?> type = parameterContext.getParameter().getType();
        boolean supported = type == DriverFleet.class;
        LOGGER.debug("supportsParameter for {}: {}", type, supported);
        return supported;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        LOGGER.debug("Resolving DriverFleet parameter");

        validateNativeType(parameterContext, ConnectorLookup.activeConnector());

        List<String> names = parameterContext.findRepeatableAnnotations(ForDriver.class).stream()
                .map(ForDriver::value)
                .toList();
        DriverFleet<?> fleet = extensionContext.getStore(NAMESPACE)
                .getOrComputeIfAbsent(DRIVER_FLEET_KEY, key -> new DriverFleet<>(names), DriverFleet.class);
        fleet.prepareForTest();

        return fleet;
    }

    private static void validateNativeType(ParameterContext parameterContext, Connector<?> connector) {
        Type genericType = parameterContext.getParameter().getParameterizedType();
        if (!(genericType instanceof ParameterizedType parameterizedType)) {
            return;
        }

        Type typeArgument = parameterizedType.getActualTypeArguments()[0];
        if (typeArgument instanceof Class<?> declaredNativeType && declaredNativeType != connector.nativeType()) {
            throw new IllegalArgumentException(
                    "Parameter declares native type " + declaredNativeType.getName()
                            + " but active connector " + connector + " produces " + connector.nativeType().getName());
        }
    }
}
