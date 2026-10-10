package io.github.matsfeddern.navarch.core.connector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ServiceLoader;

public final class ConnectorLookup {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorLookup.class);

    private static volatile Connector<?> cached;

    private ConnectorLookup() {
    }

    public static Connector<?> activeConnector() {
        Connector<?> connector = cached;
        if (connector == null) {
            synchronized (ConnectorLookup.class) {
                connector = cached;
                if (connector == null) {
                    connector = lookup();
                    cached = connector;
                }
            }
        }
        return connector;
    }

    private static Connector<?> lookup() {
        List<Connector> found = ServiceLoader.load(Connector.class).stream()
                .map(ServiceLoader.Provider::get)
                .toList();

        LOGGER.debug("Found {} connector(s) on the classpath: {}", found.size(), found);

        if (found.size() != 1) {
            LOGGER.error("Expected exactly one Connector on the classpath, found {}", found.size());
            throw new IllegalStateException(
                    "Expected exactly one Connector on the classpath, found " + found.size());
        }

        return found.get(0);
    }
}
