package pe.test.exchange.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Formato uniforme de error para todos los endpoints.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String codigo,
        String mensaje,
        String path,
        List<String> detalles
) {

    public static ErrorResponse of(int status, String codigo, String mensaje, String path) {
        return new ErrorResponse(LocalDateTime.now(), status, codigo, mensaje, path, List.of());
    }

    public static ErrorResponse of(int status, String codigo, String mensaje, String path, List<String> detalles) {
        return new ErrorResponse(LocalDateTime.now(), status, codigo, mensaje, path, detalles);
    }
}
