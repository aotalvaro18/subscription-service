package com.eklesa.subscription.event.payload;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public abstract class BaseEvent {
    private Long organizationId;
    private Long subscriptionId;
    private LocalDateTime timestamp;
}
