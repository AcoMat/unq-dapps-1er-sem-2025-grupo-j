package unq.dapp.grupoj.soccergenius.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import unq.dapp.grupoj.soccergenius.model.HistoryLog;
import unq.dapp.grupoj.soccergenius.services.history_log.HistoryLogService;

import java.time.LocalDateTime;

@Component
public class HistoryLoggingInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(HistoryLoggingInterceptor.class);
    private final HistoryLogService historyLogService;

    public HistoryLoggingInterceptor(HistoryLogService historyLogService) {
        this.historyLogService = historyLogService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        LocalDateTime startTime = LocalDateTime.now();
        request.setAttribute("startTime", startTime);

        log.info("Request URL::{} | START TIME::{} | METHOD::{} | URI::{} | REMOTE ADDR::{}",
                request.getRequestURL(), LocalDateTime.now(), request.getMethod(),
                request.getRequestURI(), request.getRemoteAddr());

        HistoryLog historyLog = HistoryLog.builder()
                                .method(request.getMethod())
                                .startTime(LocalDateTime.now())
                                .url(request.getRequestURL().toString())
                                .uri(request.getRequestURI())
                                .build();

        this.historyLogService.saveRequestLog(historyLog);
        return true;
    }
}
