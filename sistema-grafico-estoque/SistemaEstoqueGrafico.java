import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SistemaEstoqueGrafico extends JFrame {
    
    // Componentes da interface
    private JTextField txtNome, txtPreco;
    private JTable tabelaProdutos;
    private DefaultTableModel modeloTabela;
    private JButton btnAdicionar, btnRemover;
    
    // Controle de dados interna
    private int contadorId = 1;

    public SistemaEstoqueGrafico() {
        // Configurações básicas da janela principal
        setTitle("Sistema de Controle de Estoque");
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centraliza a janela na tela
        setLayout(new BorderLayout(10, 10));

        // 1. PAINEL DE ENTRADA (Topo)
        JPanel painelEntrada = new JPanel(new GridLayout(3, 2, 5, 5));
        painelEntrada.setBorder(BorderFactory.createTitledBorder("Cadastro de Produto"));

        painelEntrada.add(new JLabel(" Nome do Produto:"));
        txtNome = new JTextField();
        painelEntrada.add(txtNome);

        painelEntrada.add(new JLabel(" Preço (R$):"));
        txtPreco = new JTextField();
        painelEntrada.add(txtPreco);

        btnAdicionar = new JButton("Adicionar Produto");
        painelEntrada.add(btnAdicionar);
        
        add(painelEntrada, BorderLayout.NORTH);

        // 2. PAINEL DE SAÍDA / LISTAGEM (Centro)
        // Definição das colunas da tabela
        String[] colunas = {"ID", "Nome do Produto", "Preço"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Impede o usuário de editar o texto direto na tabela
            }
        };
        
        tabelaProdutos = new JTable(modeloTabela);
        JScrollPane scrollTabela = new JScrollPane(tabelaProdutos);
        scrollTabela.setBorder(BorderFactory.createTitledBorder("Produtos em Estoque"));
        
        add(scrollTabela, BorderLayout.CENTER);

        // 3. PAINEL DE AÇÕES / REMOÇÃO (Baixo)
        JPanel painelAcoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRemover = new JButton("Remover Selecionado");
        btnRemover.setBackground(new Color(220, 53, 69)); // Cor vermelha para alerta
        btnRemover.setForeground(Color.WHITE);
        painelAcoes.add(btnRemover);
        
        add(painelAcoes, BorderLayout.SOUTH);

        // --- ASSOCIAÇÃO DOS EVENTOS (BOTÕES) ---
        configurarEventos();
    }

    private void configurarEventos() {
        // Ação do Botão Adicionar (Entrada + Validação + Adicionar + Listar)
        btnAdicionar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nome = txtNome.getText().trim();
                String precoStr = txtPreco.getText().trim().replace(",", "."); // Aceita ponto ou vírgula

                // Validação: Campo vazio
                if (nome.isEmpty() || precoStr.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Por favor, preencha todos os campos!", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Validação: Formato numérico do preço
                double preco;
                try {
                    preco = Double.parseDouble(precoStr);
                    if (preco <= 0) {
                        JOptionPane.showMessageDialog(null, "O preço deve ser maior que zero!", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Preço inválido! Digite apenas números decimais.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Saída de dados: Atualiza a interface gráfica adicionando uma nova linha
                String precoFormatado = String.format("R$ %.2f", preco);
                modeloTabela.addRow(new Object[]{contadorId++, nome, precoFormatado});

                // Limpa os campos para a próxima entrada
                txtNome.setText("");
                txtPreco.setText("");
                txtNome.requestFocus();
            }
        });

        // Ação do Botão Remover (Remover + Validação)
        btnRemover.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int linhaSelecionada = tabelaProdutos.getSelectedRow();

                // Validação: Verifica se o usuário selecionou uma linha da lista
                if (linhaSelecionada == -1) {
                    JOptionPane.showMessageDialog(null, "Selecione um produto na tabela para remover!", "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Confirmação de exclusão
                int confirmacao = JOptionPane.showConfirmDialog(null, "Tem certeza que deseja remover este produto?", "Confirmar Exclusão", JOptionPane.YES_NO_OPTION);
                
                if (confirmacao == JOptionPane.YES_OPTION) {
                    modeloTabela.removeRow(linhaSelecionada);
                    JOptionPane.showMessageDialog(null, "Produto removido com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
    }

    // Método principal para rodar a aplicação gráfica
    public static void main(String[] args) {
        // Define o visual do sistema operacional nativo (Look and Feel)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Executa a janela em uma thread segura de eventos
        SwingUtilities.invokeLater(() -> {
            new SistemaEstoqueGrafico().setVisible(true);
        });
    }
}
