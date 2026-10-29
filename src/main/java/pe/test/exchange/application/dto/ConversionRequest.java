package pe.test.exchange.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

/**
 * Request del endpoint de conversión.
 * Las validaciones aquí son de formato (fail-fast en el borde HTTP);
 * las invariantes de negocio se vuelven a proteger en los Value Objects del dominio.
 */
public record ConversionRequest(

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0", inclusive = false, message = "El monto debe ser mayor a cero")
        @Digits(integer = 15, fraction = 4, message = "El monto admite como máximo 15 enteros y 4 decimales")
        BigDecimal monto,

        @NotBlank(message = "La moneda origen es obligatoria")
        @Pattern(regexp = "^[A-Za-z]{3}$", message = "La moneda origen debe tener 3 letras (ISO 4217)")
        String monedaOrigen,

        @NotBlank(message = "La moneda destino es obligatoria")
        @Pattern(regexp = "^[A-Za-z]{3}$", message = "La moneda destino debe tener 3 letras (ISO 4217)")
        String monedaDestino
) {
}
