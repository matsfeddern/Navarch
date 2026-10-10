package io.github.matsfeddern.navarch.connector.selenium;

import io.github.matsfeddern.navarch.core.driver.DriverFleet;
import io.github.matsfeddern.navarch.junit5.ForDriver;
import io.github.matsfeddern.navarch.junit5.NavarchExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(NavarchExtension.class)
class SeleniumConnectorTest {

    private static final String PAGE_WITH_BUTTON = "data:text/html,"
            + "<title>ok</title>"
            + "<button id='btn' onclick=\"document.title='clicked'\">Click me</button>";

    @Test
    void forDriverAnnotationResolvesNamedFirefoxDriver(
            @ForDriver("firefox-headless") DriverFleet<WebDriver> driverFleet) {
        String title = driverFleet.getDriver("firefox-headless").execute(SeleniumConnectorTest::titleOfBlankPage).join();

        assertThat(title).isEqualTo("ok");
    }

    @Test
    void forDriversExecutesAcrossFirefoxAndChromeAndClicksEachIndependently(
            @ForDriver("firefox-headless") @ForDriver("chrome-headless") DriverFleet<WebDriver> driverFleet) {
        driverFleet.executeAll(SeleniumConnectorTest::openPageWithButton).join();

        List<String> buttonLabels = driverFleet.executeAll(SeleniumConnectorTest::buttonText).join().toList();
        assertThat(buttonLabels).containsExactly("Click me", "Click me");

        driverFleet.executeAll(SeleniumConnectorTest::clickButton).join();

        List<String> titlesAfterClick = driverFleet.executeAll(WebDriver::getTitle).join().toList();
        assertThat(titlesAfterClick).containsExactly("clicked", "clicked");
    }

    private static String titleOfBlankPage(WebDriver nativeDriver) {
        nativeDriver.get("data:text/html,<title>ok</title>");
        return nativeDriver.getTitle();
    }

    private static Void openPageWithButton(WebDriver nativeDriver) {
        nativeDriver.get(PAGE_WITH_BUTTON);
        return null;
    }

    private static String buttonText(WebDriver nativeDriver) {
        return nativeDriver.findElement(By.id("btn")).getText();
    }

    private static Void clickButton(WebDriver nativeDriver) {
        nativeDriver.findElement(By.id("btn")).click();
        return null;
    }
}
