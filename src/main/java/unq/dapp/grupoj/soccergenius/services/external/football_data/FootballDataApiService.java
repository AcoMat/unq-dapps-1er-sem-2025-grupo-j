package unq.dapp.grupoj.soccergenius.services.external.football_data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import unq.dapp.grupoj.soccergenius.exceptions.FootballDataApiException;
import unq.dapp.grupoj.soccergenius.model.dtos.external.football_data.FootballDataMatchsDto;
import unq.dapp.grupoj.soccergenius.util.FootballDataIdsUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class FootballDataApiService {

    private final RestTemplate restTemplate;

    @Value("${FOOTBALL_DATA_API_KEY}")
    private String apiKey;

    @Value("${football-data.api.base-url:http://api.football-data.org/v4}")
    private String baseUrl;

    @Autowired
    public FootballDataApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public FootballDataMatchsDto getLastXMatchesFromTeam(int teamId, int limit) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(1);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String dateFrom = startDate.format(formatter);
        String dateTo = endDate.format(formatter);

        String url = baseUrl + "/teams/" + teamId + "/matches?dateFrom=" + dateFrom + "&dateTo=" + dateTo + "&limit=" + limit + "&status=FINISHED";

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth-Token", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<FootballDataMatchsDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    FootballDataMatchsDto.class
            );
            return response.getBody();
        } catch (Exception e) {
            throw new FootballDataApiException("Error fetching last matches for team with ID: " + teamId + ".");
        }
    }

    public FootballDataMatchsDto getUpcomingMatchesFromTeam(String teamName) {
        int footballDataId = FootballDataIdsUtil.getTeamIdFromTeamName(teamName);

        String url = "https://api.football-data.org/v4/teams/id/matches?status=SCHEDULED".replace("id", String.valueOf(footballDataId));

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth-Token", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<FootballDataMatchsDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    FootballDataMatchsDto.class
            );

            if (response.getStatusCode().isError()) {
                throw new FootballDataApiException("Error fetching upcoming matches for team: " + teamName);
            }
            return response.getBody();
        } catch (Exception e) {
            throw new FootballDataApiException("Error fetching upcoming matches for team: " + teamName + ".");
        }
    }
}
