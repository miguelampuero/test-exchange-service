package pe.test.exchange.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Conversión registrada en base de datos (endpoint de consulta del historial).
 */
public record ConversionHistoryResponse(
        Long id,
        BigDecimal monto,
        BigDecimal montoConvertido,
        String monedaOrigen,
        String monedaDestino,
        BigDecimal tipoCambio,
        LocalDateTime fechaOperacion
) {
}
