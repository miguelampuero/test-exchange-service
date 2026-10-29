package pe.test.exchange.infrastructure.persistence;

import org.springframework.stereotype.Component;
import pe.test.exchange.domain.model.CurrencyCode;
import pe.test.exchange.domain.model.CurrencyConversion;
import pe.test.exchange.domain.model.ExchangeRate;
import pe.test.exchange.domain.model.Money;

import java.math.BigDecimal;

/**
 * Mapea entre el agregado de dominio y la entidad JPA.
 */
@Component
public class CurrencyConversionPersistenceMapper {

    public CurrencyConversionJpaEntity toEntity(CurrencyConversion conversion) {
        return new CurrencyConversionJpaEntity(
                conversion.getId(),
                conversion.getOriginalAmount().amount(),
                conversion.getSourceCurrency().value(),
                conversion.getTargetCurrency().value(),
                conversion.getRate(),
                conversion.getConvertedAmount().amount(),
                conversion.getOperationDate()
        );
    }

    public CurrencyConversion toDomain(CurrencyConversionJpaEntity entity) {
        CurrencyCode source = CurrencyCode.of(entity.getSourceCurrency());
        CurrencyCode target = CurrencyCode.of(entity.getTargetCurrency());
        return CurrencyConversion.restore(
                entity.getId(),
                new Money(stripPadding(entity.getOriginalAmount()), source),
                new Money(entity.getConvertedAmount(), target),
                new ExchangeRate(source, target, stripPadding(entity.getExchangeRate())),
                entity.getOperationDate()
        );
    }

    /**
     * Quita los ceros de relleno que agrega la escala de la columna (100.0000 -> 100, 3.4504000000 -> 3.4504),
     * sin caer en notación científica (1E+2). El monto convertido conserva sus 2 decimales.
     */
    static BigDecimal stripPadding(BigDecimal value) {
        BigDecimal stripped = value.stripTrailingZeros();
        return stripped.scale() < 0 ? stripped.setScale(0) : stripped;
    }
}
