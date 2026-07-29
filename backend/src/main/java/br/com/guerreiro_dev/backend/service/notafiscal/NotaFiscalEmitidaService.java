package br.com.guerreiro_dev.backend.service.notafiscal;

import br.com.guerreiro_dev.backend.domain.Locacao;
import br.com.guerreiro_dev.backend.domain.NotaFiscalEmitida;
import br.com.guerreiro_dev.backend.dto.NotaFiscalEmitida.NotaFiscalDownloadDTO;
import br.com.guerreiro_dev.backend.dto.NotaFiscalEmitida.NotaFiscalEmitidaResponseDTO;
import br.com.guerreiro_dev.backend.infrastructure.GeradorNotaFiscal;
import br.com.guerreiro_dev.backend.mapper.NotaFiscalEmitidaMapper;
import br.com.guerreiro_dev.backend.repository.LocacaoRepository;
import br.com.guerreiro_dev.backend.repository.NotaFiscalEmitidaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotaFiscalEmitidaService {

    private final NotaFiscalEmitidaRepository notaFiscalEmitidaRepository;
    private final NotaFiscalEmitidaMapper mapper;
    private final GeradorCodigoNotaFiscal geradorCodigoNotaFiscal;
    private final LocacaoRepository locacaoRepository;
    private final GeradorNotaFiscal geradorNotaFiscal;

    public List<NotaFiscalEmitidaResponseDTO> findAll(){
        return notaFiscalEmitidaRepository.findAll()
                .stream()
                .map(mapper::toResponseDTO)
                .toList();
    }

    public NotaFiscalEmitidaResponseDTO findById(UUID id){
        return mapper.toResponseDTO(findEntityById(id));
    }

    public void delete(UUID id){
        notaFiscalEmitidaRepository.delete(findEntityById(id));
    }

    @Transactional
    public NotaFiscalEmitidaResponseDTO emitirNota(UUID locacaoId){
        Locacao locacao = locacaoRepository.findById(locacaoId).
                orElseThrow(() -> new RuntimeException("Locação não encontrada com id: " + locacaoId));

        LocalDate data = LocalDate.now();

        Long sequencia = notaFiscalEmitidaRepository.obterProximaSequencia();

        String codigoNota = geradorCodigoNotaFiscal.gerar(data,sequencia);

        NotaFiscalEmitida notaFiscalEmitida = new NotaFiscalEmitida();
        notaFiscalEmitida.setLocacao(locacao);
        notaFiscalEmitida.setDataEmissao(data);
        notaFiscalEmitida.setNumeroNota(codigoNota);

        byte[] pdf = geradorNotaFiscal.gerar(notaFiscalEmitida);
        notaFiscalEmitida.setPdfArquivo(pdf);


        NotaFiscalEmitida notaSalva = notaFiscalEmitidaRepository.save(notaFiscalEmitida);

        return mapper.toResponseDTO(notaSalva);
    }

    public NotaFiscalDownloadDTO download(UUID id){
        NotaFiscalEmitida nota = findEntityById(id);
        if (nota.getPdfArquivo()==null){
            throw new RuntimeException(
                    "PDF não encontrado para esta nota."
            );
        }
        return new NotaFiscalDownloadDTO(nota.getNumeroNota(), nota.getPdfArquivo());
    }

    private NotaFiscalEmitida findEntityById(UUID id){
        return notaFiscalEmitidaRepository.findById(id)
                .orElseThrow(() ->
                new RuntimeException("Nota fiscal não encontrada com o id: " + id));

    }


}
