package unq.dapp.grupoj.soccergenius.config;

import com.google.genai.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GenAiConfig {

    @Value("${GOOGLE_API_KEY}")
    private String googleApiKey;

    @Bean
    public Client client() {
        return new Client.Builder()
                .apiKey(googleApiKey)
                .build();
    }
}
