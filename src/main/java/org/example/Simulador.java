package org.example;
public class Simulador {
    private Automato automato;

    public Simulador(Automato automato) {
        this.automato = automato;
    }

    public boolean simular(String cadeia) throws AutomatoInvalidoException {
        return this.automato.verificarAceitacao(cadeia);
    }
}