package br.com.guerreiro_dev.backend.dto.NotaFiscalEmitida;

public record NotaFiscalDownloadDTO (
        String numeroNota,
        byte[] arquivo
){
}
