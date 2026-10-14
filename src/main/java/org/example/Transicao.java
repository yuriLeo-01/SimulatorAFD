package org.example;

import java.util.Objects;

/**
 * Representa uma transição determinística entre dois estados consumindo um símbolo do alfabeto.
 * δ(origem, simbolo) = destino
 */
public class Transicao {
    private final Estado origem;
    private final String simbolo;
    private final Estado destino;

    public Transicao(Estado origem, String simbolo, Estado destino) {
        this.origem = Objects.requireNonNull(origem, "Estado de origem não pode ser nulo");
        this.simbolo = Objects.requireNonNull(simbolo, "Símbolo da transição não pode ser nulo");
        this.destino = Objects.requireNonNull(destino, "Estado de destino não pode ser nulo");
    }

    public Estado getOrigem() {
        return this.origem;
    }

    public String getSimbolo() {
        return this.simbolo;
    }

    public Estado getDestino() {
        return this.destino;
    }

    public String toLinhaArquivo() {
        return String.format("%s %s %s", origem.getNome(), simbolo, destino.getNome());
    }

    @Override
    public String toString() {
        return String.format("&delta;(%s, '%s') &rarr; %s", origem.getNome(), simbolo, destino.getNome());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transicao transicao = (Transicao) o;
        return Objects.equals(origem, transicao.origem) &&
                Objects.equals(simbolo, transicao.simbolo) &&
                Objects.equals(destino, transicao.destino);
    }

    @Override
    public int hashCode() {
        return Objects.hash(origem, simbolo, destino);
    }
}