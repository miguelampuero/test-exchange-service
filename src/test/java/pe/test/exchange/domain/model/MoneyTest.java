package pe.test.exchange.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import pe.test.exchange.domain.exception.InvalidAmountException;
import pe.test.exchange.domain.exception.InvalidCurrencyException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    void creaMontoValido() {
        Money money = Money.of(new BigDecimal("100.50"), "usd");

        assertThat(money.amount()).isEqualByComparingTo("100.50");
        assertThat(money.currency().value()).isEqualTo("USD");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "-1", "-0.01"})
    void rechazaMontosCeroONegativos(String amount) {
        assertThatThrownBy(() -> Money.of(new BigDecimal(amount), "USD"))
                .isInstanceOf(InvalidAmountException.class)
                .hasMessageContaining("mayor a cero");
    }

    @Test
    void rechazaMontoNulo() {
        assertThatThrownBy(() -> Money.of(null, "USD"))
                .isInstanceOf(InvalidAmountException.class);
    }

    @Test
    void rechazaMonedaNula() {
        assertThatThrownBy(() -> new Money(BigDecimal.TEN, null))
                .isInstanceOf(InvalidCurrencyException.class);
    }
}
