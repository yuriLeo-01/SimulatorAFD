package org.example;
import org.example.Automato;
import org.example.AutomatoInvalidoException;
import org.example.Simulador;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Scanner;

public class JanelaPrincipal extends JFrame {
    private JTextField txtAlfabeto;
    private JTextField txtEstados;
    private JTextField txtEstadoInicial;
    private JTextField txtEstadosFinais;
    private JTextArea txtTransicoes;

    private JTextField txtCadeiaEntrada;
    private JLabel lblResultado;

    private Automato automatoAtual;
    private Simulador simulador;

    public JanelaPrincipal() {
        setTitle("Simulador de Autômatos Finitos Determinísticos (AFD)");
        setSize(720, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // PAINEL DE CONFIGURAÇÃO DO AUTÔMATO
        JPanel painelForm = new JPanel(new GridLayout(6, 2, 5, 5));
        painelForm.setBorder(BorderFactory.createTitledBorder("Definição do Autômato"));

        painelForm.add(new JLabel(" Alfabeto (ex: 0 1):"));
        txtAlfabeto = new JTextField("0 1");
        painelForm.add(txtAlfabeto);

        painelForm.add(new JLabel(" Estados (ex: q0 q1 q2):"));
        txtEstados = new JTextField("q0 q1 q2");
        painelForm.add(txtEstados);

        painelForm.add(new JLabel(" Estado Inicial (ex: q0):"));
        txtEstadoInicial = new JTextField("q0");
        painelForm.add(txtEstadoInicial);

        painelForm.add(new JLabel(" Estados de Aceitação (ex: q2):"));
        txtEstadosFinais = new JTextField("q2");
        painelForm.add(txtEstadosFinais);

        JButton btnCarregarArquivo = new JButton("Carregar de Arquivo (.txt)");
        btnCarregarArquivo.addActionListener(e -> carregarDeArquivo());
        painelForm.add(btnCarregarArquivo);

        JButton btnConstruir = new JButton("Construir / Atualizar Autômato");
        btnConstruir.setBackground(new Color(220, 240, 255));
        btnConstruir.addActionListener(e -> construirAutomatoManualmente());
        painelForm.add(btnConstruir);

        add(painelForm, BorderLayout.NORTH);

        // ÁREA DAS TRANSIÇÕES
        JPanel painelTransicoes = new JPanel(new BorderLayout());
        painelTransicoes.setBorder(BorderFactory.createTitledBorder("Transições (Formato: Origem Simbolo Destino - uma por linha)"));
        txtTransicoes = new JTextArea("q0 0 q1\nq0 1 q0\nq1 0 q1\nq1 1 q2\nq2 0 q1\nq2 1 q0");
        txtTransicoes.setFont(new Font("Monospaced", Font.PLAIN, 13));
        painelTransicoes.add(new JScrollPane(txtTransicoes), BorderLayout.CENTER);
        add(painelTransicoes, BorderLayout.CENTER);

        // PAINEL DE TESTE DE CADEIAS
        JPanel painelSimulacao = new JPanel(new GridLayout(3, 1, 5, 5));
        painelSimulacao.setBorder(BorderFactory.createTitledBorder("Simulação de Cadeias"));

        JPanel painelEntrada = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelEntrada.add(new JLabel("Cadeia de Entrada: "));
        txtCadeiaEntrada = new JTextField(20);
        painelEntrada.add(txtCadeiaEntrada);

        JButton btnTestar = new JButton("Testar Cadeia");
        btnTestar.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnTestar.addActionListener(e -> testarCadeia());
        painelEntrada.add(btnTestar);

        painelSimulacao.add(painelEntrada);

        lblResultado = new JLabel("Status: Aguardando construção do autômato...", SwingConstants.CENTER);
        lblResultado.setFont(new Font("SansSerif", Font.BOLD, 16));
        painelSimulacao.add(lblResultado);

        add(painelSimulacao, BorderLayout.SOUTH);
    }

    private void construirAutomatoManualmente() {
        // Descarta o autômato anterior: se a validação falhar, não pode sobrar um autômato antigo ativo
        this.automatoAtual = null;
        this.simulador = null;
        try {
            Automato automato = new Automato();

            for (String s : txtAlfabeto.getText().trim().split("\\s+")) {
                if (!s.isEmpty()) automato.adicionarSimboloAlfabeto(s);
            }

            for (String e : txtEstados.getText().trim().split("\\s+")) {
                if (!e.isEmpty()) automato.adicionarEstado(e);
            }

            automato.setEstadoInicial(txtEstadoInicial.getText().trim());

            for (String f : txtEstadosFinais.getText().trim().split("\\s+")) {
                if (!f.isEmpty()) automato.adicionarEstadoFinal(f);
            }

            String[] linhas = txtTransicoes.getText().split("\n");
            for (String linha : linhas) {
                linha = linha.trim();
                if (linha.isEmpty()) continue;
                String[] partes = linha.split("\\s+");
                if (partes.length == 3) {
                    automato.adicionarTransicao(partes[0], partes[1], partes[2]);
                } else {
                    throw new AutomatoInvalidoException("Formato inválido na linha de transição: '" + linha + "'");
                }
            }

            this.automatoAtual = automato;
            this.simulador = new Simulador(automatoAtual);

            lblResultado.setText("Autômato Construído com Sucesso! Pronto para simular.");
            lblResultado.setForeground(new Color(0, 128, 0));
            JOptionPane.showMessageDialog(this, "Autômato AFD válido construído com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            avisarSeIncompleto(automato);

        } catch (AutomatoInvalidoException ex) {
            lblResultado.setText("Erro: Autômato Inválido!");
            lblResultado.setForeground(Color.RED);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro de Validação (AFD)", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void avisarSeIncompleto(Automato automato) {
        java.util.List<String> faltantes = automato.getTransicoesFaltantes();
        if (faltantes.isEmpty()) return;
        lblResultado.setText("Autômato válido, porém INCOMPLETO (faltam " + faltantes.size() + " transição(ões)).");
        lblResultado.setForeground(new Color(200, 120, 0));
        JOptionPane.showMessageDialog(this,
                "O autômato é determinístico, mas não é completo.\n"
                        + "Transições não definidas: " + String.join(", ", faltantes) + "\n\n"
                        + "Ao chegar em um desses pontos, a simulação para em um estado poço implícito\n"
                        + "e a cadeia é REJEITADA.",
                "Aviso: AFD incompleto", JOptionPane.WARNING_MESSAGE);
    }

    private void carregarDeArquivo() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File arquivo = fileChooser.getSelectedFile();
            this.automatoAtual = null;
            this.simulador = null;
            try {
                Automato automato = GerenciadorIO.montarPorArquivo(arquivo);
                this.automatoAtual = automato;
                this.simulador = new Simulador(automatoAtual);

                txtAlfabeto.setText(String.join(" ", automato.getAlfabeto()));
                txtEstados.setText(String.join(" ", automato.getEstados().keySet()));
                txtEstadoInicial.setText(automato.getEstadoInicial() != null ? automato.getEstadoInicial().getNome() : "");
                txtEstadosFinais.setText(automato.getEstadosFinais().stream().map(Estado::getNome).reduce((a, b) -> a + " " + b).orElse(""));

                StringBuilder sb = new StringBuilder();
                for (Transicao t : automato.getTodasTransicoes()) {
                    sb.append(t.toLinhaArquivo()).append("\n");
                }
                txtTransicoes.setText(sb.toString().trim());

                lblResultado.setText("Autômato carregado do arquivo com sucesso! (" + automato.getEstados().size() + " estados)");
                lblResultado.setForeground(new Color(0, 128, 0));
                JOptionPane.showMessageDialog(this, "Autômato AFD carregado com sucesso via GerenciadorIO!", "Arquivo Carregado", JOptionPane.INFORMATION_MESSAGE);
                avisarSeIncompleto(automato);

            } catch (Exception ex) {
                lblResultado.setText("Erro ao carregar arquivo!");
                lblResultado.setForeground(Color.RED);
                JOptionPane.showMessageDialog(this, "Erro ao carregar arquivo: " + ex.getMessage(), "Erro no Arquivo", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void testarCadeia() {
        if (simulador == null) {
            JOptionPane.showMessageDialog(this, "Por favor, construa ou carregue um autômato primeiro!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String cadeia = txtCadeiaEntrada.getText().trim();
        try {
            ResultadoSimulacao res = simulador.executar(cadeia);
            if (res.isAceita()) {
                lblResultado.setText("<html><center>Cadeia '" + cadeia + "' &rarr; <font color='green'><b>ACEITA!</b></font><br><span style='font-size:11px; color:#555;'>Caminho: " + res.getCaminhoFormatado() + " (" + res.getMensagemDetalhada() + ")</span></center></html>");
            } else {
                lblResultado.setText("<html><center>Cadeia '" + cadeia + "' &rarr; <font color='red'><b>REJEITADA!</b></font><br><span style='font-size:11px; color:#555;'>Caminho: " + res.getCaminhoFormatado() + " (" + res.getMensagemDetalhada() + ")</span></center></html>");
            }
        } catch (AutomatoInvalidoException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro de Simulação", JOptionPane.ERROR_MESSAGE);
        }
    }
}