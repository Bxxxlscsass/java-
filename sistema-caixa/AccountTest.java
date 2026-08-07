import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;

// CLASSE PRINCIPAL (Nome do arquivo: AccountTest.java)
public class AccountTest {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Cria as duas contas de teste
            Conta conta1 = new Conta("Jane Green", 100.00);
            Conta conta2 = new Conta("John Blue", 50.00);
            
            new CaixaEletronicoTela(conta1, conta2);
        });
    }

    // CLASSE DA TELA COM MENU SUSPENSO DE CLIENTES
    public static class CaixaEletronicoTela extends JFrame {
        private Conta conta1;
        private Conta conta2;
        private Conta contaAtiva; // Controla quem está operando o caixa agora

        // Componentes da interface
        private JComboBox<String> comboClientes;
        private JLabel lblSaldo;
        private JTextField txtValor;
        private JTextArea txtAreaExtrato;
        private JButton btnTransferir;

        public CaixaEletronicoTela(Conta c1, Conta c2) {
            this.conta1 = c1;
            this.conta2 = c2;
            this.contaAtiva = c1; // Começa com a Jane Green por padrão

            // Configurações da Janela
            setTitle("Caixa Eletrônico Multi-Contas");
            setSize(480, 530);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            setLayout(new BorderLayout(10, 10));

            // 1. PAINEL SUPERIOR: Seleção de Cliente e Exibição de Saldo
            JPanel painelSuperior = new JPanel(new GridLayout(3, 1, 5, 5));
            painelSuperior.setBackground(new Color(235, 245, 255));
            painelSuperior.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            JLabel lblSelecao = new JLabel("Selecione o Cliente Ativo:", SwingConstants.CENTER);
            lblSelecao.setFont(new Font("Arial", Font.BOLD, 12));
            
            // JComboBox para alternar os clientes
            String[] nomesClientes = { conta1.getNome(), conta2.getNome() };
            comboClientes = new JComboBox<>(nomesClientes);
            comboClientes.setFont(new Font("Arial", Font.PLAIN, 14));
            
            lblSaldo = new JLabel("Saldo Atual: R$ " + String.format("%.2f", contaAtiva.getSaldo()), SwingConstants.CENTER);
            lblSaldo.setFont(new Font("Arial", Font.BOLD, 22));
            lblSaldo.setForeground(new Color(0, 102, 204));
            
            painelSuperior.add(lblSelecao);
            painelSuperior.add(comboClientes);
            painelSuperior.add(lblSaldo);
            add(painelSuperior, BorderLayout.NORTH);

            // 2. PAINEL CENTRAL: Inputs e Botões
            JPanel painelCentral = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 6, 6, 6);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            JLabel lblInstrucao = new JLabel("Digite o valor da operação (R$):");
            gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
            painelCentral.add(lblInstrucao, gbc);

            txtValor = new JTextField(15);
            txtValor.setFont(new Font("Arial", Font.PLAIN, 14));
            gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
            painelCentral.add(txtValor, gbc);

            JButton btnDepositar = new JButton("Depositar");
            JButton btnSacar = new JButton("Sacar");
            btnTransferir = new JButton("Transferir p/ " + conta2.getNome());
            JButton btnExtrato = new JButton("Ver Extrato");

            gbc.gridwidth = 1;
            gbc.gridx = 0; gbc.gridy = 2; painelCentral.add(btnDepositar, gbc);
            gbc.gridx = 1; gbc.gridy = 2; painelCentral.add(btnSacar, gbc);
            gbc.gridx = 0; gbc.gridy = 3; painelCentral.add(btnTransferir, gbc);
            gbc.gridx = 1; gbc.gridy = 3; painelCentral.add(btnExtrato, gbc);

            add(painelCentral, BorderLayout.CENTER);

            // 3. PAINEL INFERIOR: Histórico/Extrato
            txtAreaExtrato = new JTextArea(10, 30);
            txtAreaExtrato.setEditable(false);
            txtAreaExtrato.setFont(new Font("Monospaced", Font.PLAIN, 12));
            JScrollPane scrollPane = new JScrollPane(txtAreaExtrato);
            scrollPane.setBorder(BorderFactory.createTitledBorder("Painel do Extrato / Mensagens"));
            add(scrollPane, BorderLayout.SOUTH);

            // --- EVENTOS E AÇÕES ---

            // AÇÃO DO COMBOBOX: Detecta a mudança de cliente na caixinha
            comboClientes.addActionListener(e -> {
                int indexSelecionado = comboClientes.getSelectedIndex();
                if (indexSelecionado == 0) {
                    contaAtiva = conta1;
                    btnTransferir.setText("Transferir p/ " + conta2.getNome());
                } else {
                    contaAtiva = conta2;
                    btnTransferir.setText("Transferir p/ " + conta1.getNome());
                }
                atualizarInterface("Mudança de cliente: " + contaAtiva.getNome() + " ativo.");
            });

            // Ação: Depositar
            btnDepositar.addActionListener(e -> {
                double valor = lerValorInput();
                if (valor > 0) {
                    contaAtiva.depositar(valor);
                    atualizarInterface("Depósito de R$" + String.format("%.2f", valor) + " realizado com sucesso.");
                }
            });

            // Ação: Sacar
            btnSacar.addActionListener(e -> {
                double valor = lerValorInput();
                if (valor > 0) {
                    if (contaAtiva.sacar(valor)) {
                        atualizarInterface("Saque de R$" + String.format("%.2f", valor) + " realizado.");
                    } else {
                        JOptionPane.showMessageDialog(this, "Erro: Saldo insuficiente ou valor inválido.", "Erro no Saque", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            // Ação: Transferir
            btnTransferir.addActionListener(e -> {
                double valor = lerValorInput();
                if (valor > 0) {
                    // Define quem é o destino dinamicamente baseado em quem está ativo
                    Conta destino = (contaAtiva == conta1) ? conta2 : conta1;
                    
                    if (contaAtiva.transferir(destino, valor)) {
                        atualizarInterface("Transferência de R$" + String.format("%.2f", valor) + " para " + destino.getNome() + " realizada.");
                    } else {
                        JOptionPane.showMessageDialog(this, "Erro: Saldo insuficiente ou valor inválido.", "Erro na Transferência", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            // Ação: Ver Extrato
            btnExtrato.addActionListener(e -> {
                txtAreaExtrato.setText("");
                txtAreaExtrato.append("--- EXTRATO EMITIDO PARA " + contaAtiva.getNome().toUpperCase() + " ---\n");
                for (String transacao : contaAtiva.getExtratoList()) {
                    txtAreaExtrato.append("[HISTÓRICO] " + transacao + "\n");
                }
                txtAreaExtrato.append("\nSaldo Final Disponível: R$ " + String.format("%.2f", contaAtiva.getSaldo()));
            });

            setVisible(true);
        }

        private double lerValorInput() {
            try {
                double valor = Double.parseDouble(txtValor.getText().replace(",", "."));
                if (valor <= 0) {
                    JOptionPane.showMessageDialog(this, "Digite um valor maior que zero.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    return -1;
                }
                return valor;
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Por favor, insira um número válido.", "Erro de Digitação", JOptionPane.ERROR_MESSAGE);
                return -1;
            }
        }

        // Atualiza o rótulo de saldo e limpa o input de texto
        private void atualizarInterface(String mensagemCentral) {
            lblSaldo.setText("Saldo Atual: R$ " + String.format("%.2f", contaAtiva.getSaldo()));
            txtAreaExtrato.setText(mensagemCentral);
            txtValor.setText("");
        }
    }

    // CLASSE DA CONTA (Model)
    public static class Conta {
        private String nome;
        private double saldo;
        private ArrayList<String> extrato;

        public Conta(String nome, double saldoInicial) {
            this.nome = nome;
            this.extrato = new ArrayList<>();
            if (saldoInicial > 0.0) {
                this.saldo = saldoInicial;
                this.extrato.add(String.format("Saldo inicial: R$%.2f", saldoInicial));
            } else {
                this.extrato.add("Conta aberta com saldo de R$0,00.");
            }
        }

        public void depositar(double valorDeposito) {
            if (valorDeposito > 0.0) {
                this.saldo += valorDeposito;
                this.extrato.add(String.format("Depósito recebido: +R$%.2f", valorDeposito));
            }
        }

        public boolean sacar(double valorSaque) {
            if (valorSaque > 0.0 && valorSaque <= saldo) {
                this.saldo -= valorSaque;
                this.extrato.add(String.format("Saque realizado: -R$%.2f", valorSaque));
                return true;
            }
            return false;
        }

        public boolean transferir(Conta contaDestino, double valorTransferencia) {
            if (valorTransferencia > 0.0 && valorTransferencia <= saldo) {
                this.saldo -= valorTransferencia;
                contaDestino.saldo += valorTransferencia;
                this.extrato.add(String.format("Transferência enviada para %s: -R$%.2f", contaDestino.getNome(), valorTransferencia));
                contaDestino.extrato.add(String.format("Transferência recebida de %s: +R$%.2f",this.getNome(), valorTransferencia));
				return true;
			}
			return false;
		}

		public double getSaldo() {
			return saldo;
		}
		public String getNome() {
			return nome;
		}
		public ArrayList<String> getExtratoList() {
			return extrato;
		}
	}
}