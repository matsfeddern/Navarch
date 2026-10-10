package io.github.matsfeddern.navarch.connector.selenium;

import io.github.matsfeddern.navarch.core.connector.DriverConfig;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;

public final class SeleniumDriverConfigs {

    private SeleniumDriverConfigs() {
    }

    public static DriverConfig firefoxHeadless() {
        return new SeleniumDriverConfig(new FirefoxOptions().addArguments("-headless"));
    }

    public static DriverConfig chromeHeadless() {
        return new SeleniumDriverConfig(new ChromeOptions().addArguments("--headless=new"));
    }
}
