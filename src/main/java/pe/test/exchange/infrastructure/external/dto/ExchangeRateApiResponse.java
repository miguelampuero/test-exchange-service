package pe.test.exchange.infrastructure.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Map;


public record ExchangeRateApiResponse(
        String result,
        @JsonProperty("base_code")
        String baseCode,
        @JsonProperty("conversion_rates")
        Map<String, BigDecimal> conversionRates,
        @JsonProperty("error-type")
        String errorType
) {

    public static final String RESULT_SUCCESS = "success";

    @JsonIgnore
    public boolean isSuccess() {
        return RESULT_SUCCESS.equalsIgnoreCase(result);
    }
}
