package unq.dapp.grupoj.soccergenius.util;

import java.util.HashMap;
import java.util.Map;

public class FootballDataIdsUtil {

    private FootballDataIdsUtil() {
        throw new IllegalStateException("Utility class should not be instantiated");
    }

    private static final Map<String, Integer> teamNamesMap = new HashMap<>();

    static {
        teamNamesMap.put("mallorca", 89);
        teamNamesMap.put("real madrid", 86);
        teamNamesMap.put("athletic club", 77);
        teamNamesMap.put("real betis", 90);
        teamNamesMap.put("valencia", 95);
        teamNamesMap.put("real valladolid", 250);
        teamNamesMap.put("deportivo alaves", 263);
        teamNamesMap.put("celta vigo", 558);
        teamNamesMap.put("atletico madrid", 78);
        teamNamesMap.put("rayo vallecano", 87);
        teamNamesMap.put("barcelona", 81);
        teamNamesMap.put("sevilla", 559);
        teamNamesMap.put("real sociedad", 92);
        teamNamesMap.put("espanyol", 80);
        teamNamesMap.put("osasuna", 79);
        teamNamesMap.put("getafe", 82);
        teamNamesMap.put("leganes", 745);
        teamNamesMap.put("las palmas", 275);
        teamNamesMap.put("villarreal", 94);
        teamNamesMap.put("girona", 298);
    }

    public static int getTeamIdFromTeamName(String teamName) {
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
