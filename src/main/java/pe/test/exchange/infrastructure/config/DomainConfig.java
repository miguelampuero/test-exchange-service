package pe.test.exchange.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pe.test.exchange.domain.service.CurrencyConverterService;
import pe.test.exchange.domain.service.ExchangeRateProvider;

import java.time.Clock;

/**
 * Registra como beans los servicios de dominio (que son Java puro, sin anotaciones de Spring)
 * y sus dependencias técnicas.
 */
@Configuration
@EnableConfigurationProperties(ExchangeRateApiProperties.class)
public class DomainConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public CurrencyConverterService currencyConverterService(ExchangeRateProvider exchangeRateProvider, Clock clock) {
        return new CurrencyConverterService(exchangeRateProvider, clock);
    }
}
