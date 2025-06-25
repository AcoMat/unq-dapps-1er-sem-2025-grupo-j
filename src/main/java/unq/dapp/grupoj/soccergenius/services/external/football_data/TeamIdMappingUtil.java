package unq.dapp.grupoj.soccergenius.services.external.football_data;

import java.util.HashMap;
import java.util.Map;

public class TeamIdMappingUtil {

    private static final Map<Integer, Integer> teamIdsMap = new HashMap<>();
    private static final Map<String, Integer> teamNamesMap = new HashMap<>();

    static {
        teamIdsMap.put(89, 51);   // Mallorca
        teamIdsMap.put(86, 52);   // Real Madrid
        teamIdsMap.put(77, 53);   // Athletic Club
        teamIdsMap.put(90, 54);   // Real Betis
        teamIdsMap.put(95, 55);   // Valencia
        teamIdsMap.put(250, 58);  // Real Valladolid
        teamIdsMap.put(263, 60);  // Deportivo Alaves
        teamIdsMap.put(558, 62);  // Celta Vigo
        teamIdsMap.put(78, 63);   // Atletico Madrid
        teamIdsMap.put(87, 64);   // Rayo Vallecano
        teamIdsMap.put(81, 65);   // Barcelona
        teamIdsMap.put(559, 67);  // Sevilla
        teamIdsMap.put(92, 68);   // Real Sociedad
        teamIdsMap.put(80, 70);   // Espanyol
        teamIdsMap.put(79, 131);  // Osasuna
        teamIdsMap.put(82, 819);  // Getafe
        teamIdsMap.put(745, 825); // Leganes
        teamIdsMap.put(275, 838); // Las Palmas
        teamIdsMap.put(94, 839);  // Villarreal
        teamIdsMap.put(298, 2783); // Girona

        teamNamesMap.put("mallorca", 51);
        teamNamesMap.put("real madrid", 52);
        teamNamesMap.put("athletic club", 53);
        teamNamesMap.put("real betis", 54);
        teamNamesMap.put("valencia", 55);
        teamNamesMap.put("real valladolid", 58);
        teamNamesMap.put("deportivo alaves", 60);
        teamNamesMap.put("celta vigo", 62);
        teamNamesMap.put("atletico madrid", 63);
        teamNamesMap.put("rayo vallecano", 64);
        teamNamesMap.put("barcelona", 65);
        teamNamesMap.put("sevilla", 67);
        teamNamesMap.put("real sociedad", 68);
        teamNamesMap.put("espanyol", 70);
        teamNamesMap.put("osasuna", 131);
        teamNamesMap.put("getafe", 819);
        teamNamesMap.put("leganes", 825);
        teamNamesMap.put("las palmas", 838);
        teamNamesMap.put("villarreal", 839);
        teamNamesMap.put("girona", 2783);
    }

    public static int whoScoredToFootballDataTeamIdMap(int whoScoredTeamId) {
        for (Map.Entry<Integer, Integer> entry : teamIdsMap.entrySet()) {
            if (entry.getValue() == whoScoredTeamId) {
                return entry.getKey();
            }
        }
        throw new IllegalArgumentException("No Football-data.org ID found for WhoScored team ID: " + whoScoredTeamId);
    }

    public static int footballDataTeamIdFromName(String teamName) {
        if (teamName == null || teamName.isEmpty()) {
            throw new IllegalArgumentException("Team name cannot be null or empty");
        }

        Integer teamId = teamNamesMap.get(teamName);
        if (teamId == null) {
            throw new IllegalArgumentException("No team found in LaLiga with name: " + teamName);
        }

        return teamId;
    }
}
