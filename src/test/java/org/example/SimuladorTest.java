package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - Motor do Simulador e Depurador")
class SimuladorTest {

    private Simulador simulador;

    @BeforeEach
    void setUp() throws AutomatoInvalidoException {
        Automato automato = new Automato();
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

        simulador = new Simulador(automato);
    }

    @Test
    @DisplayName("Compatibilidade: método simular deve responder booleanamente")
    void deveSimularCompatibilidade() throws AutomatoInvalidoException {
        assertTrue(simulador.simular("01"));
        assertFalse(simulador.simular("00"));
    }

    @Test
    @DisplayName("Depuração interativa: deve avançar e retroceder passos corretamente")
    void deveControlarPassoAPasso() throws AutomatoInvalidoException {
        simulador.carregarCadeia("01");

        // Passo 0: Inicial (q0)
        PassoSimulacao p0 = simulador.getPassoAtual();
        assertEquals(0, simulador.getIndicePassoAtual());
        assertEquals("q0", p0.getEstadoDestino().getNome());
        assertFalse(simulador.isFinalizado());

        // Passo 1: Leu '0', foi para q1
        PassoSimulacao p1 = simulador.passoSeguinte();
        assertEquals(1, simulador.getIndicePassoAtual());
        assertEquals("q1", p1.getEstadoDestino().getNome());
        assertEquals("0", p1.getSimboloLido());

        // Passo 2: Leu '1', foi para q2 (final)
        PassoSimulacao p2 = simulador.passoSeguinte();
        assertEquals(2, simulador.getIndicePassoAtual());
        assertEquals("q2", p2.getEstadoDestino().getNome());
        assertTrue(p2.isEstadoFinal());
        assertTrue(simulador.isFinalizado());

        // Retroceder para passo 1
        PassoSimulacao pRecuo = simulador.passoAnterior();
        assertEquals(1, simulador.getIndicePassoAtual());
        assertEquals("q1", pRecuo.getEstadoDestino().getNome());

        // Reiniciar
        simulador.reiniciar();
        assertEquals(0, simulador.getIndicePassoAtual());
        assertEquals("q0", simulador.getPassoAtual().getEstadoDestino().getNome());

        // Ir para fim
        simulador.irParaFim();
        assertEquals(2, simulador.getIndicePassoAtual());
        assertTrue(simulador.isFinalizado());
    }

    @Test
    @DisplayName("Simulação em lote: deve processar lista de palavras e retornar mapa de resultados")
    void deveSimularEmLote() throws AutomatoInvalidoException {
        List<String> palavras = List.of("01", "001", "10", "111", "0101");
        Map<String, ResultadoSimulacao> mapa = simulador.simularEmLote(palavras);

        assertEquals(5, mapa.size());
        assertTrue(mapa.get("01").isAceita());
        assertTrue(mapa.get("001").isAceita());
        assertFalse(mapa.get("10").isAceita());
        assertFalse(mapa.get("111").isAceita());
        assertTrue(mapa.get("0101").isAceita());
    }
}
