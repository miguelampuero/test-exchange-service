package pe.test.exchange.infrastructure.external;

import feign.Response;
import feign.codec.ErrorDecoder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Convierte las respuestas HTTP no exitosas de ExchangeRate-API en {@link ExchangeRateApiException},
 * extrayendo el campo {@code error-type} del cuerpo de error, p. ej.:
 * <pre>{"result":"error","error-type":"unsupported-code"}</pre>
 */
public class ExchangeRateApiErrorDecoder implements ErrorDecoder {

    private static final Pattern ERROR_TYPE = Pattern.compile("\"error-type\"\\s*:\\s*\"([^\"]+)\"");

    @Override
    public Exception decode(String methodKey, Response response) {
        String errorType = extractErrorType(readBody(response));
        return new ExchangeRateApiException(response.status(), errorType);
    }

    static String extractErrorType(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }
        Matcher matcher = ERROR_TYPE.matcher(body);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static String readBody(Response response) {
        if (response.body() == null) {
            return null;
        }
        try (InputStream in = response.body().asInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }
}
