package br.com.guerreiro_dev.backend.service;

import br.com.guerreiro_dev.backend.domain.Cliente;
import br.com.guerreiro_dev.backend.domain.Locacao;
import br.com.guerreiro_dev.backend.domain.Veiculo;
import br.com.guerreiro_dev.backend.dto.locacao.LocacaoCreateDTO;
import br.com.guerreiro_dev.backend.dto.locacao.LocacaoResponseDTO;
import br.com.guerreiro_dev.backend.mapper.LocacaoMapper;
import br.com.guerreiro_dev.backend.repository.ClienteRepository;
import br.com.guerreiro_dev.backend.repository.LocacaoRepository;
import br.com.guerreiro_dev.backend.repository.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocacaoService {

    private final LocacaoRepository repository;
    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;
    private final LocacaoMapper mapper;

    public List<LocacaoResponseDTO> findAll(){
        return repository.findAll()
                .stream()
                .map(mapper::toResponseDTO)
                .toList();
    }

    public LocacaoResponseDTO findById(UUID id){
        return mapper.toResponseDTO(findEntityById(id));
    }

    public LocacaoResponseDTO insert(LocacaoCreateDTO dto){
        Cliente cliente = clienteRepository.findById(dto.clienteId())
                .orElseThrow();

        Veiculo veiculo = veiculoRepository.findById(dto.veiculoId())
                .orElseThrow();

        Locacao locacao = mapper.toEntity(dto,cliente,veiculo);

        locacao.calcularValorTotal();

        locacao =repository.save(locacao);

        return mapper.toResponseDTO(locacao);

    }

    public void delete(UUID id){
        repository.delete(findEntityById(id));
    }



    private Locacao findEntityById(UUID id){
        return repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Locação não encontrado com o id: " + id));

    }

}
