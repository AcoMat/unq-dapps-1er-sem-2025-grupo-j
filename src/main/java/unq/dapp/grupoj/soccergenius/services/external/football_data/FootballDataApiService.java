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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class FootballDataApiService {

    private final RestTemplate restTemplate;
    private final Map<Integer, Integer> whoScoredToFootballDataTeamIdMap;

    @Value("${FOOTBALL_DATA_API_KEY}")
    private String apiKey;

    @Value("${football-data.api.base-url:http://api.football-data.org/v4}")
    private String baseUrl;

    @Autowired
    public FootballDataApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.whoScoredToFootballDataTeamIdMap = TeamIdMappingUtil.getWhoScoredToFootballDataTeamIdMap();
    }

    /**
     * Converts a WhoScored team ID to the corresponding Football-data.org team ID
     * @param whoScoredTeamId Team ID from WhoScored
     * @return Corresponding team ID for Football-data.org API or null if no mapping exists
     */
    public Integer convertWhoScoredIdToFootballDataId(int whoScoredTeamId) {
        return whoScoredToFootballDataTeamIdMap.get(whoScoredTeamId);
    }

    public FootballDataMatchsDto getLastXMatchesFromTeam(int teamId, int limit) {
        Integer footballDataId = convertWhoScoredIdToFootballDataId(teamId);
        if (footballDataId == null) {
            throw new FootballDataApiException("No Football-data.org ID found for WhoScored team ID: " + teamId);
        }
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(1);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String dateFrom = startDate.format(formatter);
        String dateTo = endDate.format(formatter);

        String url = baseUrl + "/teams/" + footballDataId + "/matches?dateFrom=" + dateFrom + "&dateTo=" + dateTo + "&limit=" + limit + "&status=FINISHED";

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth-Token", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<FootballDataMatchsDto> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            FootballDataMatchsDto.class
        );

        return response.getBody();
    }

}
