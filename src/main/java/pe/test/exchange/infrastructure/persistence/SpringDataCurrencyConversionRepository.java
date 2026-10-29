package pe.test.exchange.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data (detalle técnico). El resto de la aplicación usa el puerto de dominio.
 */
public interface SpringDataCurrencyConversionRepository extends JpaRepository<CurrencyConversionJpaEntity, Long> {
}
