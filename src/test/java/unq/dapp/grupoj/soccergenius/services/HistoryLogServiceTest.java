package unq.dapp.grupoj.soccergenius.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import unq.dapp.grupoj.soccergenius.model.HistoryLog;
import unq.dapp.grupoj.soccergenius.repository.HistoryLogRespository;
import unq.dapp.grupoj.soccergenius.services.historyLog.HistoryLogServiceImpl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HistoryLogServiceTest {

    @Mock
    private HistoryLogRespository historyLogRespository;

    @InjectMocks
    private HistoryLogServiceImpl historyLogService;

    private HistoryLog sampleHistoryLog1;
    private HistoryLog sampleHistoryLog2;

    @BeforeEach
    void setUp() {
        // Usamos el constructor con todos los argumentos para crear los objetos HistoryLog
        sampleHistoryLog1 = new HistoryLog(1L, "http://localhost:8080/api/players/1", "GET", "/api/players/1", LocalDateTime.now().minusDays(1));
        sampleHistoryLog2 = new HistoryLog(2L, "http://localhost:8080/api/teams/5", "GET", "/api/teams/5", LocalDateTime.now());
    }

    @Test
    void testSaveRequestLog_ShouldCallRepositorySave() {
        // Arrange
        // Creamos el nuevo log usando el constructor con todos los argumentos.
        // El ID es null porque se espera que sea generado por la base de datos.
        HistoryLog newLog = new HistoryLog(null, "http://localhost:8080/api/users", "POST", "/api/users", LocalDateTime.now());

        // Act
        historyLogService.saveRequestLog(newLog);

        // Assert
        // Verifica que el método save del repositorio fue llamado exactamente una vez con el objeto newLog
        verify(historyLogRespository, times(1)).save(newLog);
    }

    @Test
    void testGetHistoryLogs_WhenLogsExist_ShouldReturnListOfLogs() {
        // Arrange
        List<HistoryLog> expectedLogs = Arrays.asList(sampleHistoryLog1, sampleHistoryLog2);
        when(historyLogRespository.findAll()).thenReturn(expectedLogs);

        // Act
        List<HistoryLog> actualLogs = historyLogService.getHistoryLogs();

        // Assert
        assertNotNull(actualLogs);
        assertEquals(2, actualLogs.size());
        assertEquals(expectedLogs, actualLogs);
        // Verifica que el método findAll del repositorio fue llamado exactamente una vez
        verify(historyLogRespository, times(1)).findAll();
    }

    @Test
    void testGetHistoryLogs_WhenNoLogsExist_ShouldReturnEmptyList() {
        // Arrange
        when(historyLogRespository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<HistoryLog> actualLogs = historyLogService.getHistoryLogs();

        // Assert
        assertNotNull(actualLogs);
        assertEquals(0, actualLogs.size());
        // Verifica que el método findAll del repositorio fue llamado exactamente una vez
        verify(historyLogRespository, times(1)).findAll();
    }
}
