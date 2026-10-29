package pe.test.exchange.infrastructure.external;

import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ExchangeRateApiErrorDecoderTest {

    private final ExchangeRateApiErrorDecoder decoder = new ExchangeRateApiErrorDecoder();

    @Test
    void extraeElErrorTypeDelCuerpo() {
        Exception ex = decoder.decode("getLatestRates", response(404, "{\"result\":\"error\",\"error-type\":\"unsupported-code\"}"));

        assertThat(ex).isInstanceOf(ExchangeRateApiException.class);
        ExchangeRateApiException apiEx = (ExchangeRateApiException) ex;
        assertThat(apiEx.getStatus()).isEqualTo(404);
        assertThat(apiEx.getErrorType()).isEqualTo("unsupported-code");
    }

    @Test
    void cuerpoSinErrorTypeDejaElTipoNulo() {
        Exception ex = decoder.decode("getLatestRates", response(500, "<html>Internal Server Error</html>"));

        ExchangeRateApiException apiEx = (ExchangeRateApiException) ex;
        assertThat(apiEx.getStatus()).isEqualTo(500);
        assertThat(apiEx.getErrorType()).isNull();
    }

    @Test
    void cuerpoVacioDejaElTipoNulo() {
        assertThat(ExchangeRateApiErrorDecoder.extractErrorType(null)).isNull();
        assertThat(ExchangeRateApiErrorDecoder.extractErrorType("  ")).isNull();
    }

    private static Response response(int status, String body) {
        Request request = Request.create(Request.HttpMethod.GET, "https://v6.exchangerate-api.com/v6/key/latest/XYZ",
                Map.of(), null, StandardCharsets.UTF_8, null);
        return Response.builder()
                .status(status)
                .reason("error")
                .request(request)
                .headers(Map.of())
                .body(body, StandardCharsets.UTF_8)
                .build();
    }
}
