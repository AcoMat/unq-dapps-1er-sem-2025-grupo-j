package unq.dapp.grupoj.soccergenius.config;

import unq.dapp.grupoj.soccergenius.interceptor.HistoryLoggingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private HistoryLoggingInterceptor historyLoggingInterceptor;

//    public WebMvcConfig(HistoryLoggingInterceptor historyLoggingInterceptor){
//        this.historyLoggingInterceptor = historyLoggingInterceptor;
//    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(historyLoggingInterceptor)
                .addPathPatterns("/**");// Aplica a todas las rutas
//                .excludePathPatterns("/static/**", "/error"); // Ejemplo de exclusión
    }

}
