package br.com.guerreiro_dev.backend.service.notafiscal;


import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class GeradorCodigoNotaFiscal {

    public String gerar(LocalDate data, Long sequencia){
        int ano = data.getYear();
        return "%d-%06d".formatted(ano, sequencia);
    }

}
