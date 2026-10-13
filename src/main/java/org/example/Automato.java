package org.example;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Automato {
    private Map<String, Estado> estados;
    private Set<String> alfabeto;
    private Estado estadoInicial;
    private Set<Estado> estadosFinais;

    public Automato() {
        this.estados = new HashMap<>();
        this.alfabeto = new HashSet<>();
        this.estadosFinais = new HashSet<>();
    }

    public void adicionarSimboloAlfabeto(String simbolo) {
        this.alfabeto.add(simbolo);
    }

    public void adicionarEstado(String nome) {
        this.estados.putIfAbsent(nome, new Estado(nome));
    }

    public void setEstadoInicial(String nome) throws AutomatoInvalidoException {
        if (!this.estados.containsKey(nome)) {
            throw new AutomatoInvalidoException("Erro: O estado inicial '" + nome + "' não existe nos estados declarados.");
        }
        this.estadoInicial = this.estados.get(nome);
    }

    public void adicionarEstadoFinal(String nome) throws AutomatoInvalidoException {
        if (!this.estados.containsKey(nome)) {
            throw new AutomatoInvalidoException("Erro: O estado final '" + nome + "' não existe nos estados declarados.");
        }
        this.estadosFinais.add(this.estados.get(nome));
    }

    public void adicionarTransicao(String nomeOrigem, String simbolo, String nomeDestino) throws AutomatoInvalidoException {
        if (!this.alfabeto.contains(simbolo)) {
            throw new AutomatoInvalidoException("Erro: O símbolo '" + simbolo + "' não pertence ao alfabeto definido.");
        }

        Estado origem = this.estados.get(nomeOrigem);
        Estado destino = this.estados.get(nomeDestino);

        if (origem == null || destino == null) {
            throw new AutomatoInvalidoException("Erro: Estado de origem (" + nomeOrigem + ") ou destino (" + nomeDestino + ") não encontrado.");
        }

        origem.adicionarTransicao(simbolo, destino);
    }

    public boolean verificarAceitacao(String cadeia) throws AutomatoInvalidoException {
        if (this.estadoInicial == null) {
            throw new AutomatoInvalidoException("Erro: Estado inicial não foi definido.");
        }

        Estado estadoAtual = this.estadoInicial;

        // Trata a cadeia vazia (epsilon)
        if (cadeia == null || cadeia.isEmpty()) {
            return this.estadosFinais.contains(estadoAtual);
        }

        for (char c : cadeia.toCharArray()) {
            String simbolo = String.valueOf(c);

            if (!this.alfabeto.contains(simbolo)) {
                return false; // Rejeita símbolos fora do alfabeto
            }

            Estado proximoEstado = estadoAtual.getEstado(simbolo);
            if (proximoEstado == null) {
                return false; // Caminho inexistente
            }
            estadoAtual = proximoEstado;
        }

        return this.estadosFinais.contains(estadoAtual);
    }
}