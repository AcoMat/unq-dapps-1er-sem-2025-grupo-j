package unq.dapp.grupoj.soccergenius.services.external.whoscored;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class WebScrapingService {
    protected static final String BASE_URL = "https://es.whoscored.com";
    protected static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/114.0.0.0 Safari/537.36";

    public WebDriver setupDriverAndNavigate(String url) {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = createWebDriver();
        driver.get(url);
        return driver;
    }

    protected WebDriver createWebDriver() {
        ChromeOptions options = new ChromeOptions();

        String chromeOpts = System.getenv("CHROME_OPTS");
        if (chromeOpts != null && !chromeOpts.isEmpty()) {
            for (String opt : chromeOpts.split(" ")) {
                options.addArguments(opt);
            }
        } else {
            options.addArguments("--headless=new");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1280,800");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
        }

        options.addArguments("user-agent=" + USER_AGENT);
        return new ChromeDriver(options);
    }
}

