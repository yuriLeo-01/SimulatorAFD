package org.example;

import java.util.Objects;

/**
 * Representa um passo atômico individual na execução de um autômato finito.
 */
public class PassoSimulacao {
    private final int numeroPasso;
    private final Estado estadoOrigem;
    private final String simboloLido;
    private final Estado estadoDestino;
    private final String restanteCadeia;
    private final boolean estadoFinal;
    private final String explicacao;

    public PassoSimulacao(int numeroPasso, Estado estadoOrigem, String simboloLido,
                           Estado estadoDestino, String restanteCadeia,
                           boolean estadoFinal, String explicacao) {
        this.numeroPasso = numeroPasso;
        this.estadoOrigem = estadoOrigem;
        this.simboloLido = simboloLido;
        this.estadoDestino = estadoDestino;
        this.restanteCadeia = restanteCadeia;
        this.estadoFinal = estadoFinal;
        this.explicacao = explicacao;
    }

    public int getNumeroPasso() {
        return numeroPasso;
    }

    public Estado getEstadoOrigem() {
        return estadoOrigem;
    }

    public String getSimboloLido() {
        return simboloLido;
    }

    public Estado getEstadoDestino() {
        return estadoDestino;
    }

    public String getRestanteCadeia() {
        return restanteCadeia;
    }

    public boolean isEstadoFinal() {
        return estadoFinal;
    }

    public String getExplicacao() {
        return explicacao;
    }

    @Override
    public String toString() {
        if (simboloLido == null) {
            return String.format("[Passo %d] Início no estado %s | Restante: '%s'",
                    numeroPasso, estadoDestino.getNome(), restanteCadeia);
        }
        return String.format("[Passo %d] &delta;(%s, '%s') -> %s | Restante: '%s'",
                numeroPasso,
                estadoOrigem != null ? estadoOrigem.getNome() : "?",
                simboloLido,
                estadoDestino != null ? estadoDestino.getNome() : "POÇO",
                restanteCadeia);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PassoSimulacao that = (PassoSimulacao) o;
        return numeroPasso == that.numeroPasso &&
                estadoFinal == that.estadoFinal &&
                Objects.equals(estadoOrigem, that.estadoOrigem) &&
                Objects.equals(simboloLido, that.simboloLido) &&
                Objects.equals(estadoDestino, that.estadoDestino) &&
                Objects.equals(restanteCadeia, that.restanteCadeia);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numeroPasso, estadoOrigem, simboloLido, estadoDestino, restanteCadeia, estadoFinal);
    }
}
