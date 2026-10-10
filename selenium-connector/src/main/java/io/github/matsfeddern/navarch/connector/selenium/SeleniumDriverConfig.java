package io.github.matsfeddern.navarch.connector.selenium;

import io.github.matsfeddern.navarch.core.connector.DriverConfig;
import org.openqa.selenium.Capabilities;

import java.net.URL;

public class SeleniumDriverConfig implements DriverConfig {
    private final Capabilities capabilities;
    private final URL remoteAddress;

    public SeleniumDriverConfig(Capabilities capabilities) {
        this(capabilities, null);
    }

    public SeleniumDriverConfig(Capabilities capabilities, URL remoteAddress) {
        this.capabilities = capabilities;
        this.remoteAddress = remoteAddress;
    }

    public Capabilities capabilities() {
        return capabilities;
    }

    public URL remoteAddress() {
        return remoteAddress;
    }
}
