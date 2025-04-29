package unq.dapp.grupoj.soccergenius.services.scrapping;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;
import unq.dapp.grupoj.soccergenius.model.Team;
import unq.dapp.grupoj.soccergenius.model.player.Player;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class WebScrapingService {
    private static final String BASE_URL        = "https://es.whoscored.com";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36";

    public List<Player> scrapeWebsite(String teamName, String country) {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = createWebDriver();

        List<Player> scrapedData = new ArrayList<>();

        try {
            String urlTMP = BASE_URL + "/search/?t=" + teamName;
            driver.navigate().to(urlTMP);

            WebElement divResult = driver.findElement(By.className("search-result"));
            WebElement teamsTable = divResult.findElement(By.xpath("./table[1]"));
            WebElement tbody = teamsTable.findElement(By.tagName("tbody"));
            List<WebElement> teamsList = tbody.findElements(By.xpath("./tr[position()>1]"));

            String teamUrl = "";
            for (WebElement team : teamsList) {
                WebElement linkEquipo = team.findElement(By.xpath("./td[1]/a"));
                String teamNameSource = linkEquipo.getText();

                WebElement spanPais = team.findElement(By.xpath("./td[2]/span"));
                String countryName = spanPais.getText();

                if (teamNameSource.toLowerCase().contains(teamName) && countryName.equalsIgnoreCase(country)){
                    teamUrl = linkEquipo.getAttribute("href");
                    break;
                }
            }

            driver.navigate().to(teamUrl);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("player-table-statistics-body")));
            WebElement playerList = driver.findElement(By.id("player-table-statistics-body"));
            List<WebElement> playerList2 = playerList.findElements(By.xpath("./*"));

            for (WebElement player : playerList2) {
                String name        = player.findElement(By.className("player-link")).findElement(By.xpath("./*[2]")).getText();
                String gamesPlayed = player.findElement(By.xpath("./*[5]")).getText();
                String goals       = player.findElement(By.className("goal")).getText().equals("-") ? "0" : player.findElement(By.className("goal")).getText();
                String assists     = player.findElement(By.className("assistTotal")).getText();
                String rating      = player.findElement(By.xpath("./*[15]")).getText();

                //Player p = new Player(name,gamesPlayed,goals,assists,rating);
                Player p = new Player();
                scrapedData.add(p);
            }
        } finally {
            driver.quit();
        }
        return scrapedData;
    }

    public Player scrapPlayerData(int playerId) {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = createWebDriver();

        String URL = BASE_URL + "/player/" + playerId;
        driver.navigate().to(URL);

        WebElement spanElement = driver.findElement(By.xpath("//span[text()='Nombre: ']"));
        WebElement playerNameDiv = spanElement.findElement(By.xpath("./..")); // Navigate to the parent div
        String playerName = playerNameDiv.getText().replace("Nombre: ", "").trim();
        //TODO: WIP

        return new Player();
    }

    public Team scrapTeamData(int teamId) {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = createWebDriver();

        String teamName;
        String leagueName;
        String countryName;

        String URL = BASE_URL + "/teams/" + teamId;
        driver.navigate().to(URL);

        WebElement teamNameSpan = driver.findElement(By.className("team-header-name"));
        teamName = teamNameSpan.getText();

        WebElement leagueHref = driver.findElement(By.cssSelector("#breadcrumb-nav a"));
        leagueName = leagueHref.getText();

        WebElement countrySpan = driver.findElement(By.cssSelector(".iconize.iconize-icon-left"));
        countryName = countrySpan.getText();

        driver.quit();

        return new Team(teamId, teamName, leagueName, countryName);
    }


    private WebDriver createWebDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new"); // Modo headless moderno
        options.addArguments("--disable-gpu"); // Necesario a veces en headless
        options.addArguments("--window-size=1920,1080"); // Definir tamaño puede ayudar
        options.addArguments("--no-sandbox"); // A veces necesario en entornos Linux/Docker
        options.addArguments("--disable-dev-shm-usage"); // A veces necesario en entornos Linux/Docker
        options.addArguments("user-agent=" + USER_AGENT); // Usar constante
        return new ChromeDriver(options);
    }
}
