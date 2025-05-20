package unq.dapp.grupoj.soccergenius.services.external.whoScored;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.lang3.NotImplementedException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;
import unq.dapp.grupoj.soccergenius.exceptions.ScrappingException;
import unq.dapp.grupoj.soccergenius.model.Team;
import unq.dapp.grupoj.soccergenius.model.player.Player;
import unq.dapp.grupoj.soccergenius.model.player.summary.CurrentParticipationsSummary;
import unq.dapp.grupoj.soccergenius.model.player.summary.HistoricalParticipationsSummary;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class WebScrapingService {
    private static final String BASE_URL = "https://es.whoscored.com";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36";

    public List<String> getPlayersIdsFromTeam(String teamName, String country) {
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
            List<WebElement> playerRows = playerList.findElements(By.xpath("./*"));

            List<String> players = new ArrayList<>();
            for (WebElement player : playerRows) {
                WebElement playerLink = player.findElement(By.className("player-link"));
                players.add(playerLink.findElement(By.xpath("./*[2]")).getText());
            }

            return players;
        } finally {
            driver.quit();
        }
    }

    public Player scrapPlayerData(int playerId) {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = createWebDriver();

        String URL = BASE_URL + "/players/" + playerId;
        try{

        driver.navigate().to(URL);

        List<WebElement> errorMessages = driver.findElements(
                By.xpath("//*[contains(text(), 'The page you requested does not exist')]"));
        if (!errorMessages.isEmpty()) {
            // Page doesn't exist, return null
            return null;
        }

        WebElement nameContainer = driver.findElement(By.xpath("//span[contains(text(),'Nombre: ')]/parent::div"));
        String playerName = nameContainer.getText().replace("Nombre: ", "").trim();

        WebElement ageElement = driver.findElement(By.xpath("//span[contains(text(),'Edad: ')]/parent::div"));
        String ageText = ageElement.getText().replace("Edad: ", "").trim();
        int playerAge = Integer.parseInt(ageText.split(" ")[0]);

        // Extract nationality - get just the country name without the icon
        WebElement nationalityElement = driver.findElement(By.xpath("//span[contains(text(),'Nacionalidad:')]/parent::div"));
        WebElement countryElement = nationalityElement.findElement(By.className("iconize-icon-left"));
        String playerNationality = countryElement.getText().split(" ")[0].trim();

        // Extract positions
        List<String> playerPositions = new ArrayList<>();
        WebElement positionsDiv = driver.findElement(By.xpath("//span[contains(text(),'Posiciones: ')]/parent::div"));
        List<WebElement> positionSpans = positionsDiv.findElements(By.xpath(".//span[@style='display: inline-block;']"));
        for (WebElement position : positionSpans) {
            playerPositions.add(position.getText().trim());
        }

        String height_text = driver.findElement(By.xpath("//span[contains(text(),'Altura:')]/parent::div")).getText();
        String playerHeight = height_text.replace("Altura:", "").strip();

        return new Player(
                playerId,
                playerName,
                playerAge,
                playerNationality,
                playerHeight,
                playerPositions
        );
        } catch (Exception e) {
            throw new ScrappingException("Error scraping player data: " + e.getMessage());
        } finally {
            driver.quit();
        }
    }

    public CurrentParticipationsSummary getCurrentParticipationInfo(Player player) {
        String URL = BASE_URL + "/players/" + player.getId();

        WebDriverManager.chromedriver().setup();
        WebDriver driver = createWebDriver();

        try {
            driver.navigate().to(URL);

            // Wait for the table to load
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("top-player-stats-summary-grid")));

            // Get the last row (Total/Promedio)
            WebElement totalRow = driver.findElement(By.xpath("//table[@id='top-player-stats-summary-grid']/tbody/tr[last()]"));

            // Get the rating cell (last cell in the row)
            WebElement ratingCell = totalRow.findElement(By.xpath("./td[@class='rating']/strong"));
            String ratingText = ratingCell.getText();

            double rating = Double.parseDouble(ratingText);

            return new CurrentParticipationsSummary(player, rating);
        } catch (Exception e) {
            throw new ScrappingException("Error scraping player participation info: " + e.getMessage());
        } finally {
            driver.quit();
        }
    }

    public HistoricalParticipationsSummary getHistoryInfo(Player player) {
        String URL = BASE_URL + "/players/" + player.getId() + "/history";
        WebDriverManager.chromedriver().setup();
        WebDriver driver = createWebDriver();

        try{
            driver.navigate().to(URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("top-player-stats-summary-grid")));

            // Get the last row (Total/Promedio)
            WebElement totalRow = driver.findElement(By.xpath("//table[@id='top-player-stats-summary-grid']/tbody/tr[last()]"));

            // Get the rating cell (last cell in the row)
            WebElement ratingCell = totalRow.findElement(By.xpath("./td[@class='rating']/strong"));
            String ratingText = ratingCell.getText();

            double rating = Double.parseDouble(ratingText);

            return new HistoricalParticipationsSummary(player, rating);
        } catch (Exception e) {
            throw new ScrappingException("Error scraping player history info: " + e.getMessage());
        } finally {
            driver.quit();
        }
    }


    public Team scrapTeamDataById(int teamId) {
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

        return new Team(teamId, teamName, countryName, leagueName);
    }

    public int scrapActualTeamFromPlayer(int playerId) {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = createWebDriver();

        String teamName;
        String leagueName;
        String countryName;

        String URL = BASE_URL + "/players/" + playerId;
        try {
            driver.navigate().to(URL);

            WebElement team_element = driver.findElement(By.className("team-link"));
            String href = team_element.getAttribute("href");

            String teamId = href.replaceAll(".*/teams/(\\d+)/.*", "$1");
            return Integer.parseInt(teamId);
        } catch (Exception e) {
            throw new ScrappingException("Error scraping team data: " + e.getMessage());
        } finally {
            driver.quit();
        }
    }

    public List<String> getLastEncountersBetween(int firstTeam, int SecTeam){
        WebDriverManager.chromedriver().setup();
        WebDriver driver = createWebDriver();

        String URL = BASE_URL + "/players/" + firstTeam + "/" + SecTeam;
        try{

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            driver.quit();
        }

        throw new NotImplementedException();
    }

    public int getCurrentPositionOnLeague(int teamId){
        throw new NotImplementedException();
    }

    public int getCurrentRankingOfTeam(int teamId) {
        throw new NotImplementedException();
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
