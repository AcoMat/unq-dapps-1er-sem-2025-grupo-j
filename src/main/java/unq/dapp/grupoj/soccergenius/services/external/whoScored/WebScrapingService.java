package unq.dapp.grupoj.soccergenius.services.external.whoScored;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;
import unq.dapp.grupoj.soccergenius.exceptions.ScrappingException;
import unq.dapp.grupoj.soccergenius.exceptions.TeamNotFoundException;
import unq.dapp.grupoj.soccergenius.model.Match;
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

    public Player scrapPlayerData(int playerId) {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = createWebDriver();

        String url = BASE_URL + "/players/" + playerId;
        try{

        driver.navigate().to(url);

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

        String heightText = driver.findElement(By.xpath("//span[contains(text(),'Altura:')]/parent::div")).getText();
        String playerHeight = heightText.replace("Altura:", "").strip();

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
        String url = BASE_URL + "/players/" + player.getId();

        WebDriverManager.chromedriver().setup();
        WebDriver driver = createWebDriver();

        try {
            driver.navigate().to(url);

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
        String url = BASE_URL + "/players/" + player.getId() + "/history";
        WebDriverManager.chromedriver().setup();
        WebDriver driver = createWebDriver();

        try{
            driver.navigate().to(url);
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
            String teamId = href.replaceAll(".*/teams/(\\d+)/.*", "$1");
            return Integer.parseInt(teamId);
        } catch (Exception e) {
            throw new ScrappingException("Error scraping team data: " + e.getMessage());
        } finally {
            driver.quit();
        }
    }

    public String findMatchLink(String teamName1, String teamName2){
        WebDriverManager.chromedriver().setup();
        WebDriver driver = createWebDriver();

        String url = BASE_URL + "/regions/206/tournaments/4/espa%C3%B1a-laliga";
        String matchUrl = null;
        try {
            driver.navigate().to(url);

            // Find all match fixtures
            List<WebElement> matches = driver.findElements(By.className("Match-module_match__XlKTY"));

            for (WebElement matchElement : matches) {
                // Find team names within this match fixture
                List<WebElement> teamNameElements = matchElement.findElements(By.className("Match-module_teamNameText__Dqv-G"));

                if (teamNameElements.size() == 2) { // Ensure there are exactly two team name elements
                    String actualTeam1 = teamNameElements.get(0).getText().trim();
                    String actualTeam2 = teamNameElements.get(1).getText().trim();

                    // Check if both provided team names are present in this match
                    // (Order might not matter, so check both combinations)
                    boolean team1Found = actualTeam1.equalsIgnoreCase(teamName1) || actualTeam2.equalsIgnoreCase(teamName1);
                    boolean team2Found = actualTeam1.equalsIgnoreCase(teamName2) || actualTeam2.equalsIgnoreCase(teamName2);

                    if (team1Found && team2Found) {
                        // If both teams are found, get the link of that match
                        WebElement scoreLinkElement = matchElement.findElement(By.className("Match-module_score__5Ghhj"));
                        matchUrl = scoreLinkElement.getAttribute("href");
                        break;
                    }
                }
            }
        } catch (Exception e) {
            // It's good practice to log the exception or handle it more specifically
            // e.printStackTrace();
            throw new ScrappingException("Error scraping match links: " + e.getMessage());
        } finally {
            driver.quit();
        }
        return matchUrl;
    }

    public List<Match> getPreviousMeetings(String teamName1, String teamName2) {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = createWebDriver();

        List<Match> matches = new ArrayList<>();
        String matchLink = findMatchLink(teamName1, teamName2);
        if (matchLink == null) {
            driver.quit();
            return matches;
        }
        try {
            driver.navigate().to(matchLink);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("previous-meetings-grid")));
            WebElement grid = driver.findElement(By.id("previous-meetings-grid"));
            List<WebElement> rows = grid.findElements(By.className("divtable-row"));
            for (WebElement row : rows) {
                String matchId = row.getAttribute("data-id");
                String date = extractDate(row);
                String[] teams = extractTeams(row);
                String homeTeam = teams[0];
                String awayTeam = teams[1];
                String[] scores = extractScores(row);
                String homeScore = scores[0];
                String awayScore = scores[1];
                String winner = extractWinner(row, homeTeam, awayTeam, homeScore, awayScore);
                matches.add(new Match(
                        matchId,
                        date,
                        homeTeam,
                        awayTeam,
                        winner,
                        homeScore,
                        awayScore
                ));
            }
            return matches;
        } catch (Exception e) {
            throw new ScrappingException("Error scraping previous meetings: " + e.getMessage());
        } finally {
            driver.quit();
        }
    }

    private String extractDate(WebElement row) {
        try {
            WebElement dateDiv = row.findElement(By.cssSelector(".date-long > div"));
            return dateDiv.getText();
        } catch (Exception e) {
            try {
                List<WebElement> dateParts = row.findElements(By.cssSelector(".date-stacked > div"));
                if (dateParts.size() == 2) {
                    return dateParts.get(0).getText() + " " + dateParts.get(1).getText();
                } else if (!dateParts.isEmpty()) {
                    return dateParts.getFirst().getText();
                }
            } catch (Exception ignored) {}
        }
        return "";
    }

    private String[] extractTeams(WebElement row) {
        String homeTeam = "";
        String awayTeam = "";
        try {
            homeTeam = row.findElement(By.cssSelector(".horizontal-match-display.team.home .team-link")).getText();
            awayTeam = row.findElement(By.cssSelector(".horizontal-match-display.team.away .team-link")).getText();
        } catch (Exception e) {
            try {
                List<WebElement> teamsStacked = row.findElements(By.cssSelector(".stacked-teams-display .team-link"));
                if (teamsStacked.size() >= 2) {
                    homeTeam = teamsStacked.get(0).getText();
                    awayTeam = teamsStacked.get(1).getText();
                }
            } catch (Exception ignored) {}
        }
        return new String[]{homeTeam, awayTeam};
    }

    private String[] extractScores(WebElement row) {
        String homeScore = "";
        String awayScore = "";
        try {
            WebElement resultEl = row.findElement(By.cssSelector(".result > a.horiz-match-link"));
            String[] scores = resultEl.getText().split(":");
            if (scores.length == 2) {
                homeScore = scores[0].trim();
                awayScore = scores[1].trim();
            }
        } catch (Exception e) {
            try {
                homeScore = row.findElement(By.cssSelector(".stacked-score-display .home-score")).getText();
                awayScore = row.findElement(By.cssSelector(".stacked-score-display .away-score")).getText();
            } catch (Exception ignored) {}
        }
        return new String[]{homeScore, awayScore};
    }

    private String extractWinner(WebElement row, String homeTeam, String awayTeam, String homeScore, String awayScore) {
        try {
            return row.findElement(By.cssSelector(".horizontal-match-display.team.home.winner .team-link")).getText();
        } catch (Exception eHomeWinner) {
            try {
                return row.findElement(By.cssSelector(".horizontal-match-display.team.away.winner .team-link")).getText();
            } catch (Exception eAwayWinner) {
                try {
                    return row.findElement(By.cssSelector(".stacked-teams-display .team.winner .team-link")).getText();
                } catch (Exception eStackedWinner) {
                    if (!homeScore.isEmpty() && !awayScore.isEmpty() && !homeTeam.isEmpty() && !awayTeam.isEmpty()) {
                        try {
                            int hS = Integer.parseInt(homeScore);
                            int aS = Integer.parseInt(awayScore);
                            if (hS > aS) {
                                return homeTeam;
                            } else if (aS > hS) {
                                return awayTeam;
                            }
                        } catch (NumberFormatException nfe) {
                            // Ignorar, devolver Draw
                        }
                    }
                }
            }
        }
        return "Draw";
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
                    } catch (NumberFormatException e) {
                        System.err.println("Could not parse team ID or position for a row: " + e.getMessage());
                    } catch (org.openqa.selenium.NoSuchElementException e) {
                        System.err.println("Could not find position element structure in a row for team ID " + currentRowTeamIdStr + ": " + e.getMessage());
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


