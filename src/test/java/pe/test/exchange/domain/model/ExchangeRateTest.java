package pe.test.exchange.domain.model;

import org.junit.jupiter.api.Test;
import pe.test.exchange.domain.exception.InvalidAmountException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExchangeRateTest {

    private static final CurrencyCode USD = CurrencyCode.of("USD");
    private static final CurrencyCode PEN = CurrencyCode.of("PEN");

    @Test
    void convierteElMontoAplicandoElTipoDeCambio() {
        ExchangeRate rate = new ExchangeRate(USD, PEN, new BigDecimal("3.70"));

        Money converted = rate.convert(new Money(new BigDecimal("100"), USD));

        assertThat(converted.amount()).isEqualByComparingTo("370.00");
        assertThat(converted.amount().scale()).isEqualTo(2);
        assertThat(converted.currency()).isEqualTo(PEN);
    }

    @Test
    void redondeaHalfUpADosDecimales() {
        ExchangeRate rate = new ExchangeRate(USD, PEN, new BigDecimal("3.7125"));

        Money converted = rate.convert(new Money(new BigDecimal("1"), USD));

        assertThat(converted.amount()).isEqualByComparingTo("3.71");

        Money converted2 = new ExchangeRate(USD, PEN, new BigDecimal("3.715")).convert(new Money(BigDecimal.ONE, USD));
        assertThat(converted2.amount()).isEqualByComparingTo("3.72");
    }

    @Test
    void tipoDeCambioIdentidadDevuelveElMismoMonto() {
        Money converted = ExchangeRate.identity(USD).convert(new Money(new BigDecimal("42.5"), USD));

        assertThat(converted.amount()).isEqualByComparingTo("42.50");
        assertThat(converted.currency()).isEqualTo(USD);
    }

    @Test
    void rechazaTipoDeCambioCeroONegativo() {
        assertThatThrownBy(() -> new ExchangeRate(USD, PEN, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ExchangeRate(USD, PEN, new BigDecimal("-3.7")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rechazaMontoEnMonedaDistintaALaDeOrigen() {
        ExchangeRate rate = new ExchangeRate(USD, PEN, new BigDecimal("3.70"));

        assertThatThrownBy(() -> rate.convert(new Money(BigDecimal.TEN, PEN)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rechazaMontoQueAlConvertirseRedondeaACero() {
        ExchangeRate rate = new ExchangeRate(USD, PEN, new BigDecimal("0.001"));

        assertThatThrownBy(() -> rate.convert(new Money(new BigDecimal("1"), USD)))
                .isInstanceOf(InvalidAmountException.class)
                .hasMessageContaining("demasiado pequeño");
    }
}
