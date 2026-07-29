package br.com.guerreiro_dev.backend.infrastructure;


import br.com.guerreiro_dev.backend.domain.Cliente;
import br.com.guerreiro_dev.backend.domain.Locacao;
import br.com.guerreiro_dev.backend.domain.NotaFiscalEmitida;
import br.com.guerreiro_dev.backend.domain.Veiculo;
import org.springframework.stereotype.Component;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.core.io.ClassPathResource;
import org.apache.pdfbox.pdmodel.font.PDFont;
import java.io.IOException;

import java.io.ByteArrayOutputStream;

@Component
public class GeradorNotaFiscal {



    public byte[] gerar(NotaFiscalEmitida nota){
        Locacao locacao = nota.getLocacao();
        Cliente cliente = locacao.getCliente();
        Veiculo veiculo = locacao.getVeiculo();
        try {
            ClassPathResource resource = new ClassPathResource(
                    "templates/modelo-nota-fiscal.pdf"
            );

            PDDocument document = Loader.loadPDF(
                    resource.getInputStream().readAllBytes()
            );

            escreverCampos(document, nota, locacao, veiculo, cliente);

            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();

            document.save(output);
            document.close();

            return output.toByteArray();


        } catch (Exception e) {
            throw new RuntimeException("Erro ao abrir template da nota fiscal", e);
        }

    }

    private void escreverCampos(
            PDDocument document,
            NotaFiscalEmitida nota,
            Locacao locacao,
            Veiculo veiculo,
            Cliente cliente

    ) throws IOException {
        PDPage page = document.getPage(0);

        PDPageContentStream contentStream =
                new PDPageContentStream(
                        document,
                        page,
                        PDPageContentStream.AppendMode.APPEND,
                        true,
                        true
                );
        escrever(
                contentStream,
                CoordenadasNotaFiscal.DATA_EMISSAO,
                nota.getDataEmissao().toString()

        );
        escrever(
                contentStream,
                CoordenadasNotaFiscal.DATA_RECEBIMENTO,
                nota.getDataEmissao().toString()
        );
        escrever(
                contentStream,
                CoordenadasNotaFiscal.NUMERO_NOTA_TOPO,
                nota.getNumeroNota()
        );
        escrever(
                contentStream,
                CoordenadasNotaFiscal.NUMERO_NOTA_MEIO,
                nota.getNumeroNota()
        );
        escrever(
                contentStream,
                CoordenadasNotaFiscal.NUMERO_NOTA_RODAPE,
                nota.getNumeroNota()
        );
        escrever(
                contentStream,
                CoordenadasNotaFiscal.VALOR_VEICULO,
                locacao.getValorVeiculo().toString()
        );
        escrever(
                contentStream,
                CoordenadasNotaFiscal.VALOR_TOTAL,
                locacao.getValorTotal().toString()
        );
        escrever(
                contentStream,
                CoordenadasNotaFiscal.DESCRICAO_VEICULO,
                veiculo.getModelo()
        );
        escrever(
                contentStream,
                CoordenadasNotaFiscal.NOME_CLIENTE,
                cliente.getNome()
        );
        escrever(
                contentStream,
                CoordenadasNotaFiscal.CPF_CLIENTE,
                cliente.getCpf()
        );



        contentStream.close();
    }

    private void escrever(
            PDPageContentStream contentStream,
            CoordenadasNotaFiscal.Posicao posicao,
            String texto
    )throws IOException {
        contentStream.beginText();

        contentStream.setFont(FONTE, 8);

        contentStream.newLineAtOffset(
                posicao.x(),
                posicao.y()
        );

        contentStream.showText(
                texto != null ? texto : ""
        );

        contentStream.endText();
    }

    private static final PDFont FONTE =
            new PDType1Font(
                    Standard14Fonts.FontName.HELVETICA
            );

    //    public byte[] gerarGradeTeste() {
//        try (
//                var template = new ClassPathResource("templates/modelo-nota-fiscal.pdf").getInputStream();
//                var output = new ByteArrayOutputStream()
//        ) {
//            PDDocument document = Loader.loadPDF(template.readAllBytes());
//            PDPage page = document.getPage(0);
//
//            PDPageContentStream cs = new PDPageContentStream(
//                    document,
//                    page,
//                    PDPageContentStream.AppendMode.APPEND,
//                    true,
//                    true
//            );
//
//            var fonte = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
//
//            cs.setFont(fonte, 6);
//
//            for (int x = 0; x <= 600; x += 30) {
//                escrever(cs, fonte, 6, x, 820, "x" + x);
//            }
//
//            for (int y = 0; y <= 800; y += 10) {
//                escrever(cs, fonte, 6, 5, y, "y" + y);
//            }
//
//            cs.close();
//
//            document.save(output);
//            document.close();
//
//            return output.toByteArray();
//
//        } catch (Exception e) {
//            throw new RuntimeException("Erro ao gerar grade de teste", e);
//        }
//    }

}
