package org.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Responsável pelo carregamento, parsing robusto e exportação de arquivos de autômatos.
 */
public class GerenciadorIO {

    /**
     * Monta um autômato a partir de um arquivo .txt.
     */
    public static Automato montarPorArquivo(File arquivo) throws FileNotFoundException, AutomatoInvalidoException {
        if (!arquivo.exists()) {
            throw new FileNotFoundException("Arquivo não encontrado: " + arquivo.getAbsolutePath());
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {
            return parseLinhas(reader);
        } catch (IOException e) {
            throw new AutomatoInvalidoException("Erro ao ler arquivo: " + e.getMessage(), e);
        }
    }

    /**
     * Monta um autômato diretamente a partir de uma String multilinha.
     */
    public static Automato montarPorTexto(String conteudoTexto) throws AutomatoInvalidoException {
        try (BufferedReader reader = new BufferedReader(new StringReader(conteudoTexto))) {
            return parseLinhas(reader);
        } catch (IOException e) {
            throw new AutomatoInvalidoException("Erro ao processar texto do autômato: " + e.getMessage(), e);
        }
    }

    private static Automato parseLinhas(BufferedReader reader) throws IOException, AutomatoInvalidoException {
        Automato automato = new Automato();
        List<String> linhas = new ArrayList<>();
        String linhaBruta;

        while ((linhaBruta = reader.readLine()) != null) {
            String linha = linhaBruta.trim();

            // Ignora linhas vazias e comentários (# ou //)
            if (linha.isEmpty() || linha.startsWith("#") || linha.startsWith("//")) {
                continue;
            }

            // Remove comentários inline
            if (linha.contains("#")) {
                linha = linha.substring(0, linha.indexOf('#')).trim();
            } else if (linha.contains("//")) {
                linha = linha.substring(0, linha.indexOf("//")).trim();
            }

            if (!linha.isEmpty()) {
                linhas.add(linha);
            }
        }

        if (linhas.size() < 3) {
            throw new AutomatoInvalidoException(
                    "Arquivo incompleto: deve conter Alfabeto, Estados e Estado Inicial "
                            + "(seguidos dos Estados de Aceitação, que podem ser omitidos, e das transições)."
            );
        }

        // 1. Alfabeto
        for (String s : linhas.get(0).split("\\s+")) {
            if (!s.isEmpty()) automato.adicionarSimboloAlfabeto(s);
        }

        // 2. Estados
        for (String e : linhas.get(1).split("\\s+")) {
            if (!e.isEmpty()) automato.adicionarEstado(e);
        }

        // 3. Estado Inicial
        automato.setEstadoInicial(linhas.get(2).trim());

        // 4. Estados de Aceitação.
        // A linha de aceitação pode estar vazia (F = conjunto vazio). Como linhas em branco são ignoradas,
        // se a próxima linha já tem a forma "Origem Simbolo Destino" (o símbolo do meio pertence ao
        // alfabeto), ela é uma transição e F é vazio.
        int inicioTransicoes = 3;
        if (linhas.size() > 3 && !pareceTransicao(automato, linhas.get(3))) {
            for (String f : linhas.get(3).split("\\s+")) {
                if (!f.isEmpty()) automato.adicionarEstadoFinal(f);
            }
            inicioTransicoes = 4;
        }

        // 5. Transições
        for (int i = inicioTransicoes; i < linhas.size(); i++) {
            String linhaTrans = linhas.get(i);
            String[] partes = linhaTrans.split("\\s+");
            if (partes.length == 3) {
                automato.adicionarTransicao(partes[0], partes[1], partes[2]);
            } else {
                throw new AutomatoInvalidoException(
                        "Formato de transição inválido: esperava 'Origem Simbolo Destino', encontrou: '" + linhaTrans + "'",
                        i + 1
                );
            }
        }

        return automato;
    }

    private static boolean pareceTransicao(Automato automato, String linha) {
        String[] partes = linha.split("\\s+");
        return partes.length == 3 && automato.getAlfabeto().contains(partes[1]);
    }

    /**
     * Salva o autômato em formato texto padrão.
     */
    public static void salvarEmArquivo(Automato automato, File destino) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(destino))) {
            writer.write(automato.exportarFormatoArquivo());
        }
    }

    /**
     * Salva a representação DOT do autômato para renderização no Graphviz.
     */
    public static void exportarDOT(Automato automato, File destino) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(destino))) {
            writer.write(automato.exportarDOT());
        }
    }
}