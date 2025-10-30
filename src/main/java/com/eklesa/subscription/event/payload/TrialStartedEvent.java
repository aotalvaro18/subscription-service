package com.eklesa.subscription.event.payload;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TrialStartedEvent extends BaseEvent {
    private Long organizationId;
    private Long subscriptionId;
    private LocalDateTime trialEndDate;
    private String ownerEmail;
    private LocalDateTime timestamp;
}