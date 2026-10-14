package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Motor de execução e depurador de Autômatos Finitos Determinísticos.
 * Suporta simulação direta, testes em lote e controle de execução passo a passo (Step-by-Step).
 */
public class Simulador {
    private final Automato automato;

    // Estado do depurador interativo passo a passo
    private ResultadoSimulacao resultadoAtual;
    private int indicePassoAtual;

    public Simulador(Automato automato) {
        this.automato = Objects.requireNonNull(automato, "O autômato não pode ser nulo para o simulador");
        this.indicePassoAtual = 0;
    }

    /**
     * Simula a aceitação de uma cadeia (compatibilidade com interface original).
     */
    public boolean simular(String cadeia) throws AutomatoInvalidoException {
        return this.automato.verificarAceitacao(cadeia);
    }

    /**
     * Executa a cadeia gerando o trace de execução completo.
     */
    public ResultadoSimulacao executar(String cadeia) throws AutomatoInvalidoException {
        ResultadoSimulacao res = this.automato.executarSimulacao(cadeia);
        this.resultadoAtual = res;
        this.indicePassoAtual = 0;
        return res;
    }

    /**
     * Executa uma bateria de testes com múltiplas cadeias e retorna o mapa de resultados.
     */
    public Map<String, ResultadoSimulacao> simularEmLote(List<String> cadeias) throws AutomatoInvalidoException {
        Map<String, ResultadoSimulacao> resultados = new LinkedHashMap<>();
        if (cadeias == null) return resultados;

        for (String c : cadeias) {
            resultados.put(c, this.automato.executarSimulacao(c));
        }
        return Collections.unmodifiableMap(resultados);
    }

    // --- CONTROLES DE DEPURAÇÃO PASSO A PASSO (STEP-BY-STEP) ---

    /**
     * Prepara o simulador para depurar uma cadeia passo a passo.
     */
    public void carregarCadeia(String cadeia) throws AutomatoInvalidoException {
        this.resultadoAtual = this.automato.executarSimulacao(cadeia);
        this.indicePassoAtual = 0;
    }

    /**
     * Avança um passo na execução.
     */
    public PassoSimulacao passoSeguinte() {
        if (resultadoAtual == null || resultadoAtual.getPassos().isEmpty()) {
            return null;
        }
        if (indicePassoAtual < resultadoAtual.getPassos().size() - 1) {
            indicePassoAtual++;
        }
        return getPassoAtual();
    }

    /**
     * Retrocede um passo na execução.
     */
    public PassoSimulacao passoAnterior() {
        if (resultadoAtual == null || resultadoAtual.getPassos().isEmpty()) {
            return null;
        }
        if (indicePassoAtual > 0) {
            indicePassoAtual--;
        }
        return getPassoAtual();
    }

    /**
     * Reinicia a depuração para o passo zero (estado inicial).
     */
    public PassoSimulacao reiniciar() {
        this.indicePassoAtual = 0;
        return getPassoAtual();
    }

    /**
     * Salta diretamente para o último passo da simulação.
     */
    public PassoSimulacao irParaFim() {
        if (resultadoAtual != null && !resultadoAtual.getPassos().isEmpty()) {
            this.indicePassoAtual = resultadoAtual.getPassos().size() - 1;
        }
        return getPassoAtual();
    }

    public PassoSimulacao getPassoAtual() {
        if (resultadoAtual == null || resultadoAtual.getPassos().isEmpty()) {
            return null;
        }
        return resultadoAtual.getPassos().get(indicePassoAtual);
    }

    public int getIndicePassoAtual() {
        return indicePassoAtual;
    }

    public int getTotalPassos() {
        return resultadoAtual != null ? resultadoAtual.getPassos().size() : 0;
    }

    public boolean isFinalizado() {
        if (resultadoAtual == null || resultadoAtual.getPassos().isEmpty()) {
            return true;
        }
        return indicePassoAtual == resultadoAtual.getPassos().size() - 1;
    }

    public ResultadoSimulacao getResultadoAtual() {
        return resultadoAtual;
    }

    public Automato getAutomato() {
        return automato;
    }
}