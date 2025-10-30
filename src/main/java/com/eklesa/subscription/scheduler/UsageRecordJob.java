package com.eklesa.subscription.scheduler;

import com.eklesa.subscription.service.UsageTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Job que registra el uso de features periódicamente.
 * 
 * OPCIONAL: Solo si queremos histórico granular
 * 
 * SCHEDULE: Cada 6 horas
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UsageRecordJob {
    
    private final UsageTrackingService usageTrackingService;
    
    /**
     * Ejecuta cada 6 horas.
     */
    @Scheduled(fixedDelay = 21600000) // 6 horas en ms
    public void recordUsageSnapshot() {
        log.info("Starting usage record snapshot job");
        
        // TODO: Implementar si necesitamos histórico granular
        // Por ahora, el tracking es on-demand (cuando crm-service lo solicita)
        
        log.info("Usage record snapshot job completed");
    }
}
