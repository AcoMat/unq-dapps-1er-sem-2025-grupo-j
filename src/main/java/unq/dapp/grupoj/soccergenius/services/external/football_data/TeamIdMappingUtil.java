package unq.dapp.grupoj.soccergenius.services.external.football_data;

import java.util.HashMap;
import java.util.Map;

public class TeamIdMappingUtil {
    /**
     * Retorna el mapeo entre IDs de WhoScored y Football-data.org
     * @return Un mapa con el ID de WhoScored como clave y el ID de Football-data.org como valor
     */
    public static Map<Integer, Integer> getWhoScoredToFootballDataTeamIdMap() {
        Map<Integer, Integer> mapping = new HashMap<>();
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
}

