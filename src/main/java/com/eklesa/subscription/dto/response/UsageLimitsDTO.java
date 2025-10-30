package com.eklesa.subscription.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO con límites de uso actuales.
 * 
 * USADO POR: crm-service para validar antes de crear recursos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageLimitsDTO {
    
    // Contacts
    private Integer maxContacts;
    private Integer currentContacts;
    private Boolean canCreateContact;
    private Integer contactsRemaining;
    
    // Users
    private Integer maxUsers;
    private Integer currentUsers;
    private Boolean canCreateUser;
    private Integer usersRemaining;
    
    // Pipelines
    private Integer maxPipelines;
    private Integer currentPipelines;
    private Boolean canCreatePipeline;
    
    // Deals
    private Integer maxDeals;
    private Integer currentDeals;
    private Boolean canCreateDeal;
    
    // Storage
    private Integer maxStorageGb;
    private BigDecimal currentStorageGb;
    private Boolean hasStorageAvailable;
    
    // General
    private String planCode;
    private String planName;
    private Boolean isReadOnly;
}
