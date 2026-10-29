package pe.test.exchange.infrastructure.external;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pe.test.exchange.domain.exception.DomainException;
import pe.test.exchange.domain.exception.ExchangeRateUnavailableException;
import pe.test.exchange.domain.exception.UnsupportedCurrencyException;
import pe.test.exchange.domain.model.CurrencyCode;
import pe.test.exchange.domain.model.ExchangeRate;
import pe.test.exchange.domain.service.ExchangeRateProvider;
import pe.test.exchange.infrastructure.config.ExchangeRateApiProperties;
import pe.test.exchange.infrastructure.external.dto.ExchangeRateApiResponse;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Adaptador (anti-corruption layer) que implementa el puerto de dominio {@link ExchangeRateProvider}
 * usando ExchangeRate-API vía OpenFeign. Traduce el modelo y los errores de la API externa
 * a conceptos y excepciones del dominio.
 */
@Component
public class ExchangeRateApiAdapter implements ExchangeRateProvider {

    private static final Logger log = LoggerFactory.getLogger(ExchangeRateApiAdapter.class);

    static final String ERROR_UNSUPPORTED_CODE = "unsupported-code";

    private final ExchangeRateApiClient client;
    private final ExchangeRateApiProperties properties;

    public ExchangeRateApiAdapter(ExchangeRateApiClient client, ExchangeRateApiProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public ExchangeRate getRate(CurrencyCode source, CurrencyCode target) {
        ExchangeRateApiResponse response = fetchLatestRates(source);

        if (response == null) {
            throw new ExchangeRateUnavailableException("ExchangeRate-API devolvió una respuesta vacía");
        }
        if (!response.isSuccess()) {
            throw translateApiError(response.errorType(), source, null);
        }

        Map<String, BigDecimal> rates = response.conversionRates();
        if (rates == null || rates.isEmpty()) {
            throw new ExchangeRateUnavailableException("ExchangeRate-API no devolvió tipos de cambio para " + source);
        }

        BigDecimal rate = rates.get(target.value());
        if (rate == null) {
            throw new UnsupportedCurrencyException(target.value());
        }
        if (rate.signum() <= 0) {
            throw new ExchangeRateUnavailableException(
                    "ExchangeRate-API devolvió un tipo de cambio inválido para " + source + " -> " + target);
        }

        log.debug("Tipo de cambio obtenido {} -> {} = {}", source, target, rate);
        return new ExchangeRate(source, target, rate);
    }

    private ExchangeRateApiResponse fetchLatestRates(CurrencyCode source) {
        try {
            return client.getLatestRates(properties.key(), source.value());
        } catch (ExchangeRateApiException ex) {
            throw translateApiError(ex.getErrorType(), source, ex);
        } catch (FeignException ex) {
            // Timeouts, errores de red, errores de decodificación, etc.
            throw new ExchangeRateUnavailableException(
                    "Error de comunicación con ExchangeRate-API (HTTP " + ex.status() + ")", ex);
        }
    }

    private DomainException translateApiError(String errorType, CurrencyCode source, Throwable cause) {
        if (ERROR_UNSUPPORTED_CODE.equals(errorType)) {
            return new UnsupportedCurrencyException(source.value());
        }
        // invalid-key, inactive-account, quota-reached, malformed-request o desconocido:
        // son problemas del proveedor/configuración, no del cliente de nuestra API.
        String reason = errorType != null ? errorType : "desconocido";
        return new ExchangeRateUnavailableException("ExchangeRate-API respondió con error: " + reason, cause);
    }
}
