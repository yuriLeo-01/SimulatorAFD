package org.example;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementa a Minimização de Autômatos Finitos Determinísticos
 * utilizando o algoritmo de refinamento de partições (Equivalência de Moore / Myhill-Nerode).
 */
public class MinimizadorAFD {

    /**
     * Minimiza o autômato original, removendo estados inalcançáveis e unindo estados indistinguíveis.
     * @param original AFD original a ser minimizado
     * @return Novo AFD equivalente com o número mínimo possível de estados
     * @throws AutomatoInvalidoException Se houver erro na construção do autômato mínimo
     */
    public static Automato minimizar(Automato original) throws AutomatoInvalidoException {
        if (original.getEstadoInicial() == null) {
            throw new AutomatoInvalidoException("Não é possível minimizar autômato sem estado inicial.");
        }

        // Passo 1: Eliminar estados inalcançáveis a partir do estado inicial
        Set<Estado> alcancaveis = obterAlcancaveis(original);
        Set<String> alfabeto = original.getAlfabeto();

        // Se não houver estados finais alcançáveis, o autômato mínimo tem apenas 1 estado não-final
        Set<Estado> finaisAlcancaveis = alcancaveis.stream()
                .filter(e -> original.getEstadosFinais().contains(e))
                .collect(Collectors.toSet());

        Set<Estado> naoFinaisAlcancaveis = new HashSet<>(alcancaveis);
        naoFinaisAlcancaveis.removeAll(finaisAlcancaveis);

        // Passo 2: Partição inicial P0 = { Finais, Não-Finais }
        List<Set<Estado>> particoes = new ArrayList<>();
        if (!finaisAlcancaveis.isEmpty()) {
            particoes.add(finaisAlcancaveis);
        }
        if (!naoFinaisAlcancaveis.isEmpty()) {
            particoes.add(naoFinaisAlcancaveis);
        }

        // Passo 3: Refinamento iterativo de partições
        boolean refinou = true;
        while (refinou) {
            refinou = false;
            List<Set<Estado>> novaParticao = new ArrayList<>();

            for (Set<Estado> grupo : particoes) {
                if (grupo.size() <= 1) {
                    novaParticao.add(grupo);
                    continue;
                }

                // Subdivide o grupo baseado no comportamento para cada símbolo
                Map<String, Set<Estado>> subGrupos = new HashMap<>();
                for (Estado estado : grupo) {
                    // Chave de assinatura: índice dos grupos de destino para cada símbolo do alfabeto
                    StringBuilder assinatura = new StringBuilder();
                    for (String simbolo : alfabeto) {
                        Estado destino = estado.getEstado(simbolo);
                        int grupoDestinoIdx = encontrarIndiceGrupo(particoes, destino);
                        assinatura.append(simbolo).append(":").append(grupoDestinoIdx).append(";");
                    }
                    subGrupos.computeIfAbsent(assinatura.toString(), k -> new HashSet<>()).add(estado);
                }

                if (subGrupos.size() > 1) {
                    refinou = true;
                }
                novaParticao.addAll(subGrupos.values());
            }

            particoes = novaParticao;
        }

        // Passo 4: Construir o novo autômato minimizado
        Automato minimo = new Automato();
        for (String s : alfabeto) {
            minimo.adicionarSimboloAlfabeto(s);
        }

        // Mapear cada grupo para um novo nome de estado
        Map<Set<Estado>, String> nomesNovosEstados = new LinkedHashMap<>();
        int contador = 0;
        String nomeInicial = null;
        Set<String> nomesFinais = new LinkedHashSet<>();

        for (Set<Estado> grupo : particoes) {
            // Nome do estado composto ordenado: e.g. "q0_q1" ou simplificado "P0", "P1"
            String nomeGrupo = grupo.stream()
                    .map(Estado::getNome)
                    .sorted()
                    .collect(Collectors.joining("_"));
            nomesNovosEstados.put(grupo, nomeGrupo);
            minimo.adicionarEstado(nomeGrupo);

            // Verifica se contém o estado inicial original
            if (grupo.contains(original.getEstadoInicial())) {
                nomeInicial = nomeGrupo;
            }

            // Verifica se contém algum estado final
            for (Estado e : grupo) {
                if (original.getEstadosFinais().contains(e)) {
                    nomesFinais.add(nomeGrupo);
                    break;
                }
            }
            contador++;
        }

        if (nomeInicial != null) {
            minimo.setEstadoInicial(nomeInicial);
        }

        for (String f : nomesFinais) {
            minimo.adicionarEstadoFinal(f);
        }

        // Adiciona as transições entre os grupos minimizados
        for (Map.Entry<Set<Estado>, String> entry : nomesNovosEstados.entrySet()) {
            Set<Estado> grupoOrigem = entry.getKey();
            String nomeOrigem = entry.getValue();
            Estado representante = grupoOrigem.iterator().next();

            for (String simbolo : alfabeto) {
                Estado destinoOriginal = representante.getEstado(simbolo);
                if (destinoOriginal != null) {
                    // Encontra o grupo do destino
                    Set<Estado> grupoDestino = particoes.stream()
                            .filter(g -> g.contains(destinoOriginal))
                            .findFirst()
                            .orElse(null);

                    if (grupoDestino != null) {
                        String nomeDestino = nomesNovosEstados.get(grupoDestino);
                        minimo.adicionarTransicao(nomeOrigem, simbolo, nomeDestino);
                    }
                }
            }
        }

        return minimo;
    }

    private static int encontrarIndiceGrupo(List<Set<Estado>> particoes, Estado destino) {
        if (destino == null) return -1; // Estado poço / indefinido
        for (int i = 0; i < particoes.size(); i++) {
            if (particoes.get(i).contains(destino)) {
                return i;
            }
        }
        return -1;
    }

    private static Set<Estado> obterAlcancaveis(Automato automato) {
        Set<Estado> alcancaveis = new HashSet<>();
        Queue<Estado> fila = new ArrayDeque<>();

        if (automato.getEstadoInicial() != null) {
            alcancaveis.add(automato.getEstadoInicial());
            fila.add(automato.getEstadoInicial());
        }

        while (!fila.isEmpty()) {
            Estado atual = fila.poll();
            for (Estado dest : atual.getTransicoes().values()) {
                if (alcancaveis.add(dest)) {
                    fila.add(dest);
                }
            }
        }

        return alcancaveis;
    }
}
