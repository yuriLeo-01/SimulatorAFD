package org.example;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

/**
 * Representa a 5-tupla formal de um Autômato Finito Determinístico:
 * M = (Q, &Sigma;, &delta;, q&#8320;, F)
 */
public class Automato {
    private final Map<String, Estado> estados;
    private final Set<String> alfabeto;
    private Estado estadoInicial;
    private final Set<Estado> estadosFinais;

    public Automato() {
        this.estados = new LinkedHashMap<>();
        this.alfabeto = new LinkedHashSet<>();
        this.estadosFinais = new LinkedHashSet<>();
    }

    public void adicionarSimboloAlfabeto(String simbolo) {
        if (simbolo != null && !simbolo.trim().isEmpty()) {
            this.alfabeto.add(simbolo.trim());
        }
    }

    public void adicionarEstado(String nome) {
        if (nome != null && !nome.trim().isEmpty()) {
            String nomeTrim = nome.trim();
            this.estados.putIfAbsent(nomeTrim, new Estado(nomeTrim));
        }
    }

    public void setEstadoInicial(String nome) throws AutomatoInvalidoException {
        if (nome == null || nome.trim().isEmpty()) {
            throw new AutomatoInvalidoException("Erro: Nome do estado inicial não pode ser vazio.");
        }
        String nomeTrim = nome.trim();
        if (!this.estados.containsKey(nomeTrim)) {
            throw new AutomatoInvalidoException("Erro: O estado inicial '" + nomeTrim + "' não existe nos estados declarados.");
        }
        this.estadoInicial = this.estados.get(nomeTrim);
    }

    public void adicionarEstadoFinal(String nome) throws AutomatoInvalidoException {
        if (nome == null || nome.trim().isEmpty()) {
            throw new AutomatoInvalidoException("Erro: Nome do estado final não pode ser vazio.");
        }
        String nomeTrim = nome.trim();
        if (!this.estados.containsKey(nomeTrim)) {
            throw new AutomatoInvalidoException("Erro: O estado final '" + nomeTrim + "' não existe nos estados declarados.");
        }
        this.estadosFinais.add(this.estados.get(nomeTrim));
    }

    public void adicionarTransicao(String nomeOrigem, String simbolo, String nomeDestino) throws AutomatoInvalidoException {
        if (simbolo == null || simbolo.trim().isEmpty()) {
            throw new AutomatoInvalidoException("Erro: O símbolo da transição não pode ser nulo ou vazio.");
        }
        String simTrim = simbolo.trim();
        if (!this.alfabeto.contains(simTrim)) {
            throw new AutomatoInvalidoException("Erro: O símbolo '" + simTrim + "' não pertence ao alfabeto definido: " + this.alfabeto);
        }

        Estado origem = this.estados.get(nomeOrigem != null ? nomeOrigem.trim() : null);
        Estado destino = this.estados.get(nomeDestino != null ? nomeDestino.trim() : null);

        if (origem == null || destino == null) {
            throw new AutomatoInvalidoException("Erro: Estado de origem (" + nomeOrigem + ") ou destino (" + nomeDestino + ") não encontrado.");
        }

        origem.adicionarTransicao(simTrim, destino);
    }

    /**
     * Valida se a cadeia é aceita pelo autômato (compatibilidade retroativa).
     */
    public boolean verificarAceitacao(String cadeia) throws AutomatoInvalidoException {
        ResultadoSimulacao resultado = executarSimulacao(cadeia);
        return resultado.isAceita();
    }

    /**
     * Executa a simulação completa gerando o ExecutionTrace detalhado passo a passo.
     */
    public ResultadoSimulacao executarSimulacao(String cadeia) throws AutomatoInvalidoException {
        if (this.estadoInicial == null) {
            throw new AutomatoInvalidoException("Erro: Estado inicial não foi definido no autômato.");
        }

        long inicioNanos = System.nanoTime();
        List<PassoSimulacao> passos = new ArrayList<>();
        String cadeiaSegura = cadeia != null ? cadeia : "";
        Estado estadoAtual = this.estadoInicial;

        // Passo 0: Estado inicial
        passos.add(new PassoSimulacao(
                0,
                null,
                null,
                estadoAtual,
                cadeiaSegura,
                this.estadosFinais.contains(estadoAtual),
                "Iniciando no estado inicial " + estadoAtual.getNome()
        ));

        // Trata a cadeia vazia (&epsilon;)
        if (cadeiaSegura.isEmpty()) {
            boolean aceita = this.estadosFinais.contains(estadoAtual);
            StatusSimulacao status = aceita ? StatusSimulacao.ACEITA : StatusSimulacao.REJEITADA_ESTADO_NAO_FINAL;
            String msg = aceita
                    ? "Cadeia vazia (ε) aceita: o estado inicial é de aceitação."
                    : "Cadeia vazia (ε) rejeitada: o estado inicial não é de aceitação.";
            long fimNanos = System.nanoTime();
            return new ResultadoSimulacao(cadeiaSegura, status, passos, estadoAtual, msg, fimNanos - inicioNanos);
        }

        char[] caracteres = cadeiaSegura.toCharArray();
        for (int i = 0; i < caracteres.length; i++) {
            String simbolo = String.valueOf(caracteres[i]);
            String restante = (i + 1 < caracteres.length) ? cadeiaSegura.substring(i + 1) : "";

            // 1. Validação de pertinência ao alfabeto
            if (!this.alfabeto.contains(simbolo)) {
                passos.add(new PassoSimulacao(
                        i + 1,
                        estadoAtual,
                        simbolo,
                        null,
                        restante,
                        false,
                        "Símbolo '" + simbolo + "' não pertence ao alfabeto Σ."
                ));
                long fimNanos = System.nanoTime();
                return new ResultadoSimulacao(
                        cadeiaSegura,
                        StatusSimulacao.REJEITADA_SIMBOLO_INVALIDO,
                        passos,
                        estadoAtual,
                        "Erro léxico: o símbolo '" + simbolo + "' na posição " + i + " não faz parte do alfabeto " + this.alfabeto,
                        fimNanos - inicioNanos
                );
            }

            // 2. Busca pela transição determinística
            Estado proximo = estadoAtual.getEstado(simbolo);
            if (proximo == null) {
                passos.add(new PassoSimulacao(
                        i + 1,
                        estadoAtual,
                        simbolo,
                        null,
                        restante,
                        false,
                        "Nenhuma transição definida a partir de " + estadoAtual.getNome() + " com '" + simbolo + "' (Poço)."
                ));
                long fimNanos = System.nanoTime();
                return new ResultadoSimulacao(
                        cadeiaSegura,
                        StatusSimulacao.REJEITADA_TRANSICAO_INEXISTENTE,
                        passos,
                        estadoAtual,
                        "Transição indefinida: o autômato parou em estado poço no nó " + estadoAtual.getNome() + " com o símbolo '" + simbolo + "'.",
                        fimNanos - inicioNanos
                );
            }

            boolean isFinal = (i == caracteres.length - 1) && this.estadosFinais.contains(proximo);
            passos.add(new PassoSimulacao(
                    i + 1,
                    estadoAtual,
                    simbolo,
                    proximo,
                    restante,
                    this.estadosFinais.contains(proximo),
                    String.format("δ(%s, '%s') -> %s", estadoAtual.getNome(), simbolo, proximo.getNome())
            ));

            estadoAtual = proximo;
        }

        boolean aceita = this.estadosFinais.contains(estadoAtual);
        StatusSimulacao status = aceita ? StatusSimulacao.ACEITA : StatusSimulacao.REJEITADA_ESTADO_NAO_FINAL;
        String msg = aceita
                ? "Cadeia aceita com sucesso: execução concluída no estado final " + estadoAtual.getNome() + "."
                : "Cadeia rejeitada: execução concluída no estado " + estadoAtual.getNome() + ", que não é de aceitação.";

        long fimNanos = System.nanoTime();
        return new ResultadoSimulacao(cadeiaSegura, status, passos, estadoAtual, msg, fimNanos - inicioNanos);
    }

    /**
     * Verifica se o AFD é completo (função de transição total para todo par estado x símbolo).
     */
    public boolean isCompleto() {
        for (Estado e : this.estados.values()) {
            for (String s : this.alfabeto) {
                if (!e.temTransicao(s)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Lista os pares (estado, símbolo) sem transição definida, no formato "δ(q1, 1)".
     * Lista vazia significa que o AFD é completo.
     */
    public List<String> getTransicoesFaltantes() {
        List<String> faltantes = new ArrayList<>();
        for (Estado e : this.estados.values()) {
            for (String s : this.alfabeto) {
                if (!e.temTransicao(s)) {
                    faltantes.add("δ(" + e.getNome() + ", " + s + ")");
                }
            }
        }
        return faltantes;
    }

    /**
     * Identifica estados inalcançáveis a partir do estado inicial usando busca em largura (BFS).
     */
    public Set<Estado> obterEstadosInalcancaveis() {
        if (this.estadoInicial == null) {
            return new HashSet<>(this.estados.values());
        }

        Set<Estado> alcancaveis = new HashSet<>();
        Queue<Estado> fila = new ArrayDeque<>();

        alcancaveis.add(this.estadoInicial);
        fila.add(this.estadoInicial);

        while (!fila.isEmpty()) {
            Estado atual = fila.poll();
            for (Estado destino : atual.getTransicoes().values()) {
                if (alcancaveis.add(destino)) {
                    fila.add(destino);
                }
            }
        }

        Set<Estado> inalcancaveis = new HashSet<>(this.estados.values());
        inalcancaveis.removeAll(alcancaveis);
        return inalcancaveis;
    }

    /**
     * Identifica estados mortos (estados que não conseguem alcançar nenhum estado final).
     */
    public Set<Estado> obterEstadosMortos() {
        Set<Estado> vivos = new HashSet<>(this.estadosFinais);
        boolean mudou = true;

        // Propagação reversa de vivacidade
        while (mudou) {
            mudou = false;
            for (Estado e : this.estados.values()) {
                if (!vivos.contains(e)) {
                    for (Estado dest : e.getTransicoes().values()) {
                        if (vivos.contains(dest)) {
                            vivos.add(e);
                            mudou = true;
                            break;
                        }
                    }
                }
            }
        }

        Set<Estado> mortos = new HashSet<>(this.estados.values());
        mortos.removeAll(vivos);
        return mortos;
    }

    /**
     * Retorna todas as transições do autômato reunidas em uma lista.
     */
    public List<Transicao> getTodasTransicoes() {
        List<Transicao> todas = new ArrayList<>();
        for (Estado e : this.estados.values()) {
            todas.addAll(e.getListaTransicoes());
        }
        return Collections.unmodifiableList(todas);
    }

    /**
     * Exporta o autômato para o formato Graphviz DOT para visualização gráfica.
     */
    public String exportarDOT() {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph AFD {\n");
        sb.append("    rankdir=LR;\n");
        sb.append("    node [shape=circle, fontname=\"Inter, sans-serif\"];\n");
        sb.append("    edge [fontname=\"Inter, sans-serif\"];\n\n");

        // Estado inicial com seta invisível de entrada
        sb.append("    __start__ [shape=point, style=invis];\n");
        if (estadoInicial != null) {
            sb.append("    __start__ -> ").append(estadoInicial.getNome()).append(" [label=\"start\"];\n\n");
        }

        // Estados finais (círculo duplo)
        if (!estadosFinais.isEmpty()) {
            sb.append("    node [shape=doublecircle];\n");
            for (Estado f : estadosFinais) {
                sb.append("    ").append(f.getNome()).append(";\n");
            }
            sb.append("    node [shape=circle];\n\n");
        }

        // Transições agrupadas por par (origem -> destino)
        Map<String, List<String>> arestasAgrupadas = new LinkedHashMap<>();
        for (Transicao t : getTodasTransicoes()) {
            String chave = t.getOrigem().getNome() + "->" + t.getDestino().getNome();
            arestasAgrupadas.computeIfAbsent(chave, k -> new ArrayList<>()).add(t.getSimbolo());
        }

        for (Map.Entry<String, List<String>> entry : arestasAgrupadas.entrySet()) {
            String[] partes = entry.getKey().split("->");
            String rotulo = String.join(", ", entry.getValue());
            sb.append("    ").append(partes[0]).append(" -> ").append(partes[1])
                    .append(" [label=\"").append(rotulo).append("\"];\n");
        }

        sb.append("}\n");
        return sb.toString();
    }

    /**
     * Exporta o autômato no formato padrão texto (.txt) utilizado no trabalho.
     */
    public String exportarFormatoArquivo() {
        StringBuilder sb = new StringBuilder();
        // Linha 1: Alfabeto
        sb.append(String.join(" ", this.alfabeto)).append("\n");
        // Linha 2: Estados
        sb.append(String.join(" ", this.estados.keySet())).append("\n");
        // Linha 3: Estado inicial
        sb.append(this.estadoInicial != null ? this.estadoInicial.getNome() : "").append("\n");
        // Linha 4: Estados finais
        Set<String> nomesFinais = new TreeSet<>();
        for (Estado f : this.estadosFinais) {
            nomesFinais.add(f.getNome());
        }
        sb.append(String.join(" ", nomesFinais)).append("\n");
        // Linhas 5+: Transições
        for (Transicao t : getTodasTransicoes()) {
            sb.append(t.toLinhaArquivo()).append("\n");
        }
        return sb.toString();
    }

    // --- GETTERS ---
    public Map<String, Estado> getEstados() {
        return Collections.unmodifiableMap(this.estados);
    }

    public Set<String> getAlfabeto() {
        return Collections.unmodifiableSet(this.alfabeto);
    }

    public Estado getEstadoInicial() {
        return this.estadoInicial;
    }

    public Set<Estado> getEstadosFinais() {
        return Collections.unmodifiableSet(this.estadosFinais);
    }

    public Estado getEstado(String nome) {
        return this.estados.get(nome != null ? nome.trim() : null);
    }

    public boolean contemEstado(String nome) {
        return this.estados.containsKey(nome != null ? nome.trim() : null);
    }
}