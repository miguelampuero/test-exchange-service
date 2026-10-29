package pe.test.exchange.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.test.exchange.domain.exception.ExchangeRateUnavailableException;
import pe.test.exchange.domain.model.CurrencyCode;
import pe.test.exchange.domain.model.CurrencyConversion;
import pe.test.exchange.domain.model.ExchangeRate;
import pe.test.exchange.domain.model.Money;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrencyConverterServiceTest {

    private static final CurrencyCode USD = CurrencyCode.of("USD");
    private static final CurrencyCode PEN = CurrencyCode.of("PEN");
    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-10-29T14:00:00Z"), LIMA);

    @Mock
    private ExchangeRateProvider provider;

    private CurrencyConverterService service;

    @BeforeEach
    void setUp() {
        service = new CurrencyConverterService(provider, FIXED_CLOCK);
    }

    @Test
    void convierteUsandoElTipoDeCambioDelProveedor() {
        when(provider.getRate(USD, PEN)).thenReturn(new ExchangeRate(USD, PEN, new BigDecimal("3.70")));

        CurrencyConversion conversion = service.convert(new Money(new BigDecimal("100"), USD), PEN);

        assertThat(conversion.getId()).isNull();
        assertThat(conversion.getOriginalAmount().amount()).isEqualByComparingTo("100");
        assertThat(conversion.getConvertedAmount().amount()).isEqualByComparingTo("370.00");
        assertThat(conversion.getSourceCurrency()).isEqualTo(USD);
        assertThat(conversion.getTargetCurrency()).isEqualTo(PEN);
        assertThat(conversion.getRate()).isEqualByComparingTo("3.70");
        assertThat(conversion.getOperationDate()).isEqualTo(LocalDateTime.of(2026, 10, 29, 9, 0));
    }

    @Test
    void mismaMonedaNoConsultaAlProveedorYUsaTipoDeCambioUno() {
        CurrencyConversion conversion = service.convert(new Money(new BigDecimal("50"), USD), USD);

        assertThat(conversion.getRate()).isEqualByComparingTo("1");
        assertThat(conversion.getConvertedAmount().amount()).isEqualByComparingTo("50.00");
        verifyNoInteractions(provider);
    }

    @Test
    void propagaLaExcepcionDelProveedor() {
        when(provider.getRate(any(), any())).thenThrow(new ExchangeRateUnavailableException("caído"));

        assertThatThrownBy(() -> service.convert(new Money(BigDecimal.TEN, USD), PEN))
                .isInstanceOf(ExchangeRateUnavailableException.class);
    }
}
