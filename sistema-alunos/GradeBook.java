import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

// CLASSE PRINCIPAL COM INTERFACE GRÁFICA (Nome do arquivo: GradeBook.java)
public class GradeBook {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String nomeCurso = "CS101 Introduction to Java Programming";
            
            ArrayList<int[]> notasIniciais = new ArrayList<>();
            notasIniciais.add(new int[]{87, 96, 70});
            notasIniciais.add(new int[]{68, 45, 55}); 
            notasIniciais.add(new int[]{94, 100, 90});

            new GradeBookTela(nomeCurso, notasIniciais);
        });
    }

    // CLASSE DA INTERFACE GRÁFICA (Aninhada como static)
    public static class GradeBookTela extends JFrame {
        private String courseName;
        private ArrayList<int[]> gradesList; 
        private int totalTestes = 3;         

        // Componentes da Tela que precisam ser atualizados
        private DefaultTableModel modeloTabela;
        private JTable tabelaNotas;
        private JTextArea txtAreaEstatisticas;

        public GradeBookTela(String courseName, ArrayList<int[]> gradesList) {
            this.courseName = courseName;
            this.gradesList = gradesList;

            // Configurações da Janela
            setTitle("Livro de Notas Dinâmico - " + this.courseName);
            setSize(650, 600);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            setLayout(new BorderLayout(10, 10));

            // 1. PAINEL SUPERIOR: Título do Curso e Botões (Adicionar e Remover)
            JPanel painelSuperior = new JPanel(new BorderLayout(10, 10));
            painelSuperior.setBackground(new Color(240, 240, 245));
            painelSuperior.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            
            JLabel lblTitulo = new JLabel("Curso: " + this.courseName);
            lblTitulo.setFont(new Font("Arial", Font.BOLD, 14));
            
            // Sub-painel para agrupar os botões no canto direito
            JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            painelBotoes.setOpaque(false);
            
            JButton btnAdicionar = new JButton("Adicionar Aluno");
            btnAdicionar.setBackground(new Color(220, 245, 220));
            
            JButton btnRemover = new JButton("Remover Selecionado");
            btnRemover.setBackground(new Color(255, 220, 220));
            
            painelBotoes.add(btnAdicionar);
            painelBotoes.add(btnRemover);
            
            painelSuperior.add(lblTitulo, BorderLayout.WEST);
            painelSuperior.add(painelBotoes, BorderLayout.EAST);
            add(painelSuperior, BorderLayout.NORTH);

            // 2. PAINEL CENTRAL: Tabela com as Notas e Médias
            String[] colunas = {"Aluno", "Teste 1", "Teste 2", "Teste 3", "Média"};
            modeloTabela = new DefaultTableModel(colunas, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; 
                }
            };

            tabelaNotas = new JTable(modeloTabela);
            tabelaNotas.setFont(new Font("Arial", Font.PLAIN, 13));
            tabelaNotas.setRowHeight(24);
            // Permite selecionar apenas uma linha por vez
            tabelaNotas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            // Customização para destacar médias menores que 60 em vermelho
            tabelaNotas.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, 
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    
                    if (value != null) {
                        try {
                            double media = Double.parseDouble(value.toString().replace(",", "."));
                            if (media < 60.0) {
                                c.setForeground(Color.RED);
                                c.setFont(new Font("Arial", Font.BOLD, 13));
                            } else {
                                c.setForeground(Color.BLACK);
                                c.setFont(new Font("Arial", Font.PLAIN, 13));
                            }
                        } catch (NumberFormatException e) {
                            c.setForeground(Color.BLACK);
                        }
                    }
                    return c;
                }
            });

            JScrollPane scrollTabela = new JScrollPane(tabelaNotas);
            scrollTabela.setBorder(BorderFactory.createTitledBorder("Notas dos Alunos"));

            // 3. PAINEL INFERIOR: Estatísticas Gerais e Gráfico
            txtAreaEstatisticas = new JTextArea();
            txtAreaEstatisticas.setEditable(false);
            txtAreaEstatisticas.setFont(new Font("Monospaced", Font.PLAIN, 12));
            JScrollPane scrollEstatisticas = new JScrollPane(txtAreaEstatisticas);
            scrollEstatisticas.setBorder(BorderFactory.createTitledBorder("Estatísticas e Distribuição de Notas"));

            // Carrega os dados iniciais
            atualizarTabelaEDados();

            JSplitPane divisor = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollTabela, scrollEstatisticas);
            divisor.setDividerLocation(200);
            add(divisor, BorderLayout.CENTER);

            // --- EVENTO DO BOTÃO ADICIONAR ALUNO ---
            btnAdicionar.addActionListener(e -> {
                int[] novasNotas = new int[totalTestes];
                try {
                    for (int i = 0; i < totalTestes; i++) {
                        String inputNota = JOptionPane.showInputDialog(this, 
                                "Digite a nota do Teste " + (i + 1) + " (0 a 100):", 
                                "Adicionar Nota", JOptionPane.QUESTION_MESSAGE);
                        
                        if (inputNota == null) return; 
                        
                        int nota = Integer.parseInt(inputNota);
                        if (nota < 0 || nota > 100) {
                            JOptionPane.showMessageDialog(this, "A nota deve ser entre 0 e 100.", "Erro", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        novasNotas[i] = nota;
                    }
                    
                    gradesList.add(novasNotas);
                    atualizarTabelaEDados();
                    
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Por favor, insira apenas números inteiros válidos.", "Erro de Entrada", JOptionPane.ERROR_MESSAGE);
                }
            });

            // --- EVENTO DO BOTÃO REMOVER ALUNO ---
            btnRemover.addActionListener(e -> {
                // Pega o índice da linha selecionada pelo clique do usuário (-1 se nenhuma estiver selecionada)
                int linhaSelecionada = tabelaNotas.getSelectedRow();
                
                if (linhaSelecionada == -1) {
                    JOptionPane.showMessageDialog(this, "Por favor, selecione um aluno na tabela para remover.", "Nenhum Aluno Selecionado", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // Pede uma confirmação visual antes de deletar de vez
                int confirmacao = JOptionPane.showConfirmDialog(this, 
                        "Tem certeza de que deseja remover o Aluno " + (linhaSelecionada + 1) + "?", 
                        "Confirmar Remoção", JOptionPane.YES_NO_OPTION);
                
                if (confirmacao == JOptionPane.YES_OPTION) {
                    // Remove do ArrayList dinâmico
                    gradesList.remove(linhaSelecionada);
                    // Reconstrói a tabela e recalcula médias/gráficos automaticamente
                    atualizarTabelaEDados();
                }
            });

            setVisible(true);
        }

        private void atualizarTabelaEDados() {
            modeloTabela.setRowCount(0);

            // Se a lista ficar vazia após exclusões, limpa as estatísticas e para o processo
            if (gradesList.isEmpty()) {
                txtAreaEstatisticas.setText("Nenhum aluno cadastrado no sistema.");
                return;
            }

            for (int aluno = 0; aluno < gradesList.size(); aluno++) {
                int[] notasAluno = gradesList.get(aluno);
                Object[] linha = new Object[5];
                linha[0] = "Aluno " + (aluno + 1);
                linha[1] = notasAluno[0];
                linha[2] = notasAluno[1];
                linha[3] = notasAluno[2];
                linha[4] = String.format("%.2f", getAverage(notasAluno));
                modeloTabela.addRow(linha);
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Menor Nota no Livro Geral: %d%n", getMinimum()));
            sb.append(String.format("Maior Nota no Livro Geral: %d%n%n", getMaximum()));
            sb.append(gerarBarChartTexto());
            txtAreaEstatisticas.setText(sb.toString());
        }

        public int getMinimum() {
            int lowGrade = gradesList.get(0)[0];
            for (int[] studentGrades : gradesList) {
                for (int grade : studentGrades) {
                    if (grade < lowGrade) lowGrade = grade;
                }
            }
            return lowGrade;
        }

        public int getMaximum() {
            int highGrade = gradesList.get(0)[0];
            for (int[] studentGrades : gradesList) {
                for (int grade : studentGrades) {
                    if (grade > highGrade) highGrade = grade;
                }
            }
            return highGrade;
        }

        public double getAverage(int[] setOfGrades) {
            int total = 0;
            for (int grade: setOfGrades) total += grade;
            return (double) total / setOfGrades.length;
        }

        private String gerarBarChartTexto() {
            StringBuilder chart = new StringBuilder();
            chart.append("Distribuição Geral de Notas:\n");

            int[] frequency = new int[11];
            for (int[] studentGrades : gradesList) {
                for (int grade : studentGrades) {
                    ++frequency[grade/10];
                }
            }
            for (int count =0; count < frequency.length; count++) {
                if (count == 10) {
                    chart.append(String.format("%5d:",100));
                } else {
                    chart.append(String.format("%02d-%02d:", count * 10, count * 10 + 9));
                }
                for (int stars = 0; stars < frequency[count]; stars++){
                    chart.append("*");
                }
                chart.append("\n");
            }
            return chart.toString();
        }
    }
}