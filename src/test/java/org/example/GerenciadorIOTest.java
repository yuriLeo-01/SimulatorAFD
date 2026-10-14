package org.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - Gerenciador I/O")
class GerenciadorIOTest {

    @Test
    @DisplayName("Deve carregar autômato com sucesso a partir de arquivo real 'src/automato.txt'")
    void deveCarregarArquivoOriginal() throws Exception {
        File arquivo = new File("src/automato.txt");
        assertTrue(arquivo.exists(), "src/automato.txt deve existir");

        Automato afd = GerenciadorIO.montarPorArquivo(arquivo);
        assertNotNull(afd);
        assertEquals(2, afd.getAlfabeto().size());
        assertEquals(3, afd.getEstados().size());
        assertEquals("q0", afd.getEstadoInicial().getNome());
        assertEquals(1, afd.getEstadosFinais().size());

        assertTrue(afd.verificarAceitacao("01"));
        assertFalse(afd.verificarAceitacao("10"));
    }

    @Test
    @DisplayName("Deve ignorar comentários (# ou //) e linhas em branco no parser")
    void deveIgnorarComentariosELinhasVazias() throws AutomatoInvalidoException {
        String texto = """
                # Linha de comentário no topo
                0 1 // Alfabeto binário
                
                # Conjunto de estados
                qA qB
                
                qA # Estado inicial
                qB // Estado final
                
                # Transições
                qA 0 qB
                qA 1 qA
                qB 0 qB
                qB 1 qA
                """;

        Automato afd = GerenciadorIO.montarPorTexto(texto);
        assertNotNull(afd);
        assertEquals(2, afd.getEstados().size());
        assertEquals("qA", afd.getEstadoInicial().getNome());
        assertTrue(afd.verificarAceitacao("0"));
        assertFalse(afd.verificarAceitacao("1"));
    }

    @Test
    @DisplayName("Deve lançar exceção se transição tiver número incorreto de elementos")
    void deveDetectarTransicaoInvalida() {
        String textoInvalido = """
                0 1
                q0 q1
                q0
                q1
                q0 0 # Faltando destino
                """;

        assertThrows(AutomatoInvalidoException.class, () -> {
            GerenciadorIO.montarPorTexto(textoInvalido);
        });
    }

    @Test
    @DisplayName("Deve salvar e recarregar autômato em arquivo temporário preservando comportamento")
    void deveSalvarERecarregar(@TempDir Path tempDir) throws IOException, AutomatoInvalidoException {
        Automato afdOriginal = GerenciadorIO.montarPorArquivo(new File("src/automato.txt"));
        File tempFile = tempDir.resolve("salvo.txt").toFile();

        GerenciadorIO.salvarEmArquivo(afdOriginal, tempFile);
        assertTrue(tempFile.exists());

        Automato afdRecarregado = GerenciadorIO.montarPorArquivo(tempFile);
        assertEquals(afdOriginal.getAlfabeto(), afdRecarregado.getAlfabeto());
        assertEquals(afdOriginal.getEstados().keySet(), afdRecarregado.getEstados().keySet());
        assertEquals(afdOriginal.getEstadoInicial().getNome(), afdRecarregado.getEstadoInicial().getNome());
        assertTrue(afdRecarregado.verificarAceitacao("0101"));
    }
}
