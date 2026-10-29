package pe.test.exchange;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import pe.test.exchange.infrastructure.external.ExchangeRateApiClient;
import pe.test.exchange.infrastructure.external.dto.ExchangeRateApiResponse;
import pe.test.exchange.infrastructure.persistence.SpringDataCurrencyConversionRepository;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de integración: levanta el contexto completo con H2 real y sólo simula
 * el cliente HTTP externo (para no depender de la red ni consumir cuota de la API).
 */
@SpringBootTest
class ExchangeServiceApplicationTests {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private SpringDataCurrencyConversionRepository jpaRepository;

    @MockitoBean
    private ExchangeRateApiClient exchangeRateApiClient;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        jpaRepository.deleteAll();
    }

    @Test
    void contextLoads() {
        assertThat(context).isNotNull();
    }

    @Test
    void convierteYPersisteLaConversionEnH2() throws Exception {
        when(exchangeRateApiClient.getLatestRates(anyString(), eq("USD"))).thenReturn(new ExchangeRateApiResponse(
                "success", "USD", Map.of("USD", BigDecimal.ONE, "PEN", new BigDecimal("3.70")), null));

        mockMvc.perform(post("/api/conversion").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monto\": 100, \"monedaOrigen\": \"USD\", \"monedaDestino\": \"PEN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.montoConvertido").value(370.00))
                .andExpect(jsonPath("$.tipoCambio").value(3.70));

        assertThat(jpaRepository.findAll()).singleElement().satisfies(entity -> {
            assertThat(entity.getId()).isNotNull();
            assertThat(entity.getOriginalAmount()).isEqualByComparingTo("100");
            assertThat(entity.getSourceCurrency()).isEqualTo("USD");
            assertThat(entity.getTargetCurrency()).isEqualTo("PEN");
            assertThat(entity.getExchangeRate()).isEqualByComparingTo("3.70");
            assertThat(entity.getConvertedAmount()).isEqualByComparingTo("370.00");
            assertThat(entity.getOperationDate()).isNotNull();
        });

        mockMvc.perform(get("/api/conversion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].monedaOrigen").value("USD"))
                .andExpect(jsonPath("$[0].fechaOperacion").exists());
    }

    @Test
    void monedaNoSoportadaNoPersisteNada() throws Exception {
        when(exchangeRateApiClient.getLatestRates(anyString(), eq("USD"))).thenReturn(new ExchangeRateApiResponse(
                "success", "USD", Map.of("USD", BigDecimal.ONE), null));

        mockMvc.perform(post("/api/conversion").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monto\": 100, \"monedaOrigen\": \"USD\", \"monedaDestino\": \"XYZ\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("MONEDA_NO_SOPORTADA"));

        assertThat(jpaRepository.count()).isZero();
    }
}
