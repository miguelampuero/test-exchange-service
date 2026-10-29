package pe.test.exchange.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Modelo de persistencia (tabla CONVERSION_MONEDA). Vive en infraestructura para que
 * el modelo de dominio no dependa de JPA.
 */
@Entity
@Table(name = "conversion_moneda")
public class CurrencyConversionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "monto_original", nullable = false, precision = 19, scale = 4)
    private BigDecimal originalAmount;

    @Column(name = "moneda_origen", nullable = false, length = 3)
    private String sourceCurrency;

    @Column(name = "moneda_destino", nullable = false, length = 3)
    private String targetCurrency;

    @Column(name = "tipo_cambio", nullable = false, precision = 25, scale = 10)
    private BigDecimal exchangeRate;

    @Column(name = "monto_convertido", nullable = false, precision = 30, scale = 2)
    private BigDecimal convertedAmount;

    @Column(name = "fecha_operacion", nullable = false)
    private LocalDateTime operationDate;

    protected CurrencyConversionJpaEntity() {
        // requerido por JPA
    }

    public CurrencyConversionJpaEntity(Long id, BigDecimal originalAmount, String sourceCurrency, String targetCurrency,
                                       BigDecimal exchangeRate, BigDecimal convertedAmount, LocalDateTime operationDate) {
        this.id = id;
        this.originalAmount = originalAmount;
        this.sourceCurrency = sourceCurrency;
        this.targetCurrency = targetCurrency;
        this.exchangeRate = exchangeRate;
        this.convertedAmount = convertedAmount;
        this.operationDate = operationDate;
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }

    public String getSourceCurrency() {
        return sourceCurrency;
    }

    public String getTargetCurrency() {
        return targetCurrency;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public BigDecimal getConvertedAmount() {
        return convertedAmount;
    }

    public LocalDateTime getOperationDate() {
        return operationDate;
    }
}
