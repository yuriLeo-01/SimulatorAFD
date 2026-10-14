package org.example;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contém o resultado integral e auditável de uma simulação de cadeia em um AFD.
 */
public class ResultadoSimulacao {
    private final String cadeiaEntrada;
    private final StatusSimulacao status;
    private final List<PassoSimulacao> passos;
    private final Estado estadoFinal;
    private final String mensagemDetalhada;
    private final long tempoExecucaoNanos;

    public ResultadoSimulacao(String cadeiaEntrada, StatusSimulacao status,
                              List<PassoSimulacao> passos, Estado estadoFinal,
                              String mensagemDetalhada, long tempoExecucaoNanos) {
        this.cadeiaEntrada = cadeiaEntrada;
        this.status = status;
        this.passos = passos != null ? List.copyOf(passos) : Collections.emptyList();
        this.estadoFinal = estadoFinal;
        this.mensagemDetalhada = mensagemDetalhada;
        this.tempoExecucaoNanos = tempoExecucaoNanos;
    }

    public boolean isAceita() {
        return status.isAceita();
    }

    public String getCadeiaEntrada() {
        return cadeiaEntrada;
    }

    public StatusSimulacao getStatus() {
        return status;
    }

    public List<PassoSimulacao> getPassos() {
        return passos;
    }

    public Estado getEstadoFinal() {
        return estadoFinal;
    }

    public String getMensagemDetalhada() {
        return mensagemDetalhada;
    }

    public long getTempoExecucaoNanos() {
        return tempoExecucaoNanos;
    }

    /**
     * Retorna a sequência de estados percorridos no formato: q0 -> q1 -> q2
     */
    public String getCaminhoFormatado() {
        if (passos.isEmpty()) return "";
        return passos.stream()
                .map(p -> p.getEstadoDestino() != null ? p.getEstadoDestino().getNome() : "POÇO")
                .collect(Collectors.joining(" -> "));
    }

    @Override
    public String toString() {
        return String.format("ResultadoSimulacao{cadeia='%s', status=%s, final=%s, passos=%d, caminho='%s'}",
                cadeiaEntrada,
                status,
                estadoFinal != null ? estadoFinal.getNome() : "nenhum",
                passos.size(),
                getCaminhoFormatado());
    }
}
