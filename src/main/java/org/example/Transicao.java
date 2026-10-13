package org.example;
public class Transicao {
    private Estado origem;
    private String simbolo;
    private Estado destino;

    public Transicao(Estado origem, String simbolo, Estado destino) {
        this.origem = origem;
        this.simbolo = simbolo;
        this.destino = destino;
    }

    public Estado getOrigem() { return this.origem; }
    public String getSimbolo() { return this.simbolo; }
    public Estado getDestino() { return this.destino; }
}