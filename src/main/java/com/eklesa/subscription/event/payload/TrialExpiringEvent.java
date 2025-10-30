package com.eklesa.subscription.event.payload;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TrialExpiringEvent extends BaseEvent {
    private Long organizationId;
    private Long subscriptionId;
    private long daysLeft;
    private LocalDateTime trialEndDate;
    private LocalDateTime timestamp;
}
