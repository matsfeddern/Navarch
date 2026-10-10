package io.github.matsfeddern.navarch.core.connector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ServiceLoader;

public final class DriverConfigProviderLookup {
    private static final Logger LOGGER = LoggerFactory.getLogger(DriverConfigProviderLookup.class);

    private static volatile DriverConfigProvider cached;

    private DriverConfigProviderLookup() {
    }

    public static DriverConfigProvider activeProvider() {
        DriverConfigProvider provider = cached;
        if (provider == null) {
            synchronized (DriverConfigProviderLookup.class) {
                provider = cached;
                if (provider == null) {
                    provider = lookup();
                    cached = provider;
                }
            }
        }
        return provider;
    }

    private static DriverConfigProvider lookup() {
        List<DriverConfigProvider> found = ServiceLoader.load(DriverConfigProvider.class).stream()
                .map(ServiceLoader.Provider::get)
                .toList();

        LOGGER.debug("Found {} DriverConfigProvider(s) on the classpath: {}", found.size(), found);

        if (found.size() != 1) {
            LOGGER.error("Expected exactly one DriverConfigProvider on the classpath, found {}", found.size());
            throw new IllegalStateException(
                    "Expected exactly one DriverConfigProvider on the classpath, found " + found.size());
        }

        return found.get(0);
    }
}
