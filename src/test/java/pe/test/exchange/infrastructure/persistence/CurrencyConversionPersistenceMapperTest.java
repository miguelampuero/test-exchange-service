package pe.test.exchange.infrastructure.persistence;

import org.junit.jupiter.api.Test;
import pe.test.exchange.domain.model.CurrencyConversion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CurrencyConversionPersistenceMapperTest {

    private final CurrencyConversionPersistenceMapper mapper = new CurrencyConversionPersistenceMapper();

    @Test
    void reconstruyeElAgregadoSinCerosDeRelleno() {
        CurrencyConversionJpaEntity entity = new CurrencyConversionJpaEntity(1L,
                new BigDecimal("100.0000"), "USD", "PEN",
                new BigDecimal("3.4504000000"), new BigDecimal("345.04"),
                LocalDateTime.of(2026, 10, 29, 9, 0));

        CurrencyConversion conversion = mapper.toDomain(entity);

        assertThat(conversion.getId()).isEqualTo(1L);
        assertThat(conversion.getOriginalAmount().amount().toPlainString()).isEqualTo("100");
        assertThat(conversion.getRate().toPlainString()).isEqualTo("3.4504");
        assertThat(conversion.getConvertedAmount().amount().toPlainString()).isEqualTo("345.04");
        assertThat(conversion.getSourceCurrency().value()).isEqualTo("USD");
        assertThat(conversion.getTargetCurrency().value()).isEqualTo("PEN");
    }

    @Test
    void quitarRellenoNoUsaNotacionCientifica() {
        assertThat(CurrencyConversionPersistenceMapper.stripPadding(new BigDecimal("1000.0000")).toString()).isEqualTo("1000");
        assertThat(CurrencyConversionPersistenceMapper.stripPadding(new BigDecimal("0.0000380000")).toString()).isEqualTo("0.000038");
        assertThat(CurrencyConversionPersistenceMapper.stripPadding(new BigDecimal("100.5000")).toString()).isEqualTo("100.5");
    }

    @Test
    void mapeaElAgregadoALaEntidad() {
        CurrencyConversionJpaEntity entity = mapper.toEntity(mapper.toDomain(new CurrencyConversionJpaEntity(null,
                new BigDecimal("10"), "USD", "EUR", new BigDecimal("0.92"), new BigDecimal("9.20"),
                LocalDateTime.of(2026, 10, 29, 9, 0))));

        assertThat(entity.getId()).isNull();
        assertThat(entity.getSourceCurrency()).isEqualTo("USD");
        assertThat(entity.getTargetCurrency()).isEqualTo("EUR");
        assertThat(entity.getConvertedAmount()).isEqualByComparingTo("9.20");
    }
}
