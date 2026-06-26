package br.com.guerreiro_dev.backend.service;

import br.com.guerreiro_dev.backend.domain.Veiculo;
import br.com.guerreiro_dev.backend.dto.veiculo.VeiculoCreateDTO;
import br.com.guerreiro_dev.backend.dto.veiculo.VeiculoResponseDTO;
import br.com.guerreiro_dev.backend.mapper.VeiculoMapper;
import br.com.guerreiro_dev.backend.repository.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VeiculoService {

    private final VeiculoRepository repository;
    private final VeiculoMapper mapper;

    public List<VeiculoResponseDTO> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponseDTO)
                .toList();
    }

    public VeiculoResponseDTO findById(UUID id) {
        return mapper.toResponseDTO(findEntityById(id));
    }

    public VeiculoResponseDTO insert(VeiculoCreateDTO dto) {
        Veiculo veiculo = mapper.toEntity(dto);
        veiculo = repository.save(veiculo);
        return mapper.toResponseDTO(veiculo);
    }

    public void delete(UUID id) {
        repository.delete(findEntityById(id));
    }

    private Veiculo findEntityById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Veículo não encontrado com o id: " + id));
    }
}