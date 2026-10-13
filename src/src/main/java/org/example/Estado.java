package org.example;
import java.util.HashMap;
import java.util.Map;

public class Estado {
    private String nome;
    private Map<String, Estado> transicoes;

    public Estado(String nome) {
        this.nome = nome;
        this.transicoes = new HashMap<>();
    }

    public void adicionarTransicao(String simbolo, Estado destino) throws AutomatoInvalidoException {
        // Validação de determinismo (Exigência do trabalho)
        if (this.transicoes.containsKey(simbolo)) {
            throw new AutomatoInvalidoException("Autômato Não-Determinístico: O estado '"
                    + this.nome + "' já possui uma transição para o símbolo '" + simbolo + "'.");
        }
        this.transicoes.put(simbolo, destino);
    }

    public Estado getEstado(String simbolo) {
        return this.transicoes.get(simbolo);
    }

    public String getNome() {
        return this.nome;
    }
}
