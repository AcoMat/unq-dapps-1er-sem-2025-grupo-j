package unq.dapp.grupoj.soccergenius.services.external.whoscored;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;
import unq.dapp.grupoj.soccergenius.exceptions.ScrappingException;
import unq.dapp.grupoj.soccergenius.exceptions.TeamNotFoundException;
import unq.dapp.grupoj.soccergenius.model.Team;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class TeamScrapingService extends WebScrapingService {

    public List<String> getPlayersIdsFromTeam(String teamName, String country) {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = createWebDriver();

        try {
            String urlTmp = BASE_URL + "/search/?t=" + teamName;
            driver.navigate().to(urlTmp);

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

            if( teamUrl == null || teamUrl.isEmpty()) {
                throw new TeamNotFoundException("Team " + teamName + " not found in country " + country);
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

    public int getCurrentPositionOnLeague(int teamId){
        String url = BASE_URL + "/regions/206/tournaments/4/seasons/6960/españa-laliga";

        WebDriverManager.chromedriver().setup();
        WebDriver driver = createWebDriver();

        try {
            driver.navigate().to(url);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            WebElement tableBody = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("standings-15375-content")));

            List<WebElement> rows = tableBody.findElements(By.tagName("tr"));
            for (WebElement row : rows) {
                String currentRowTeamIdStr = row.getAttribute("data-team-id");

                if (currentRowTeamIdStr != null && !currentRowTeamIdStr.isEmpty()) {
                    try {
                        int currentRowTeamId = Integer.parseInt(currentRowTeamIdStr);

                        if (currentRowTeamId == teamId) {
                            WebElement firstCell = row.findElement(By.xpath("./td[1]"));
                            WebElement positionSpan = firstCell.findElement(By.tagName("span"));

                            String positionText = positionSpan.getText().trim();
                            if (!positionText.isEmpty()) {
                                return Integer.parseInt(positionText);
                            } else {
                                throw new TeamNotFoundException("Team ID " + teamId + " has no position in league standings.");
                            }
                        }
                    } catch (Exception e) {
                        throw new ScrappingException("Error parsing team ID from row: " + e.getMessage());
                    }
                }
            }
            throw new TeamNotFoundException("Team ID " + teamId + " not found in league standings.");
        } catch (Exception e) {
            throw new ScrappingException("Error scraping current position on league: " + e.getMessage());
        } finally {
            driver.quit();
        }
    }

    public double getCurrentRankingOfTeam(int teamId) {
        String url = BASE_URL + "/teams/" + teamId;

        WebDriverManager.chromedriver().setup();
        WebDriver driver = createWebDriver();

        try{
            driver.navigate().to(url);

            // Wait for the table to load
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            String ratingXPath = "//tbody[@id='top-team-stats-summary-content']/tr[last()]/td[@class='rating']/strong";

            // Wait for the rating element to be visible
            WebElement ratingStrongElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(ratingXPath)));

            String ratingText = ratingStrongElement.getText();

            // Handle cases where rating might not be a valid number (e.g., "-")
            if (ratingText.trim().equals("-") || ratingText.trim().isEmpty()) {
                throw new ScrappingException("Rating not available or invalid for team ID: " + teamId);
            }

            return Double.parseDouble(ratingText);

        } catch (NumberFormatException e) {
            throw new ScrappingException("Error parsing team rating: " + e.getMessage());
        } catch (Exception e) {
            throw new ScrappingException("Error scraping current ranking of team: " + e.getMessage());
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

        String url = BASE_URL + "/teams/" + teamId;
        driver.navigate().to(url);

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

        String url = BASE_URL + "/players/" + playerId;
        try {
            driver.navigate().to(url);

            WebElement teamElement = driver.findElement(By.className("team-link"));
            String href = teamElement.getAttribute("href");

            assert href != null;
            String teamId = href.replaceFirst("/teams/(\\d+)(?:/.*)?", "$1");
            return Integer.parseInt(teamId);
        } catch (Exception e) {
            throw new ScrappingException("Error scraping team data: " + e.getMessage());
        } finally {
            driver.quit();
        }
    }

}
