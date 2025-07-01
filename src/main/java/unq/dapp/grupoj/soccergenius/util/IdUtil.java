package unq.dapp.grupoj.soccergenius.util;

import java.util.Map;

public class IdUtil {

    private IdUtil() {
        throw new IllegalStateException("Utility class should not be instantiated");
    }

    public static int getTeamIdFromTeamName(String teamName, Map<String, Integer> teamNamesMap, String provider) {
        if (teamName == null || teamName.isEmpty()) {
            throw new IllegalArgumentException("Team name cannot be null or empty");
        }
        Integer teamId = teamNamesMap.get(teamName.toLowerCase());
        if (teamId == null) {
            throw new IllegalArgumentException("No team found in " + provider + " with name: " + teamName);
        }

        return teamId;
    }
}

