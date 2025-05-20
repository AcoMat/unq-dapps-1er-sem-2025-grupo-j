package unq.dapp.grupoj.soccergenius.services.external.football_data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import unq.dapp.grupoj.soccergenius.model.dtos.external.football_data.FootballDataMatchsDto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class FootballDataApiService {

    private final RestTemplate restTemplate;
    private final Map<Integer, Integer> whoScoredToFootballDataTeamIdMap;

    @Value("${football-data.api.key}")
    private String apiKey;

    @Value("${football-data.api.base-url:http://api.football-data.org/v4}")
    private String baseUrl;

    @Autowired
    public FootballDataApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.whoScoredToFootballDataTeamIdMap = initializeTeamIdMapping();
    }

    /**
     * Initializes the mapping between WhoScored team IDs and Football-data.org team IDs
     * @return A map with WhoScored team ID as key and Football-data.org team ID as value
     */
    private Map<Integer, Integer> initializeTeamIdMapping() {
        Map<Integer, Integer> mapping = new HashMap<>();

        // Mapping WhoScored IDs to Football-data.org IDs
        mapping.put(51, 89);  // Mallorca
        mapping.put(52, 86);  // Real Madrid
        mapping.put(53, 77);  // Athletic Club
        mapping.put(54, 90);  // Real Betis
        mapping.put(55, 95);  // Valencia
        mapping.put(58, 250); // Real Valladolid
        mapping.put(60, 263); // Deportivo Alaves
        mapping.put(62, 558); // Celta Vigo
        mapping.put(63, 78);  // Atletico Madrid
        mapping.put(64, 87);  // Rayo Vallecano
        mapping.put(65, 81);  // Barcelona
        mapping.put(67, 559); // Sevilla
        mapping.put(68, 92);  // Real Sociedad
        mapping.put(70, 80);  // Espanyol
        mapping.put(131, 79); // Osasuna
        mapping.put(819, 82); // Getafe
        mapping.put(825, 745); // Leganes
        mapping.put(838, 275); // Las Palmas
        mapping.put(839, 94);  // Villarreal
        mapping.put(2783, 298); // Girona

        return mapping;
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
            return null;
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
