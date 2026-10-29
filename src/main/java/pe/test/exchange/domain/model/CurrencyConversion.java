package pe.test.exchange.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad / raíz de agregado: una operación de conversión de moneda.
 * Es independiente de la tecnología de persistencia (sin anotaciones JPA);
 * la infraestructura la mapea a su propio modelo.
 */
public final class CurrencyConversion {

    private final Long id;
    private final Money originalAmount;
    private final Money convertedAmount;
    private final ExchangeRate exchangeRate;
    private final LocalDateTime operationDate;

    private CurrencyConversion(Long id, Money originalAmount, Money convertedAmount,
                               ExchangeRate exchangeRate, LocalDateTime operationDate) {
        this.id = id;
        this.originalAmount = Objects.requireNonNull(originalAmount, "El monto original es obligatorio");
        this.convertedAmount = Objects.requireNonNull(convertedAmount, "El monto convertido es obligatorio");
        this.exchangeRate = Objects.requireNonNull(exchangeRate, "El tipo de cambio es obligatorio");
        this.operationDate = Objects.requireNonNull(operationDate, "La fecha de operación es obligatoria");
    }

    /**
     * Fábrica: realiza una nueva conversión aplicando el tipo de cambio al monto original.
     */
    public static CurrencyConversion perform(Money originalAmount, ExchangeRate exchangeRate, LocalDateTime operationDate) {
        Objects.requireNonNull(originalAmount, "El monto original es obligatorio");
        Objects.requireNonNull(exchangeRate, "El tipo de cambio es obligatorio");
        Money converted = exchangeRate.convert(originalAmount);
        return new CurrencyConversion(null, originalAmount, converted, exchangeRate, operationDate);
    }

    /**
     * Reconstruye una conversión ya existente (por ejemplo, leída desde la base de datos).
     */
    public static CurrencyConversion restore(Long id, Money originalAmount, Money convertedAmount,
                                             ExchangeRate exchangeRate, LocalDateTime operationDate) {
        return new CurrencyConversion(id, originalAmount, convertedAmount, exchangeRate, operationDate);
    }

    public Long getId() {
        return id;
    }

    public Money getOriginalAmount() {
        return originalAmount;
    }

    public Money getConvertedAmount() {
        return convertedAmount;
    }

    public ExchangeRate getExchangeRate() {
        return exchangeRate;
    }

    public LocalDateTime getOperationDate() {
        return operationDate;
    }

    public CurrencyCode getSourceCurrency() {
        return originalAmount.currency();
    }

    public CurrencyCode getTargetCurrency() {
        return convertedAmount.currency();
    }

    public BigDecimal getRate() {
        return exchangeRate.rate();
    }
}
