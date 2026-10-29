package pe.test.exchange.infrastructure.persistence;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import pe.test.exchange.domain.model.CurrencyConversion;
import pe.test.exchange.domain.repository.CurrencyConversionRepository;

import java.util.List;

/**
 * Implementación del puerto de dominio {@link CurrencyConversionRepository} sobre Spring Data JPA (H2).
 */
@Repository
public class CurrencyConversionRepositoryAdapter implements CurrencyConversionRepository {

    private final SpringDataCurrencyConversionRepository jpaRepository;
    private final CurrencyConversionPersistenceMapper mapper;

    public CurrencyConversionRepositoryAdapter(SpringDataCurrencyConversionRepository jpaRepository,
                                               CurrencyConversionPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public CurrencyConversion save(CurrencyConversion conversion) {
        CurrencyConversionJpaEntity saved = jpaRepository.save(mapper.toEntity(conversion));
        return mapper.toDomain(saved);
    }

    @Override
    public List<CurrencyConversion> findAll() {
        return jpaRepository.findAll(Sort.by(Sort.Direction.DESC, "operationDate", "id")).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
