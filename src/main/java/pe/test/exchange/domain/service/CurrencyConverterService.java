package pe.test.exchange.domain.service;

import pe.test.exchange.domain.model.CurrencyCode;
import pe.test.exchange.domain.model.CurrencyConversion;
import pe.test.exchange.domain.model.ExchangeRate;
import pe.test.exchange.domain.model.Money;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Servicio de dominio: orquesta la regla de negocio de convertir un monto a otra moneda.
 * Es Java puro (sin Spring); se registra como bean en la capa de infraestructura.
 */
public class CurrencyConverterService {

    private final ExchangeRateProvider exchangeRateProvider;
    private final Clock clock;

    public CurrencyConverterService(ExchangeRateProvider exchangeRateProvider, Clock clock) {
        this.exchangeRateProvider = Objects.requireNonNull(exchangeRateProvider);
        this.clock = Objects.requireNonNull(clock);
    }

    public CurrencyConversion convert(Money amount, CurrencyCode targetCurrency) {
        Objects.requireNonNull(amount, "El monto es obligatorio");
        Objects.requireNonNull(targetCurrency, "La moneda destino es obligatoria");

        ExchangeRate rate = amount.currency().equals(targetCurrency)
                ? ExchangeRate.identity(targetCurrency)          // misma moneda: no se consulta al proveedor
                : exchangeRateProvider.getRate(amount.currency(), targetCurrency);

        return CurrencyConversion.perform(amount, rate, LocalDateTime.now(clock));
    }
}
