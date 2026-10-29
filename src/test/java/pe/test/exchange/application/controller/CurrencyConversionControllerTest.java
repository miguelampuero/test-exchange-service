package pe.test.exchange.application.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pe.test.exchange.application.dto.ConversionRequest;
import pe.test.exchange.application.dto.ConversionResponse;
import pe.test.exchange.application.service.CurrencyConversionApplicationService;
import pe.test.exchange.domain.exception.ExchangeRateUnavailableException;
import pe.test.exchange.domain.exception.UnsupportedCurrencyException;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CurrencyConversionControllerTest {

    private static final String URL = "/api/conversion";

    @Mock
    private CurrencyConversionApplicationService service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new CurrencyConversionController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void conversionExitosaDevuelve200ConElContrato() throws Exception {
        when(service.convert(any(ConversionRequest.class))).thenReturn(new ConversionResponse(
                new BigDecimal("100"), new BigDecimal("370.00"), "USD", "PEN", new BigDecimal("3.70")));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monto\": 100, \"monedaOrigen\": \"USD\", \"monedaDestino\": \"PEN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monto").value(100))
                .andExpect(jsonPath("$.montoConvertido").value(370.00))
                .andExpect(jsonPath("$.monedaOrigen").value("USD"))
                .andExpect(jsonPath("$.monedaDestino").value("PEN"))
                .andExpect(jsonPath("$.tipoCambio").value(3.70));
    }

    @Test
    void camposInvalidosDevuelven400ConDetalle() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monto\": -5, \"monedaOrigen\": \"US\", \"monedaDestino\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"))
                .andExpect(jsonPath("$.detalles", hasItem("monto: El monto debe ser mayor a cero")))
                .andExpect(jsonPath("$.detalles", hasItem("monedaOrigen: La moneda origen debe tener 3 letras (ISO 4217)")))
                .andExpect(jsonPath("$.detalles", hasItem("monedaDestino: La moneda destino es obligatoria")));

        verifyNoInteractions(service);
    }

    @Test
    void camposObligatoriosAusentesDevuelven400() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"))
                .andExpect(jsonPath("$.detalles", hasItem("monto: El monto es obligatorio")));
    }

    @Test
    void jsonMalformadoDevuelve400() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{\"monto\": \"abc\""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("CUERPO_INVALIDO"));
    }

    @Test
    void monedaNoSoportadaDevuelve400() throws Exception {
        when(service.convert(any())).thenThrow(new UnsupportedCurrencyException("XYZ"));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monto\": 10, \"monedaOrigen\": \"USD\", \"monedaDestino\": \"XYZ\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("MONEDA_NO_SOPORTADA"))
                .andExpect(jsonPath("$.path").value(URL));
    }

    @Test
    void proveedorCaidoDevuelve503() throws Exception {
        when(service.convert(any())).thenThrow(new ExchangeRateUnavailableException("timeout"));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monto\": 10, \"monedaOrigen\": \"USD\", \"monedaDestino\": \"PEN\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.codigo").value("SERVICIO_TIPO_CAMBIO_NO_DISPONIBLE"));
    }

    @Test
    void errorInesperadoDevuelve500SinExponerDetalles() throws Exception {
        when(service.convert(any())).thenThrow(new IllegalStateException("detalle interno"));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monto\": 10, \"monedaOrigen\": \"USD\", \"monedaDestino\": \"PEN\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.codigo").value("ERROR_INTERNO"))
                .andExpect(jsonPath("$.mensaje").value("Ocurrió un error inesperado"));
    }

    @Test
    void contentTypeNoJsonDevuelve415() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.TEXT_PLAIN).content("hola"))
                .andExpect(status().isUnsupportedMediaType());
    }
}
