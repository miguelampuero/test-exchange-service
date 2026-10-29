package pe.test.exchange.domain.model;

import pe.test.exchange.domain.exception.InvalidAmountException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object: tipo de cambio de una moneda origen a una moneda destino.
 * Encapsula la regla de conversión y de redondeo del monto convertido.
 */
public record ExchangeRate(CurrencyCode source, CurrencyCode target, BigDecimal rate) {

    /** Escala del monto convertido (2 decimales, redondeo HALF_UP). */
    public static final int CONVERTED_AMOUNT_SCALE = 2;

    public ExchangeRate {
        Objects.requireNonNull(source, "La moneda origen es obligatoria");
        Objects.requireNonNull(target, "La moneda destino es obligatoria");
        if (rate == null || rate.signum() <= 0) {
            throw new IllegalArgumentException("El tipo de cambio debe ser mayor a cero");
        }
    }

    /** Tipo de cambio neutro para una conversión entre la misma moneda. */
    public static ExchangeRate identity(CurrencyCode currency) {
        return new ExchangeRate(currency, currency, BigDecimal.ONE);
    }

    public Money convert(Money money) {
        if (!money.currency().equals(source)) {
            throw new IllegalArgumentException(
                    "El monto está en " + money.currency() + " pero el tipo de cambio es desde " + source);
        }
        BigDecimal converted = money.amount()
                .multiply(rate)
                .setScale(CONVERTED_AMOUNT_SCALE, RoundingMode.HALF_UP);
        if (converted.signum() == 0) {
            throw new InvalidAmountException(
                    "El monto es demasiado pequeño: convertido a " + target + " resulta 0 al redondear a "
                            + CONVERTED_AMOUNT_SCALE + " decimales");
        }
        return new Money(converted, target);
    }
}
