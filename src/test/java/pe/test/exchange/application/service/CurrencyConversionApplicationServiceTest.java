package pe.test.exchange.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.test.exchange.application.dto.ConversionHistoryResponse;
import pe.test.exchange.application.dto.ConversionRequest;
import pe.test.exchange.application.dto.ConversionResponse;
import pe.test.exchange.application.mapper.ConversionMapper;
import pe.test.exchange.domain.exception.InvalidCurrencyException;
import pe.test.exchange.domain.exception.UnsupportedCurrencyException;
import pe.test.exchange.domain.model.CurrencyCode;
import pe.test.exchange.domain.model.CurrencyConversion;
import pe.test.exchange.domain.model.ExchangeRate;
import pe.test.exchange.domain.model.Money;
import pe.test.exchange.domain.repository.CurrencyConversionRepository;
import pe.test.exchange.domain.service.CurrencyConverterService;
import pe.test.exchange.domain.service.ExchangeRateProvider;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrencyConversionApplicationServiceTest {

    private static final CurrencyCode USD = CurrencyCode.of("USD");
    private static final CurrencyCode PEN = CurrencyCode.of("PEN");

    @Mock
    private ExchangeRateProvider provider;

    @Mock
    private CurrencyConversionRepository repository;

    private CurrencyConversionApplicationService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-29T14:00:00Z"), ZoneOffset.UTC);
        CurrencyConverterService converter = new CurrencyConverterService(provider, clock);
        service = new CurrencyConversionApplicationService(converter, repository, new ConversionMapper());
    }

    @Test
    void convierteYRegistraLaOperacion() {
        when(provider.getRate(USD, PEN)).thenReturn(new ExchangeRate(USD, PEN, new BigDecimal("3.70")));
        when(repository.save(any())).thenAnswer(inv -> withId(inv.getArgument(0), 1L));

        ConversionResponse response = service.convert(new ConversionRequest(new BigDecimal("100"), "usd", "pen"));

        assertThat(response.monto()).isEqualByComparingTo("100");
        assertThat(response.montoConvertido()).isEqualByComparingTo("370.00");
        assertThat(response.monedaOrigen()).isEqualTo("USD");
        assertThat(response.monedaDestino()).isEqualTo("PEN");
        assertThat(response.tipoCambio()).isEqualByComparingTo("3.70");

        ArgumentCaptor<CurrencyConversion> captor = ArgumentCaptor.forClass(CurrencyConversion.class);
        verify(repository).save(captor.capture());
        CurrencyConversion saved = captor.getValue();
        assertThat(saved.getOriginalAmount().amount()).isEqualByComparingTo("100");
        assertThat(saved.getConvertedAmount().amount()).isEqualByComparingTo("370.00");
        assertThat(saved.getRate()).isEqualByComparingTo("3.70");
        assertThat(saved.getOperationDate()).isEqualTo(LocalDateTime.of(2026, 10, 29, 14, 0));
    }

    @Test
    void noRegistraNadaSiLaMonedaNoEsSoportada() {
        CurrencyCode xyz = CurrencyCode.of("XYZ");
        when(provider.getRate(USD, xyz)).thenThrow(new UnsupportedCurrencyException("XYZ"));

        assertThatThrownBy(() -> service.convert(new ConversionRequest(BigDecimal.TEN, "USD", "XYZ")))
                .isInstanceOf(UnsupportedCurrencyException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void validaElRequestEnElDominioAntesDeConsultarAlProveedor() {
        assertThatThrownBy(() -> service.convert(new ConversionRequest(BigDecimal.TEN, "US1", "PEN")))
                .isInstanceOf(InvalidCurrencyException.class);
        verifyNoInteractions(provider, repository);
    }

    @Test
    void listaElHistorialDeConversiones() {
        CurrencyConversion conversion = CurrencyConversion.restore(7L,
                new Money(new BigDecimal("10"), USD), new Money(new BigDecimal("37.00"), PEN),
                new ExchangeRate(USD, PEN, new BigDecimal("3.70")), LocalDateTime.of(2026, 10, 29, 9, 0));
        when(repository.findAll()).thenReturn(List.of(conversion));

        List<ConversionHistoryResponse> history = service.findAll();

        assertThat(history).singleElement().satisfies(item -> {
            assertThat(item.id()).isEqualTo(7L);
            assertThat(item.montoConvertido()).isEqualByComparingTo("37.00");
            assertThat(item.fechaOperacion()).isEqualTo(LocalDateTime.of(2026, 10, 29, 9, 0));
        });
    }

    private static CurrencyConversion withId(CurrencyConversion c, Long id) {
        return CurrencyConversion.restore(id, c.getOriginalAmount(), c.getConvertedAmount(),
                c.getExchangeRate(), c.getOperationDate());
    }
}
