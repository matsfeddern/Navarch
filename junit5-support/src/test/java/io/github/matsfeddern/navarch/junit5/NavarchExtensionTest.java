package io.github.matsfeddern.navarch.junit5;

import io.github.matsfeddern.navarch.core.driver.DriverFleet;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.platform.testkit.engine.EngineTestKit;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.instanceOf;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.message;

@ExtendWith(NavarchExtension.class)
class NavarchExtensionTest {
    @Test
    void forDriverAnnotationPreparesNamedDriverInPool(@ForDriver("special") DriverFleet<TestNativeDriver> driverFleet) {
        String label = driverFleet.getDriver("special").execute(TestNativeDriver::label).join();

        assertThat(label).isEqualTo("special");
    }

    @Test
    void testDriverFleetExecute(
            @ForDriver("special") @ForDriver("other") DriverFleet<TestNativeDriver> driverFleet) {
        int result = driverFleet.execute(nativeDriver -> nativeDriver.add(1, 2)).join();

        assertThat(result).isEqualTo(3);
    }

    @Test
    void testDriverExecuteWithException(@ForDriver("special") DriverFleet<TestNativeDriver> driverFleet) {
        CompletableFuture<Integer> future = driverFleet.getDriver("special").execute(TestNativeDriver::crash);

        assertThatThrownBy(future::join)
                .isInstanceOf(CompletionException.class)
                .cause()
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("crash");
    }

    @Test
    void testForDriverAnnotationRepeatedOnDriverFleet(
            @ForDriver("special") @ForDriver("other") DriverFleet<TestNativeDriver> driverFleet) {
        assertThat(driverFleet.getDriver("special").execute(TestNativeDriver::label).join()).isEqualTo("special");
        assertThat(driverFleet.getDriver("other").execute(TestNativeDriver::label).join()).isEqualTo("other");
    }

    @Test
    void testDriverFleetParameterWithMismatchedNativeTypeIsRejected() {
        EngineTestKit.engine("junit-jupiter")
                .selectors(selectClass(MismatchedNativeTypeTestCase.class))
                .execute()
                .testEvents()
                .assertStatistics(stats -> stats.failed(1))
                .assertThatEvents()
                .haveExactly(1, event(finishedWithFailure(
                        instanceOf(ParameterResolutionException.class),
                        message(msg -> msg.contains("java.lang.String") && msg.contains(TestNativeDriver.class.getName())))));
    }

    @ExtendWith(NavarchExtension.class)
    static class MismatchedNativeTypeTestCase {
        @Test
        void testMethod(DriverFleet<String> driverFleet) {
        }
    }

    @Test
    void driverDoesNotSurviveIntoTheNextTest() {
        EngineTestKit.engine("junit-jupiter")
                .selectors(selectClass(DriverDoesNotSurviveIntoNextTestCase.class))
                .execute()
                .testEvents()
                .assertStatistics(stats -> stats.succeeded(2).failed(0));
    }

    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @ExtendWith(NavarchExtension.class)
    static class DriverDoesNotSurviveIntoNextTestCase {
        @Test
        @Order(1)
        void firstTest(@ForDriver("special") DriverFleet<TestNativeDriver> driverFleet) {
            assertThat(driverFleet.getDriver("special")).isNotNull();
        }

        @Test
        @Order(2)
        void secondTest(DriverFleet<TestNativeDriver> driverFleet) {
            assertThat(driverFleet.getDriver("special")).isNull();
        }
    }
}
