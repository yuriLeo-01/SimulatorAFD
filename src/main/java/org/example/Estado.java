package org.example;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Representa um estado em um Autômato Finito Determinístico.
 */
public class Estado implements Comparable<Estado> {
    private final String nome;
    private final Map<String, Estado> transicoes;

    public Estado(String nome) {
        this.nome = Objects.requireNonNull(nome, "O nome do estado não pode ser nulo").trim();
        this.transicoes = new HashMap<>();
    }

    /**
     * Adiciona uma transição determinística.
     * @param simbolo Símbolo a ser consumido
     * @param destino Estado de destino
     * @throws AutomatoInvalidoException Se o estado já possuir transição para este símbolo (garantia de determinismo)
     */
    public void adicionarTransicao(String simbolo, Estado destino) throws AutomatoInvalidoException {
        Objects.requireNonNull(simbolo, "Símbolo não pode ser nulo");
        Objects.requireNonNull(destino, "Estado destino não pode ser nulo");

        if (this.transicoes.containsKey(simbolo)) {
            throw new AutomatoInvalidoException("Autômato Não-Determinístico: O estado '"
                    + this.nome + "' já possui uma transição para o símbolo '" + simbolo
                    + "' apontando para '" + this.transicoes.get(simbolo).getNome() + "'.");
        }
        this.transicoes.put(simbolo, destino);
    }

    /**
     * Retorna o estado de destino para um símbolo, ou null se não houver transição.
     */
    public Estado getEstado(String simbolo) {
        return this.transicoes.get(simbolo);
    }

    /**
     * Verifica se o estado possui transição definida para o símbolo informado.
     */
    public boolean temTransicao(String simbolo) {
        return this.transicoes.containsKey(simbolo);
    }

    /**
     * Retorna visualização somente-leitura do mapa de transições.
     */
    public Map<String, Estado> getTransicoes() {
        return Collections.unmodifiableMap(this.transicoes);
    }

    /**
     * Retorna a lista de objetos Transicao saintes deste estado.
     */
    public List<Transicao> getListaTransicoes() {
        return this.transicoes.entrySet().stream()
                .map(e -> new Transicao(this, e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    public String getNome() {
        return this.nome;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Estado estado = (Estado) o;
        return Objects.equals(nome, estado.nome);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nome);
    }

    @Override
    public String toString() {
        return nome;
    }

    @Override
    public int compareTo(Estado o) {
        return this.nome.compareTo(o.nome);
    }
}
