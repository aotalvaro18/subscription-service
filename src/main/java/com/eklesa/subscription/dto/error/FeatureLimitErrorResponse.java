package com.eklesa.subscription.dto.error;

import lombok.*;
import java.time.LocalDateTime;

@Data
// ✅ CORRECCIÓN QUIRÚRGICA: Se elimina el @Builder de nivel de clase para evitar conflicto.
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true) // ✅ MEJORA: Se pone 'true' para incluir los campos de la clase padre en equals/hashCode.
public class FeatureLimitErrorResponse extends ErrorResponse {
    private String code;
    private String featureCode;
    private Integer currentUsage;
    private Integer maxLimit;

    // ✅ Este es el ÚNICO @Builder que debe existir. Le dice a Lombok cómo construir
    // el objeto completo, incluyendo los campos de la clase padre (super).
    @Builder(builderMethodName = "featureLimitBuilder")
    public FeatureLimitErrorResponse(LocalDateTime timestamp, int status, String error, String message, String code, String featureCode, Integer currentUsage, Integer maxLimit) {
        super(timestamp, status, error, message);
        this.code = code;
        this.featureCode = featureCode;
        this.currentUsage = currentUsage;
        this.maxLimit = maxLimit;
    }
}