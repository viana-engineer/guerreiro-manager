package br.com.guerreiro_dev.backend.controller;

import br.com.guerreiro_dev.backend.dto.veiculo.VeiculoCreateDTO;
import br.com.guerreiro_dev.backend.dto.veiculo.VeiculoResponseDTO;
import br.com.guerreiro_dev.backend.service.VeiculoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/veiculos")
@RequiredArgsConstructor
public class VeiculoController {

    private final VeiculoService service;

    @GetMapping
    public ResponseEntity<List<VeiculoResponseDTO>> findAll() {
        List<VeiculoResponseDTO> veiculos = service.findAll();
        return ResponseEntity.ok().body(veiculos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VeiculoResponseDTO> findById(@PathVariable UUID id) {
        VeiculoResponseDTO dto = service.findById(id);
        return ResponseEntity.ok().body(dto);
    }

    @PostMapping
    public ResponseEntity<VeiculoResponseDTO> insert(@RequestBody VeiculoCreateDTO dto) {
        VeiculoResponseDTO veiculo = service.insert(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(veiculo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}