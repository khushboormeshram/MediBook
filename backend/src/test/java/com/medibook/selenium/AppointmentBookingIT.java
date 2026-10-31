package com.medibook.selenium;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.net.HttpURLConnection;
import java.net.URI;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Browser-level checks against a running MediBook frontend and backend. */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AppointmentBookingIT {
    private static final By LOGIN_BUTTON = By.xpath("//header//button[normalize-space()='Log in']");
    private static final By EMAIL_INPUT = By.cssSelector(".auth-modal input[name='email']");
    private static final By PASSWORD_INPUT = By.cssSelector(".auth-modal input[name='password']");
    private static final By DOCTOR_CARDS = By.cssSelector(".doctor-card");
    private static final By DATE_INPUT = By.cssSelector(".doctor-modal .slot-heading input[type='date']");
    private static final By SLOT_BUTTONS = By.cssSelector(".doctor-modal .slot-grid .slot-btn");

    private WebDriver driver;
    private WebDriverWait wait;
    private String baseUrl;

    @BeforeAll
    void startBrowser() throws InterruptedException {
        baseUrl = System.getProperty("medibook.baseUrl", "http://localhost:5173");
        waitForApplication();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--window-size=1440,1100", "--disable-gpu");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        wait.pollingEvery(Duration.ofMillis(200));
    }

    private void waitForApplication() throws InterruptedException {
        long deadline = System.nanoTime() + Duration.ofSeconds(45).toNanos();
        IOException lastError = null;
        while (System.nanoTime() < deadline) {
            try {
                HttpURLConnection connection = (HttpURLConnection) URI.create(baseUrl).toURL().openConnection();
                connection.setConnectTimeout(1500);
                connection.setReadTimeout(1500);
                connection.setRequestMethod("GET");
                int status = connection.getResponseCode();
                connection.disconnect();
                if (status >= 200 && status < 500) {
                    return;
                }
            } catch (IOException exception) {
                lastError = exception;
            }
            Thread.sleep(1000);
        }
        throw new IllegalStateException("MediBook frontend is not reachable at " + baseUrl
                + ". Start the application with `docker compose up --build -d` from the project root, "
                + "or configure -Dmedibook.baseUrl for your running frontend."
                + (lastError == null ? "" : " Last connection error: " + lastError.getMessage()), lastError);
    }

    @BeforeEach
    void openCleanApplication() {
        driver.get(baseUrl);
        ((JavascriptExecutor) driver).executeScript("window.localStorage.clear(); window.sessionStorage.clear();");
        driver.navigate().refresh();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("header")));
    }

    @AfterAll
    void closeBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void patientCanLogIn() {
        logInAsDevelopmentPatient();
        assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("header a[href='/dashboard']"))).getText().contains("My appointments"));
    }

    @Test
    void patientCanBookAnAvailableAppointment() {
        logInAsDevelopmentPatient();
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("header a[href='/doctors']"))).click();
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(DOCTOR_CARDS, 0));

        String bookedDoctor = findDoctorWithAvailableSlotAndBook();
        assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".toast")))
                .getText().contains("Appointment booked"), "The UI should confirm the booking");

        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("header a[href='/dashboard']"))).click();
        wait.until(driver -> driver.findElements(By.cssSelector(".appointment-card")).stream()
                .anyMatch(card -> card.getText().contains(bookedDoctor)));
    }

    private void logInAsDevelopmentPatient() {
        wait.until(ExpectedConditions.elementToBeClickable(LOGIN_BUTTON)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_INPUT)).sendKeys(
                System.getProperty("medibook.patientEmail", "patient@medibook.local"));
        driver.findElement(PASSWORD_INPUT).sendKeys(System.getProperty("medibook.patientPassword", "password"));
        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".auth-modal form .auth-submit"))).click();
        By dashboardLink = By.cssSelector("header a[href='/dashboard']");
        wait.until(d -> !d.findElements(dashboardLink).isEmpty()
                || !d.findElements(By.cssSelector(".toast")).isEmpty());
        if (driver.findElements(dashboardLink).isEmpty()) {
            String message = driver.findElements(By.cssSelector(".toast")).stream()
                    .findFirst().map(WebElement::getText).orElse("No message was displayed");
            throw new AssertionError("Patient login was not accepted: " + message);
        }
    }

    private String findDoctorWithAvailableSlotAndBook() {
        List<WebElement> cards = driver.findElements(DOCTOR_CARDS);
        for (int cardIndex = 0; cardIndex < cards.size(); cardIndex++) {
            WebElement card = driver.findElements(DOCTOR_CARDS).get(cardIndex);
            String doctorName = card.findElement(By.cssSelector(".doctor-card-info h3")).getText();
            card.findElement(By.xpath(".//button[normalize-space()='View profile']")).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".doctor-modal")));

            LocalDate firstDay = LocalDate.now().plusDays(1);
            for (int offset = 0; offset < 14; offset++) {
                LocalDate date = firstDay.plusDays(offset);
                if (offset > 0) {
                    changeAppointmentDate(date.toString());
                }
                waitForAvailabilityResponse();
                List<WebElement> slots = driver.findElements(SLOT_BUTTONS);
                if (!slots.isEmpty()) {
                    slots.get(0).click();
                    wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".doctor-modal")));
                    return doctorName;
                }
            }

            driver.findElement(By.cssSelector(".doctor-modal button[aria-label='Close']")).click();
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".doctor-modal")));
        }
        throw new AssertionError("No doctor had an available slot during the next 14 days");
    }

    private void changeAppointmentDate(String date) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(DATE_INPUT));
        ((JavascriptExecutor) driver).executeScript("""
                const input = arguments[0];
                const setter = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value').set;
                setter.call(input, arguments[1]);
                input.dispatchEvent(new Event('input', { bubbles: true }));
                input.dispatchEvent(new Event('change', { bubbles: true }));
                """, input, date);
        wait.until(driver -> date.equals(driver.findElement(DATE_INPUT).getDomProperty("value")));
    }

    private void waitForAvailabilityResponse() {
        wait.until(driver -> {
            return !driver.findElements(SLOT_BUTTONS).isEmpty()
                    || !driver.findElements(By.cssSelector(".doctor-modal .slots-empty:not([role='status'])")).isEmpty();
        });
    }
}
