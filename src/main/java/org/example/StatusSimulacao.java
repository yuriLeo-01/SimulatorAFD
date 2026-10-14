package org.example;

/**
 * Representa os possíveis estados do resultado de uma simulação de cadeia em um AFD.
 */
public enum StatusSimulacao {
    EM_EXECUCAO("Em execução"),
    ACEITA("Cadeia Aceita"),
    REJEITADA_ESTADO_NAO_FINAL("Rejeitada: parou em estado de não-aceitação"),
    REJEITADA_SIMBOLO_INVALIDO("Rejeitada: símbolo não pertence ao alfabeto"),
    REJEITADA_TRANSICAO_INEXISTENTE("Rejeitada: transição indefinida (estado poço)");

    private final String descricao;

    StatusSimulacao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isAceita() {
        return this == ACEITA;
    }
}
