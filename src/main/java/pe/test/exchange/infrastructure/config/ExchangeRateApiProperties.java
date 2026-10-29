package pe.test.exchange.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Propiedades de conexión a ExchangeRate-API ({@code exchange-rate.api.*}).
 * Se validan al arrancar: si falta la API key la aplicación no inicia (fail-fast).
 */
@Validated
@ConfigurationProperties(prefix = "exchange-rate.api")
public record ExchangeRateApiProperties(
        @NotBlank String baseUrl,
        @NotBlank String key
) {
}
