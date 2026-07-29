package br.com.guerreiro_dev.backend.controller;

import br.com.guerreiro_dev.backend.domain.NotaFiscalEmitida;
import br.com.guerreiro_dev.backend.dto.NotaFiscalEmitida.NotaFiscalDownloadDTO;
import br.com.guerreiro_dev.backend.dto.NotaFiscalEmitida.NotaFiscalEmitidaResponseDTO;
import br.com.guerreiro_dev.backend.service.notafiscal.NotaFiscalEmitidaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/notas-fiscais")
@RequiredArgsConstructor
public class NotaFiscalEmitidaController {

    private final NotaFiscalEmitidaService service;


    @GetMapping
    public ResponseEntity<List<NotaFiscalEmitidaResponseDTO>> findAll(){
        List<NotaFiscalEmitidaResponseDTO> notas = service.findAll();
        return ResponseEntity.ok().body(notas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotaFiscalEmitidaResponseDTO> findById(@PathVariable UUID id){
        NotaFiscalEmitidaResponseDTO dto= service.findById(id);
        return ResponseEntity.ok().body(dto);
    }

    @PostMapping("/{locacaoId}")
    public ResponseEntity<NotaFiscalEmitidaResponseDTO> create(@PathVariable UUID locacaoId){
        NotaFiscalEmitidaResponseDTO dto = service.emitirNota(locacaoId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{notaFiscalId}/pdf")
    public ResponseEntity<byte[]> download(@PathVariable UUID notaFiscalId){
        NotaFiscalDownloadDTO dto= service.download(notaFiscalId);

        String nomeArquivo = "NF-" + dto.numeroNota() + ".pdf";

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment;  filename=\"" + nomeArquivo + "\""
                )
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(dto.arquivo().length)
                .body(dto.arquivo());
    }




//    @GetMapping("/teste-pdf")
//    public ResponseEntity<byte[]> testePdf() {
//
//        UUID locacaoId = UUID.fromString(
//                "af3a452b-e8a7-4b2e-8784-9773632724d1"
//        );
//
//        NotaFiscalEmitida nota =
//                notaFiscalService.emitirNota(locacaoId);
//
//        return ResponseEntity.ok()
//                .header(
//                        HttpHeaders.CONTENT_DISPOSITION,
//                        "attachment; filename=teste.pdf"
//                )
//                .contentType(MediaType.APPLICATION_PDF)
//                .body(nota.getPdfArquivo());
//    }




}

