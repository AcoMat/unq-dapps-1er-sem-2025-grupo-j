package unq.dapp.grupoj.soccergenius.services.external.whoscored;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;
import unq.dapp.grupoj.soccergenius.exceptions.ScrappingException;
import unq.dapp.grupoj.soccergenius.model.Match;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class MatchScrapingService extends WebScrapingService {


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
            } catch (Exception ignored) {
                // Ignorar, devolver cadena vacía
            }
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
            } catch (Exception ignored) {
                // Ignorar, devolver cadenas vacías
            }
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
            } catch (Exception ignored) {
                // Ignorar, devolver cadenas vacías
            }
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
}
