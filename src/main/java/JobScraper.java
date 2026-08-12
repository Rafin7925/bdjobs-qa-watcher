import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.io.FileInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.time.Duration;
import java.util.List;
import java.util.Properties;

public class JobScraper {

    static String DB_URL = "jdbc:mysql://localhost:3306/job_watcher";
    static String DB_USER = "root";
    static String DB_PASSWORD;
    static String DISCORD_WEBHOOK;

    public static void main(String[] args) throws Exception {
        // Load config
        Properties config = new Properties();
        config.load(new FileInputStream("config.properties"));
        DB_PASSWORD = config.getProperty("db.password");
        DISCORD_WEBHOOK = config.getProperty("discord.webhook");

        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();
        JavascriptExecutor js = (JavascriptExecutor) driver;

        Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        System.out.println("Connected to database.");

        driver.get("https://bdjobs.com");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        WebElement searchBox = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.name("txtSearch"))
        );
        searchBox.click();
        searchBox.sendKeys("software quality assurance engineer");

        js.executeScript(
                "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));" +
                        "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
                searchBox
        );

        Thread.sleep(1000);
        searchBox.submit();

        wait.until(ExpectedConditions.urlContains("txtsearch"));
        Thread.sleep(3000);

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("p[data-testid='job-title']")));

        List<WebElement> titleElements = driver.findElements(By.cssSelector("p[data-testid='job-title']"));
        int count = titleElements.size();
        System.out.println("Found " + count + " total listings, filtering for QA-related roles...");

        String[] keywords = {"qa", "quality assurance", "software tester", "test engineer", "sqa"};
        int newJobsFound = 0;

        String insertSql = "INSERT IGNORE INTO jobs (title, company, link) VALUES (?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(insertSql);

        for (int i = 0; i < count; i++) {
            List<WebElement> currentTitles = driver.findElements(By.cssSelector("p[data-testid='job-title']"));
            WebElement titleEl = currentTitles.get(i);

            String title = titleEl.getText();
            String titleLower = title.toLowerCase();

            boolean matches = false;
            for (String kw : keywords) {
                if (titleLower.contains(kw)) {
                    matches = true;
                    break;
                }
            }

            if (!matches) continue;

            WebElement companyEl = titleEl.findElement(By.xpath("following-sibling::p"));
            String company = companyEl.getText();

            titleEl.click();
            Thread.sleep(1500);
            String link = driver.getCurrentUrl();

            stmt.setString(1, title);
            stmt.setString(2, company);
            stmt.setString(3, link);
            int rowsInserted = stmt.executeUpdate();

            if (rowsInserted > 0) {
                System.out.println("NEW: " + title + " | " + company + " | " + link);
                newJobsFound++;
                sendDiscordAlert(title, company, link);
            } else {
                System.out.println("Already seen: " + title);
            }

            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("p[data-testid='job-title']")));
        }

        System.out.println("New jobs added this run: " + newJobsFound);

        stmt.close();
        conn.close();
        driver.quit();
    }

    static void sendDiscordAlert(String title, String company, String link) {
        try {
            String message = "**New QA Job Found!**\n" + title + " at " + company + "\n" + link;
            String jsonBody = "{\"content\": \"" + message.replace("\"", "\\\"").replace("\n", "\\n") + "\"}";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(DISCORD_WEBHOOK))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Discord alert sent, status: " + response.statusCode());
        } catch (Exception e) {
            System.out.println("Failed to send Discord alert: " + e.getMessage());
        }
    }
}
