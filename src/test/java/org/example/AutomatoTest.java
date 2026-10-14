package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - Automato Core")
class AutomatoTest {

    private Automato automato;

    @BeforeEach
    void setUp() throws AutomatoInvalidoException {
        // Autômato que reconhece cadeias binárias terminadas em '01'
        // (mesmo do arquivo automato.txt)
        automato = new Automato();
        automato.adicionarSimboloAlfabeto("0");
        automato.adicionarSimboloAlfabeto("1");

        automato.adicionarEstado("q0");
        automato.adicionarEstado("q1");
        automato.adicionarEstado("q2");

        automato.setEstadoInicial("q0");
        automato.adicionarEstadoFinal("q2");

        automato.adicionarTransicao("q0", "0", "q1");
        automato.adicionarTransicao("q0", "1", "q0");
        automato.adicionarTransicao("q1", "0", "q1");
        automato.adicionarTransicao("q1", "1", "q2");
        automato.adicionarTransicao("q2", "0", "q1");
        automato.adicionarTransicao("q2", "1", "q0");
    }

    @Test
    @DisplayName("Deve aceitar cadeias válidas que terminam em '01'")
    void deveAceitarCadeiasValidas() throws AutomatoInvalidoException {
        assertTrue(automato.verificarAceitacao("01"));
        assertTrue(automato.verificarAceitacao("001"));
        assertTrue(automato.verificarAceitacao("101"));
        assertTrue(automato.verificarAceitacao("0101"));
        assertTrue(automato.verificarAceitacao("111001"));
    }

    @Test
    @DisplayName("Deve rejeitar cadeias que não terminam em '01'")
    void deveRejeitarCadeiasInvalidas() throws AutomatoInvalidoException {
        assertFalse(automato.verificarAceitacao("0"));
        assertFalse(automato.verificarAceitacao("1"));
        assertFalse(automato.verificarAceitacao("00"));
        assertFalse(automato.verificarAceitacao("10"));
        assertFalse(automato.verificarAceitacao("010"));
        assertFalse(automato.verificarAceitacao("111"));
    }

    @Test
    @DisplayName("Deve tratar cadeia vazia corretamente (rejeita se q0 não for final)")
    void deveTratarCadeiaVazia() throws AutomatoInvalidoException {
        assertFalse(automato.verificarAceitacao(""));
        assertFalse(automato.verificarAceitacao(null));

        ResultadoSimulacao res = automato.executarSimulacao("");
        assertEquals(StatusSimulacao.REJEITADA_ESTADO_NAO_FINAL, res.getStatus());
        assertFalse(res.isAceita());
    }

    @Test
    @DisplayName("Garantia de determinismo: deve lançar exceção ao tentar adicionar transição duplicada")
    void deveGarantirDeterminismo() {
        AutomatoInvalidoException ex = assertThrows(AutomatoInvalidoException.class, () -> {
            automato.adicionarTransicao("q0", "0", "q2");
        });
        assertTrue(ex.getMessage().contains("Não-Determinístico"));
    }

    @Test
    @DisplayName("Deve rejeitar símbolo fora do alfabeto com status apropriado")
    void deveRejeitarSimboloInvalido() throws AutomatoInvalidoException {
        ResultadoSimulacao res = automato.executarSimulacao("01a");
        assertFalse(res.isAceita());
        assertEquals(StatusSimulacao.REJEITADA_SIMBOLO_INVALIDO, res.getStatus());
        assertTrue(res.getMensagemDetalhada().contains("não faz parte do alfabeto"));
    }

    @Test
    @DisplayName("Deve gerar trace passo a passo com caminho e detalhes de cada estado")
    void deveGerarTraceCompleto() throws AutomatoInvalidoException {
        ResultadoSimulacao res = automato.executarSimulacao("01");
        assertTrue(res.isAceita());
        assertEquals(StatusSimulacao.ACEITA, res.getStatus());
        assertEquals(3, res.getPassos().size()); // Passo 0 (início), Passo 1 ('0' -> q1), Passo 2 ('1' -> q2)
        assertEquals("q0 -> q1 -> q2", res.getCaminhoFormatado());
    }

    @Test
    @DisplayName("Deve validar se o AFD é completo")
    void deveValidarCompletude() {
        assertTrue(automato.isCompleto());
    }

    @Test
    @DisplayName("Deve detectar estados inalcançáveis quando existirem")
    void deveDetectarEstadosInalcancaveis() throws AutomatoInvalidoException {
        automato.adicionarEstado("qFantasma");
        Set<Estado> inalcancaveis = automato.obterEstadosInalcancaveis();
        assertEquals(1, inalcancaveis.size());
        assertTrue(inalcancaveis.contains(automato.getEstado("qFantasma")));
    }

    @Test
    @DisplayName("Deve exportar formato DOT do Graphviz contendo nós e arestas formatadas")
    void deveExportarDOT() {
        String dot = automato.exportarDOT();
        assertNotNull(dot);
        assertTrue(dot.contains("digraph AFD"));
        assertTrue(dot.contains("q0"));
        assertTrue(dot.contains("q2"));
        assertTrue(dot.contains("doublecircle"));
    }
}
