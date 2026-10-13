package org.example;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class GerenciadorIO {

    public static Automato montarPorArquivo(File arquivo) throws FileNotFoundException, AutomatoInvalidoException {
        Scanner scannerArquivo = new Scanner(arquivo);
        Automato automato = new Automato();

        try {
            // 1. Alfabeto
            if (scannerArquivo.hasNextLine()) {
                String linha = scannerArquivo.nextLine().trim();
                if (!linha.isEmpty()) {
                    for (String simbolo : linha.split("\\s+")) {
                        automato.adicionarSimboloAlfabeto(simbolo);
                    }
                }
            }

            // 2. Estados
            if (scannerArquivo.hasNextLine()) {
                String linha = scannerArquivo.nextLine().trim();
                if (!linha.isEmpty()) {
                    for (String estado : linha.split("\\s+")) {
                        automato.adicionarEstado(estado);
                    }
                }
            }

            // 3. Estado Inicial
            if (scannerArquivo.hasNextLine()) {
                automato.setEstadoInicial(scannerArquivo.nextLine().trim());
            }

            // 4. Estados Finais
            if (scannerArquivo.hasNextLine()) {
                String linha = scannerArquivo.nextLine().trim();
                if (!linha.isEmpty()) {
                    for (String estadoFinal : linha.split("\\s+")) {
                        automato.adicionarEstadoFinal(estadoFinal);
                    }
                }
            }

            // 5. Transições
            while (scannerArquivo.hasNextLine()) {
                String linha = scannerArquivo.nextLine().trim();
                if (linha.isEmpty()) continue;

                String[] partes = linha.split("\\s+");
                if (partes.length == 3) {
                    automato.adicionarTransicao(partes[0], partes[1], partes[2]);
                } else {
                    throw new AutomatoInvalidoException("Transição em formato inválido no arquivo: '" + linha + "'");
                }
            }
        } finally {
            scannerArquivo.close();
        }

        return automato;
    }
}