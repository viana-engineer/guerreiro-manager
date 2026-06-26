package br.com.guerreiro_dev.backend.controller;

import br.com.guerreiro_dev.backend.dto.cliente.ClienteCreateDTO;
import br.com.guerreiro_dev.backend.dto.cliente.ClienteResponseDTO;
import br.com.guerreiro_dev.backend.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService service;

    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> findAll(){
        List<ClienteResponseDTO> clientes = service.findAll();
        return ResponseEntity.ok().body(clientes);
    }

    @GetMapping ("/{id}")
    public ResponseEntity<ClienteResponseDTO> findById(@PathVariable UUID id){
        ClienteResponseDTO dto = service.findById(id);
        return ResponseEntity.ok().body(dto);
    }

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> insert(@RequestBody ClienteCreateDTO dto){
        ClienteResponseDTO cliente = service.insert(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(cliente);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

}
