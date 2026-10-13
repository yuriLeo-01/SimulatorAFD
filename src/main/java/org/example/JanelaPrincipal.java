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
        painelTransicoes.setFont(new Font("Monospaced", Font.PLAIN, 13));
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

        } catch (AutomatoInvalidoException ex) {
            lblResultado.setText("Erro: Autômato Inválido!");
            lblResultado.setForeground(Color.RED);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro de Validação (AFD)", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void carregarDeArquivo() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File arquivo = fileChooser.getSelectedFile();
            try (Scanner scanner = new Scanner(arquivo)) {
                if (scanner.hasNextLine()) txtAlfabeto.setText(scanner.nextLine().trim());
                if (scanner.hasNextLine()) txtEstados.setText(scanner.nextLine().trim());
                if (scanner.hasNextLine()) txtEstadoInicial.setText(scanner.nextLine().trim());
                if (scanner.hasNextLine()) txtEstadosFinais.setText(scanner.nextLine().trim());

                StringBuilder transicoesBuilder = new StringBuilder();
                while (scanner.hasNextLine()) {
                    String linha = scanner.nextLine().trim();
                    if (!linha.isEmpty()) {
                        transicoesBuilder.append(linha).append("\n");
                    }
                }
                txtTransicoes.setText(transicoesBuilder.toString().trim());

                construirAutomatoManualmente();

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
            boolean aceita = simulador.simular(cadeia);
            if (aceita) {
                lblResultado.setText("Cadeia '" + cadeia + "' -> ACEITA!");
                lblResultado.setForeground(new Color(0, 150, 0));
            } else {
                lblResultado.setText("Cadeia '" + cadeia + "' -> REJEITADA!");
                lblResultado.setForeground(Color.RED);
            }
        } catch (AutomatoInvalidoException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro de Simulação", JOptionPane.ERROR_MESSAGE);
        }
    }
}