package br.com.guerreiro_dev.backend.repository;
import br.com.guerreiro_dev.backend.domain.NotaFiscalEmitida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface NotaFiscalEmitidaRepository extends JpaRepository<NotaFiscalEmitida, UUID> {
    @Query(
            value = "SELECT nextval('nota_fiscal_numero_seq')",
            nativeQuery = true
    )
    Long obterProximaSequencia();
}
