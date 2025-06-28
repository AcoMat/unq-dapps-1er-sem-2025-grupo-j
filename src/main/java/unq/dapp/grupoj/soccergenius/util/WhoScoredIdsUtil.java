package unq.dapp.grupoj.soccergenius.util;

import java.util.HashMap;
import java.util.Map;

public class WhoScoredIdsUtil {

    private WhoScoredIdsUtil() {
        throw new IllegalStateException("Utility class should not be instantiated");
    }

    private static final Map<String, Integer> teamNamesMap = new HashMap<>();

    static {
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

    public static int getTeamIdFromTeamName(String teamName) {
        if (teamName == null || teamName.isEmpty()) {
            throw new IllegalArgumentException("Team name cannot be null or empty");
        }
        Integer teamId = teamNamesMap.get(teamName);
        if (teamId == null) {
            throw new IllegalArgumentException("No LaLiga team found for: " + teamName);
        }
        return teamId;
    }
}
