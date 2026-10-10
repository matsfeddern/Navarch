package io.github.matsfeddern.navarch.core.driver;

import io.github.matsfeddern.navarch.core.connector.Connector;
import io.github.matsfeddern.navarch.core.connector.DriverConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class Driver<W> implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Driver.class);

    private final W nativeDriver;
    private final Connector<W> connector;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public Driver(W nativeDriver, Connector<W> connector) {
        this.nativeDriver = nativeDriver;
        this.connector = connector;
    }

    public <R> CompletableFuture<R> execute(Function<W, R> command) {
        LOGGER.debug("Submitting command to driver {}", nativeDriver);
        return CompletableFuture.supplyAsync(() -> command.apply(nativeDriver), executor)
                .whenComplete((result, error) -> {
                    if (error != null) {
                        LOGGER.error("Command on driver {} failed", nativeDriver, error);
                    } else {
                        LOGGER.debug("Command on driver {} returned {}", nativeDriver, result);
                    }
                });
    }

    public Driver<W> recreate(DriverConfig config) {
        LOGGER.debug("Recreating driver {}", nativeDriver);
        close();
        return new Driver<>(connector.create(config), connector);
    }

    @Override
    public void close() {
        LOGGER.debug("Closing driver {}", nativeDriver);
        executor.shutdown();
        connector.destroy(nativeDriver);
    }
}
