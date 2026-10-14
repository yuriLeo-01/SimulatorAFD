package org.example;

/**
 * Exceção lançada quando a definição formal ou a simulação do autômato violam as regras de um AFD.
 */
public class AutomatoInvalidoException extends Exception {
    private final Integer numeroLinha;

    public AutomatoInvalidoException(String mensagem) {
        super(mensagem);
        this.numeroLinha = null;
    }

    public AutomatoInvalidoException(String mensagem, int numeroLinha) {
        super(String.format("Linha %d: %s", numeroLinha, mensagem));
        this.numeroLinha = numeroLinha;
    }

    public AutomatoInvalidoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
        this.numeroLinha = null;
    }

    public Integer getNumeroLinha() {
        return numeroLinha;
    }
}