package io.github.matsfeddern.navarch.core.driver;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DriverTest {

    @Test
    void recreateClosesTheOldInstanceAndCreatesANewOneWithGivenConfig() {
        Driver<TestNativeDriver> driver = new Driver<>(new TestNativeDriver("original"), new TestConnector());

        Driver<TestNativeDriver> result = driver.recreate(TestDriverConfigs.labeled("recreated"));

        assertThat(result).isNotSameAs(driver);
        String label = result.execute(TestNativeDriver::label).join();
        assertThat(label).isEqualTo("recreated");
    }
}
