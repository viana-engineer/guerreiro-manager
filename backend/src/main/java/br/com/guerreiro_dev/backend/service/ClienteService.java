package br.com.guerreiro_dev.backend.service;

import br.com.guerreiro_dev.backend.domain.Cliente;
import br.com.guerreiro_dev.backend.dto.cliente.ClienteCreateDTO;
import br.com.guerreiro_dev.backend.dto.cliente.ClienteResponseDTO;
import br.com.guerreiro_dev.backend.mapper.ClienteMapper;
import br.com.guerreiro_dev.backend.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository repository;
    private final ClienteMapper mapper;

    public List<ClienteResponseDTO> findAll(){
        return repository.findAll()
                .stream()
                .map(mapper::toResponseDTO)
                .toList();
    }

    public ClienteResponseDTO findById(UUID id){
        return mapper.toResponseDTO(findEntityById(id));
    }

    public ClienteResponseDTO insert(ClienteCreateDTO dto){
        Cliente cliente = mapper.toEntity(dto);
        cliente = repository.save(cliente);
        return mapper.toResponseDTO(cliente);
    }

    public void delete(UUID id){
        repository.delete(
                findEntityById(id)
        );
    }

    private Cliente findEntityById(UUID id){
        return repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Cliente não encontrado com o id" + id));

    }

}
