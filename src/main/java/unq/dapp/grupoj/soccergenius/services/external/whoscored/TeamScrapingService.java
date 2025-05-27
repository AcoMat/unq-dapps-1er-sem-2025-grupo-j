package unq.dapp.grupoj.soccergenius.services.external.whoscored;

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
        WebDriver driver = null;
        try {
            String urlTmp = BASE_URL + "/search/?t=" + teamName;
            driver = setupDriverAndNavigate(urlTmp);

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
            if (driver != null) {
                driver.quit();
            }
        }
    }

    public int getCurrentPositionOnLeague(int teamId){
        String url = BASE_URL + "/regions/206/tournaments/4/seasons/6960/españa-laliga";
        WebDriver driver = null;
        try {
            driver = setupDriverAndNavigate(url);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            WebElement tableBody = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("standings-15375-content")));
            List<WebElement> rows = tableBody.findElements(By.tagName("tr"));

            for (WebElement row : rows) {
                Integer position = getPositionFromRowIfTeamMatches(row, teamId);
                if (position != null) {
                    return position;
                }
            }
            // Si el bucle se completa, el equipo no se encontró entre las filas con ID de equipo válidos y coincidentes
            throw new TeamNotFoundException("Team ID " + teamId + " not found in league standings after checking all rows.");
        } catch (TeamNotFoundException | ScrappingException e) { // Captura excepciones específicas del auxiliar o la final
            throw e; // Vuelve a lanzarlas ya que son específicas
        } catch (Exception e) { // Captura otras posibles excepciones de WebDriver (por ejemplo, tiempo de espera, elemento no encontrado para tableBody)
            throw new ScrappingException("Error scraping current position on league: " + e.getMessage());
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private Integer getPositionFromRowIfTeamMatches(WebElement row, int teamIdToFind) throws ScrappingException, TeamNotFoundException {
        String currentRowTeamIdStr = row.getAttribute("data-team-id");

        if (currentRowTeamIdStr != null && !currentRowTeamIdStr.isEmpty()) {
            int currentRowTeamId;
            try {
                currentRowTeamId = Integer.parseInt(currentRowTeamIdStr);
            } catch (NumberFormatException e) {
                throw new ScrappingException("Error parsing team ID from row attribute '" + currentRowTeamIdStr + "': " + e.getMessage());
            }

            if (currentRowTeamId == teamIdToFind) {
                WebElement firstCell = row.findElement(By.xpath("./td[1]"));
                WebElement positionSpan = firstCell.findElement(By.tagName("span"));
                String positionText = positionSpan.getText().trim();

                if (!positionText.isEmpty()) {
                    try {
                        return Integer.parseInt(positionText);
                    } catch (NumberFormatException e) {
                        throw new ScrappingException("Error parsing position text '" + positionText + "' from row: " + e.getMessage());
                    }
                } else {
                    // Esta excepción específica indica que se encontró el equipo, pero su posición no estaba listada.
                    throw new TeamNotFoundException("Team ID " + teamIdToFind + " found, but has no position text in league standings.");
                }
            }
        }
        return null; // La fila no corresponde a teamIdToFind o no tiene el atributo data-team-id
    }

    public double getCurrentRankingOfTeam(int teamId) {
        String url = BASE_URL + "/teams/" + teamId;
        WebDriver driver = null;
        try{
            driver = setupDriverAndNavigate(url);

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
            if (driver != null) {
                driver.quit();
            }
        }
    }

    public Team scrapTeamDataById(int teamId) {
        WebDriver driver = null;
        String teamName;
        String leagueName;
        String countryName;

        String url = BASE_URL + "/teams/" + teamId;
        try {
            driver = setupDriverAndNavigate(url);

            WebElement teamNameSpan = driver.findElement(By.className("team-header-name"));
            teamName = teamNameSpan.getText();

            WebElement leagueHref = driver.findElement(By.cssSelector("#breadcrumb-nav a"));
            leagueName = leagueHref.getText();

            WebElement countrySpan = driver.findElement(By.cssSelector(".iconize.iconize-icon-left"));
            countryName = countrySpan.getText();
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }

        return new Team(teamId, teamName, countryName, leagueName);
    }

    public int scrapActualTeamFromPlayer(int playerId) {
        WebDriver driver = null;
        String url = BASE_URL + "/players/" + playerId;
        try {
            driver = setupDriverAndNavigate(url);

            WebElement teamElement = driver.findElement(By.className("team-link"));
            String href = teamElement.getAttribute("href");

            assert href != null;
            String teamId = href.replaceFirst("/teams/(\\d+)(?:/.*)?", "$1");
            return Integer.parseInt(teamId);
        } catch (Exception e) {
            throw new ScrappingException("Error scraping team data: " + e.getMessage());
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

}
