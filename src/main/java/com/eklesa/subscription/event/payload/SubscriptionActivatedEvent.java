package com.eklesa.subscription.event.payload;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SubscriptionActivatedEvent extends BaseEvent {
    private Long organizationId;
    private Long subscriptionId;
    private String planCode;
    private String billingPeriod;
    private BigDecimal amount;
    private LocalDateTime timestamp;
}
