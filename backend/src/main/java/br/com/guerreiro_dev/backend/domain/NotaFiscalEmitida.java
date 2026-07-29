package br.com.guerreiro_dev.backend.domain;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_nota_fiscal_emitida")
public class NotaFiscalEmitida implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String numeroNota;
    private LocalDate dataEmissao;

    @ManyToOne
    @JoinColumn(name = "locacao_id")
    private Locacao locacao;

    @Lob
    @Column(name = "pdf_arquivo")
    private byte[] pdfArquivo;



}
