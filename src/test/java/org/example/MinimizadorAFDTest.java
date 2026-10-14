package org.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - Minimizador de AFD")
class MinimizadorAFDTest {

    @Test
    @DisplayName("Deve minimizar autômato com estados redundantes/equivalentes preservando a linguagem")
    void deveMinimizarAutomatoRedundante() throws AutomatoInvalidoException {
        // Criamos um autômato para números pares de 0's, mas com estados duplicados:
        // q0 (inicial/final), q1 (ímpar), q2 (redundante com q0, par/final)
        Automato afd = new Automato();
        afd.adicionarSimboloAlfabeto("0");
        afd.adicionarSimboloAlfabeto("1");

        afd.adicionarEstado("q0");
        afd.adicionarEstado("q1");
        afd.adicionarEstado("q2");

        afd.setEstadoInicial("q0");
        afd.adicionarEstadoFinal("q0");
        afd.adicionarEstadoFinal("q2");

        // Transições:
        // q0 com 0 vai para q1, com 1 vai para q2
        afd.adicionarTransicao("q0", "0", "q1");
        afd.adicionarTransicao("q0", "1", "q2");
        // q1 com 0 vai para q0, com 1 vai para q1
        afd.adicionarTransicao("q1", "0", "q0");
        afd.adicionarTransicao("q1", "1", "q1");
        // q2 com 0 vai para q1, com 1 vai para q0
        afd.adicionarTransicao("q2", "0", "q1");
        afd.adicionarTransicao("q2", "1", "q0");

        assertEquals(3, afd.getEstados().size());

        Automato min = MinimizadorAFD.minimizar(afd);
        assertNotNull(min);
        // Os estados q0 e q2 são equivalentes, portanto devem ser mesclados em 1 único estado!
        // O autômato mínimo deve ter exatamente 2 estados (par e ímpar).
        assertEquals(2, min.getEstados().size(), "O autômato minimizado deve possuir 2 estados");

        // As mesmas cadeias devem ser aceitas no mínimo
        assertTrue(min.verificarAceitacao(""));
        assertTrue(min.verificarAceitacao("1"));
        assertTrue(min.verificarAceitacao("00"));
        assertTrue(min.verificarAceitacao("010"));
        assertFalse(min.verificarAceitacao("0"));
        assertFalse(min.verificarAceitacao("000"));
    }
}
