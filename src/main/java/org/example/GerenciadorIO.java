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
        List<String> linhasSignificativas = new ArrayList<>();
        String linhaBruta;
        int numeroLinhaReal = 0;

        while ((linhaBruta = reader.readLine()) != null) {
            numeroLinhaReal++;
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
                linhasSignificativas.add(linha);
            }
        }

        if (linhasSignificativas.size() < 4) {
            throw new AutomatoInvalidoException(
                    "Arquivo incompleto: deve conter no mínimo 4 seções (Alfabeto, Estados, Estado Inicial e Estados Finais)."
            );
        }

        // 1. Alfabeto
        String[] simbolos = linhasSignificativas.get(0).split("\\s+");
        for (String s : simbolos) {
            if (!s.isEmpty()) automato.adicionarSimboloAlfabeto(s);
        }

        // 2. Estados
        String[] estados = linhasSignificativas.get(1).split("\\s+");
        for (String e : estados) {
            if (!e.isEmpty()) automato.adicionarEstado(e);
        }

        // 3. Estado Inicial
        String estadoInicial = linhasSignificativas.get(2).trim();
        automato.setEstadoInicial(estadoInicial);

        // 4. Estados Finais
        String[] estadosFinais = linhasSignificativas.get(3).split("\\s+");
        for (String f : estadosFinais) {
            if (!f.isEmpty()) automato.adicionarEstadoFinal(f);
        }

        // 5. Transições (linhas 4 em diante)
        for (int i = 4; i < linhasSignificativas.size(); i++) {
            String linhaTrans = linhasSignificativas.get(i);
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