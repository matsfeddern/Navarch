package io.github.matsfeddern.navarch.connector.selenium;

import io.github.matsfeddern.navarch.core.connector.Connector;
import io.github.matsfeddern.navarch.core.connector.ConnectorLookup;
import io.github.matsfeddern.navarch.core.connector.DriverConfig;
import io.github.matsfeddern.navarch.core.connector.DriverConfigProviderLookup;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;

import static org.assertj.core.api.Assertions.assertThat;

class SeleniumConnectorSpiTest {

    @Test
    void connectorIsDiscoveredViaServiceLoader() {
        Connector<?> connector = ConnectorLookup.activeConnector();

        assertThat(connector).isInstanceOf(SeleniumConnector.class);
        assertThat(connector.nativeType()).isEqualTo(WebDriver.class);
    }

    @Test
    void driverConfigProviderIsDiscoveredViaServiceLoader() {
        DriverConfig config = DriverConfigProviderLookup.activeProvider().get("firefox-headless");

        assertThat(config).isInstanceOf(SeleniumDriverConfig.class);
    }
}
