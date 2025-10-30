package com.eklesa.subscription.dto.error;

import lombok.*;
import java.time.LocalDateTime;
import java.util.Map;

@Data
// ✅ CORRECCIÓN QUIRÚRGICA: Se elimina el @Builder de nivel de clase para evitar conflicto.
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true) // ✅ MEJORA: Se pone 'true' para incluir los campos de la clase padre.
public class ValidationErrorResponse extends ErrorResponse {
    private Map<String, String> fieldErrors;

    // ✅ Este es el ÚNICO @Builder que debe existir.
    @Builder(builderMethodName = "validationErrorBuilder")
    public ValidationErrorResponse(LocalDateTime timestamp, int status, String error, String message, Map<String, String> fieldErrors) {
        super(timestamp, status, error, message);
        this.fieldErrors = fieldErrors;
    }
}