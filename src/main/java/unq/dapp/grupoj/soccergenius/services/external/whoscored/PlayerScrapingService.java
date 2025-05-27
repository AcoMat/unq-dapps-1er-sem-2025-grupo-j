package unq.dapp.grupoj.soccergenius.services.external.whoscored;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;
import unq.dapp.grupoj.soccergenius.exceptions.ScrappingException;
import unq.dapp.grupoj.soccergenius.model.player.Player;
import unq.dapp.grupoj.soccergenius.model.player.summary.CurrentParticipationsSummary;
import unq.dapp.grupoj.soccergenius.model.player.summary.HistoricalParticipationsSummary;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class PlayerScrapingService extends WebScrapingService{

    private static final String PLAYERS_URL_SUFFIX = "/players/";

    private double extractRatingFromPage(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("top-player-stats-summary-grid")));

        WebElement totalRow = driver.findElement(By.xpath("//table[@id='top-player-stats-summary-grid']/tbody/tr[last()]"));
        WebElement ratingCell = totalRow.findElement(By.xpath("./td[@class='rating']/strong"));
        String ratingText = ratingCell.getText();
        return Double.parseDouble(ratingText);
    }

    public Player scrapPlayerData(int playerId) {
        String url = BASE_URL + PLAYERS_URL_SUFFIX + playerId;
        WebDriver driver = null;
        try{
            driver = setupDriverAndNavigate(url);

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
            if (driver != null) {
                driver.quit();
            }
        }
    }

    public CurrentParticipationsSummary getCurrentParticipationInfo(Player player) {
        String url = BASE_URL + PLAYERS_URL_SUFFIX + player.getId();
        WebDriver driver = null;
        try {
            driver = setupDriverAndNavigate(url);
            double rating = extractRatingFromPage(driver);
            return new CurrentParticipationsSummary(player, rating);
        } catch (Exception e) {
            throw new ScrappingException("Error scraping player participation info: " + e.getMessage());
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    public HistoricalParticipationsSummary getHistoryInfo(Player player) {
        String url = BASE_URL + PLAYERS_URL_SUFFIX + player.getId() + "/history";
        WebDriver driver = null;
        try{
            driver = setupDriverAndNavigate(url);
            double rating = extractRatingFromPage(driver);
            return new HistoricalParticipationsSummary(player, rating);
        } catch (Exception e) {
            throw new ScrappingException("Error scraping player history info: " + e.getMessage());
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }
}
