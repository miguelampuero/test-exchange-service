package pe.test.exchange.domain.model;

import pe.test.exchange.domain.exception.InvalidAmountException;
import pe.test.exchange.domain.exception.InvalidCurrencyException;

import java.math.BigDecimal;

/**
 * Value Object: un monto positivo expresado en una moneda.
 * Inmutable; las invariantes se validan en el constructor.
 */
public record Money(BigDecimal amount, CurrencyCode currency) {

    public Money {
        if (amount == null) {
            throw new InvalidAmountException("El monto es obligatorio");
        }
        if (amount.signum() <= 0) {
            throw new InvalidAmountException("El monto debe ser mayor a cero");
        }
        if (currency == null) {
            throw new InvalidCurrencyException("La moneda del monto es obligatoria");
        }
    }

    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, CurrencyCode.of(currency));
    }
}
