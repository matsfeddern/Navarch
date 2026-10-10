package io.github.matsfeddern.navarch.core.driver;

import io.github.matsfeddern.navarch.core.connector.Connector;
import io.github.matsfeddern.navarch.core.connector.ConnectorLookup;
import io.github.matsfeddern.navarch.core.connector.DriverConfig;
import io.github.matsfeddern.navarch.core.connector.DriverConfigProvider;
import io.github.matsfeddern.navarch.core.connector.DriverConfigProviderLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DriverFleet<W> implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(DriverFleet.class);

    private final Map<String, Driver<W>> drivers = new ConcurrentHashMap<>();
    private final Set<String> managedNames;

    public DriverFleet() {
        this(List.of());
    }

    public DriverFleet(List<String> names) {
        this.managedNames = Set.copyOf(names);
    }

    public Driver<W> getDriver(String driverName) {
        Driver<W> driver = drivers.get(driverName);
        LOGGER.debug("Lookup for driver {}: {}", driverName, driver != null ? "found" : "not found");
        return driver;
    }

    private Driver<W> getOrRecreate(String driverName, DriverConfig config, Connector<W> connector) {
        return drivers.compute(driverName, (key, existing) -> {
            if (existing == null) {
                LOGGER.debug("No driver found for {}, creating a new one via {}", driverName, connector);
                return new Driver<>(connector.create(config), connector);
            }
            LOGGER.debug("Driver {} already in fleet, recreating it", driverName);
            return existing.recreate(config);
        });
    }

    @SuppressWarnings("unchecked")
    public void prepareForTest() {
        Connector<W> connector = (Connector<W>) ConnectorLookup.activeConnector();
        DriverConfigProvider provider = DriverConfigProviderLookup.activeProvider();

        List<CompletableFuture<Failure>> prepareProbes = managedNames.stream()
                .map(name -> CompletableFuture
                        .supplyAsync(() -> getOrRecreate(name, provider.get(name), connector))
                        .handle((driver, error) -> new Failure(name, error)))
                .toList();

        CompletableFuture.allOf(prepareProbes.toArray(new CompletableFuture[0])).join();

        List<Failure> failures = prepareProbes.stream()
                .map(CompletableFuture::join)
                .filter(failure -> failure.cause() != null)
                .toList();

        if (!failures.isEmpty()) {
            List<String> failedDriverNames = failures.stream().map(Failure::driverName).toList();
            IllegalStateException combined = new IllegalStateException(
                    failedDriverNames.size() + " of " + managedNames.size()
                            + " driver(s) failed to prepare: " + failedDriverNames);
            failures.forEach(failure -> combined.addSuppressed(failure.cause()));
            LOGGER.error("Drivers failed to prepare: {}", failedDriverNames, combined);
            throw combined;
        }
    }

    public <R> CompletableFuture<Stream<R>> executeAll(Function<W, R> command) {
        Map<String, CompletableFuture<R>> futures = drivers.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().execute(command)));

        if (futures.isEmpty()) {
            LOGGER.error("No drivers in fleet to execute the command on");
            throw new IllegalStateException("No drivers in fleet to execute the command on");
        }

        LOGGER.debug("Executing command on {} driver(s)", futures.size());

        List<CompletableFuture<Failure>> failureProbes = futures.entrySet().stream()
                .map(entry -> entry.getValue().handle((result, error) -> new Failure(entry.getKey(), error)))
                .toList();

        return CompletableFuture.allOf(failureProbes.toArray(new CompletableFuture[0]))
                .thenApply(unused -> {
                    List<Failure> failures = failureProbes.stream()
                            .map(CompletableFuture::join)
                            .filter(failure -> failure.cause() != null)
                            .toList();

                    if (!failures.isEmpty()) {
                        List<String> failedDriverNames = failures.stream().map(Failure::driverName).toList();
                        IllegalStateException combined = new IllegalStateException(
                                failedDriverNames.size() + " of " + futures.size()
                                        + " driver(s) failed: " + failedDriverNames);
                        failures.forEach(failure -> combined.addSuppressed(failure.cause()));
                        LOGGER.error("Drivers failed: {}", failedDriverNames, combined);
                        throw combined;
                    }

                    return futures.values().stream().map(CompletableFuture::join);
                });
    }

    private record Failure(String driverName, Throwable cause) {
    }

    public <R> CompletableFuture<R> execute(Function<W, R> command) {
        return executeAll(command).thenApply(results -> {
            List<R> resultList = results.toList();
            R first = resultList.get(0);
            boolean allEqual = resultList.stream().allMatch(result -> Objects.equals(result, first));
            if (!allEqual) {
                LOGGER.error("Drivers returned inconsistent results: {}", resultList);
                throw new IllegalStateException("Drivers returned inconsistent results: " + resultList);
            }
            return first;
        });
    }

    @Override
    public void close() {
        LOGGER.debug("Closing {} driver(s) in fleet", drivers.size());
        drivers.values().forEach(Driver::close);
        drivers.clear();
    }
}
