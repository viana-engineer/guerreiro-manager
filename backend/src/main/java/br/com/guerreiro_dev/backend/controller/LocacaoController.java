package br.com.guerreiro_dev.backend.controller;

import br.com.guerreiro_dev.backend.dto.locacao.LocacaoCreateDTO;
import br.com.guerreiro_dev.backend.dto.locacao.LocacaoResponseDTO;
import br.com.guerreiro_dev.backend.service.LocacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/locacoes")
@RequiredArgsConstructor
public class LocacaoController {

    private final LocacaoService service;

    @GetMapping
    public ResponseEntity<List<LocacaoResponseDTO>> findAll() {
        List<LocacaoResponseDTO> locacoes = service.findAll();
        return ResponseEntity.ok().body(locacoes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocacaoResponseDTO> findById(@PathVariable UUID id) {
        LocacaoResponseDTO dto = service.findById(id);
        return ResponseEntity.ok().body(dto);
    }

    @PostMapping
    public ResponseEntity<LocacaoResponseDTO> insert(@RequestBody LocacaoCreateDTO dto) {
        LocacaoResponseDTO locacao = service.insert(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(locacao);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}