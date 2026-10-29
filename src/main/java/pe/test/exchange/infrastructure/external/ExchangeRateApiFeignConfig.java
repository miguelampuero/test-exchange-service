package pe.test.exchange.infrastructure.external;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

/**
 * Configuración exclusiva del cliente Feign de ExchangeRate-API.
 * No se anota con @Configuration a propósito: así sólo aplica a este cliente
 * (referenciada desde {@link ExchangeRateApiClient}) y no a todos los clientes Feign.
 */
public class ExchangeRateApiFeignConfig {

    @Bean
    public ErrorDecoder exchangeRateApiErrorDecoder() {
        return new ExchangeRateApiErrorDecoder();
    }
}
