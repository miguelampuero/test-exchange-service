package pe.test.exchange.infrastructure.external;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.test.exchange.domain.exception.ExchangeRateUnavailableException;
import pe.test.exchange.domain.exception.UnsupportedCurrencyException;
import pe.test.exchange.domain.model.CurrencyCode;
import pe.test.exchange.domain.model.ExchangeRate;
import pe.test.exchange.infrastructure.config.ExchangeRateApiProperties;
import pe.test.exchange.infrastructure.external.dto.ExchangeRateApiResponse;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeRateApiAdapterTest {

    private static final String API_KEY = "test-key";
    private static final CurrencyCode USD = CurrencyCode.of("USD");
    private static final CurrencyCode PEN = CurrencyCode.of("PEN");

    @Mock
    private ExchangeRateApiClient client;

    private ExchangeRateApiAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ExchangeRateApiAdapter(client, new ExchangeRateApiProperties("http://localhost", API_KEY));
    }

    @Test
    void devuelveElTipoDeCambioDeLaMonedaDestino() {
        when(client.getLatestRates(API_KEY, "USD")).thenReturn(success(Map.of(
                "USD", BigDecimal.ONE,
                "PEN", new BigDecimal("3.7012"),
                "EUR", new BigDecimal("0.92"))));

        ExchangeRate rate = adapter.getRate(USD, PEN);

        assertThat(rate.source()).isEqualTo(USD);
        assertThat(rate.target()).isEqualTo(PEN);
        assertThat(rate.rate()).isEqualByComparingTo("3.7012");
    }

    @Test
    void monedaDestinoAusenteEnLaRespuestaEsNoSoportada() {
        when(client.getLatestRates(API_KEY, "USD")).thenReturn(success(Map.of("USD", BigDecimal.ONE)));

        assertThatThrownBy(() -> adapter.getRate(USD, CurrencyCode.of("XYZ")))
                .isInstanceOf(UnsupportedCurrencyException.class)
                .hasMessageContaining("XYZ");
    }

    @Test
    void errorUnsupportedCodeDeLaApiEsMonedaOrigenNoSoportada() {
        CurrencyCode xyz = CurrencyCode.of("XYZ");
        when(client.getLatestRates(API_KEY, "XYZ")).thenThrow(new ExchangeRateApiException(404, "unsupported-code"));

        assertThatThrownBy(() -> adapter.getRate(xyz, PEN))
                .isInstanceOf(UnsupportedCurrencyException.class)
                .hasMessageContaining("XYZ");
    }

    @Test
    void respuestaConResultErrorTambienSeTraduce() {
        when(client.getLatestRates(API_KEY, "USD"))
                .thenReturn(new ExchangeRateApiResponse("error", null, null, "unsupported-code"));

        assertThatThrownBy(() -> adapter.getRate(USD, PEN))
                .isInstanceOf(UnsupportedCurrencyException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid-key", "inactive-account", "quota-reached", "malformed-request"})
    void erroresDelProveedorSonServicioNoDisponible(String errorType) {
        when(client.getLatestRates(API_KEY, "USD")).thenThrow(new ExchangeRateApiException(403, errorType));

        assertThatThrownBy(() -> adapter.getRate(USD, PEN))
                .isInstanceOf(ExchangeRateUnavailableException.class)
                .hasMessageContaining(errorType);
    }

    @Test
    void erroresDeComunicacionSonServicioNoDisponible() {
        when(client.getLatestRates(API_KEY, "USD")).thenThrow(mock(FeignException.class));

        assertThatThrownBy(() -> adapter.getRate(USD, PEN))
                .isInstanceOf(ExchangeRateUnavailableException.class);
    }

    @Test
    void respuestaVaciaEsServicioNoDisponible() {
        when(client.getLatestRates(API_KEY, "USD")).thenReturn(null);

        assertThatThrownBy(() -> adapter.getRate(USD, PEN))
                .isInstanceOf(ExchangeRateUnavailableException.class);
    }

    @Test
    void respuestaSinTiposDeCambioEsServicioNoDisponible() {
        when(client.getLatestRates(API_KEY, "USD")).thenReturn(success(Map.of()));

        assertThatThrownBy(() -> adapter.getRate(USD, PEN))
                .isInstanceOf(ExchangeRateUnavailableException.class);
    }

    private static ExchangeRateApiResponse success(Map<String, BigDecimal> rates) {
        return new ExchangeRateApiResponse("success", "USD", rates, null);
    }
}
