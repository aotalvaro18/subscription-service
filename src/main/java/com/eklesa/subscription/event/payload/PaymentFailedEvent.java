package com.eklesa.subscription.event.payload;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PaymentFailedEvent extends BaseEvent {
    private Long organizationId;
    private Long subscriptionId;
    private String paypalSubscriptionId;
    private String reason;
    private LocalDateTime timestamp;
}
