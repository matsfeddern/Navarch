package io.github.matsfeddern.navarch.core.driver;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DriverFleetTest {

    @Test
    void executeAllReturnsOneResultPerPooledDriver() {
        DriverFleet<TestNativeDriver> driverFleet = new DriverFleet<>(List.of("special", "other"));
        driverFleet.prepareForTest();

        List<String> labels = driverFleet.executeAll(TestNativeDriver::label).join().toList();

        assertThat(labels).containsExactlyInAnyOrder("special", "other");
    }

    @Test
    void executeReturnsTheSharedValueWhenAllDriversAgree() {
        DriverFleet<TestNativeDriver> driverFleet = new DriverFleet<>(List.of("agree-a", "agree-b"));
        driverFleet.prepareForTest();

        String label = driverFleet.execute(TestNativeDriver::label).join();

        assertThat(label).isEqualTo("same");
    }

    @Test
    void executeThrowsWhenDriversDisagree() {
        DriverFleet<TestNativeDriver> driverFleet = new DriverFleet<>(List.of("special", "other"));
        driverFleet.prepareForTest();

        CompletableFuture<String> allResults = driverFleet.execute(TestNativeDriver::label);

        assertThatThrownBy(allResults::join)
                .cause()
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("inconsistent");
    }

    @Test
    void executeAllAggregatesFailuresFromEveryDriverAsSuppressed() {
        try (DriverFleet<TestNativeDriver> driverFleet = new DriverFleet<>(List.of("special", "other"))) {
            driverFleet.prepareForTest();
            CompletableFuture<Stream<Integer>> allResults = driverFleet.executeAll(TestNativeDriver::crash);

            assertThatThrownBy(allResults::join)
                    .cause()
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("2 of 2 driver(s) failed")
                    .satisfies(cause -> assertThat(cause.getSuppressed()).hasSize(2));
        }
    }

    @Test
    void executeAllReportsOnlyTheDriverThatActuallyFailed() {
        try (DriverFleet<TestNativeDriver> driverFleet = new DriverFleet<>(List.of("special", "other"))) {
            driverFleet.prepareForTest();

            Function<TestNativeDriver, String> command = nativeDriver -> {
                if ("other".equals(nativeDriver.label())) {
                    throw new IllegalStateException("boom");
                }
                return nativeDriver.label();
            };

            CompletableFuture<Stream<String>> allResults = driverFleet.executeAll(command);

            assertThatThrownBy(allResults::join)
                    .cause()
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("1 of 2 driver(s) failed")
                    .hasMessageContaining("other")
                    .satisfies(cause -> assertThat(cause.getSuppressed()).hasSize(1));
        }
    }

    @Test
    void prepareForTestRecreatesAnAlreadyPooledDriver() {
        DriverFleet<TestNativeDriver> driverFleet = new DriverFleet<>(List.of("special"));
        driverFleet.prepareForTest();
        int firstIdentity = driverFleet.getDriver("special").execute(System::identityHashCode).join();

        driverFleet.prepareForTest();
        int secondIdentity = driverFleet.getDriver("special").execute(System::identityHashCode).join();

        assertThat(secondIdentity).isNotEqualTo(firstIdentity);
    }

    @Test
    void prepareForTestAggregatesFailuresButStillPreparesTheOthers() {
        DriverFleet<TestNativeDriver> driverFleet = new DriverFleet<>(List.of("special", "unknown"));

        assertThatThrownBy(driverFleet::prepareForTest)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("1 of 2 driver(s) failed to prepare")
                .hasMessageContaining("unknown")
                .satisfies(cause -> assertThat(cause.getSuppressed()).hasSize(1));

        assertThat(driverFleet.getDriver("special")).isNotNull();
        assertThat(driverFleet.getDriver("unknown")).isNull();
    }

    @Test
    void closeShutsDownEveryDriverInThePool() {
        DriverFleet<TestNativeDriver> driverFleet = new DriverFleet<>(List.of("special", "other"));
        driverFleet.prepareForTest();
        Driver<TestNativeDriver> special = driverFleet.getDriver("special");
        Driver<TestNativeDriver> other = driverFleet.getDriver("other");

        driverFleet.close();

        assertThatThrownBy(() -> special.execute(TestNativeDriver::label))
                .isInstanceOf(RejectedExecutionException.class);
        assertThatThrownBy(() -> other.execute(TestNativeDriver::label))
                .isInstanceOf(RejectedExecutionException.class);
    }

    @Test
    void closeClearsThePoolSoDeadDriversAreNotHandedOutAfterwards() {
        DriverFleet<TestNativeDriver> driverFleet = new DriverFleet<>(List.of("special"));
        driverFleet.prepareForTest();

        driverFleet.close();

        assertThat(driverFleet.getDriver("special")).isNull();
    }
}
