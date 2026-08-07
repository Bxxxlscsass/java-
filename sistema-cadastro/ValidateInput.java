import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ValidateInput extends JFrame {

    // Componentes da tela (Campos de texto)
    private JTextField txtFirstName = new JTextField(20);
    private JTextField txtLastName = new JTextField(20);
    private JTextField txtAddress = new JTextField(20);
    private JTextField txtCity = new JTextField(20);
    private JTextField txtState = new JTextField(20);
    private JTextField txtZip = new JTextField(20);
    private JTextField txtPhone = new JTextField(20);

    // Componentes da Tabela
    private JTable table;
    private DefaultTableModel tableModel;

    // Construtor: Cria a tela visível
    public ValidateInput() {
        setTitle("Validador de Cadastro com Tabela e Exclusão");
        setSize(700, 650); // Aumentado ligeiramente para o novo botão
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centraliza na tela

        // Painel Principal com BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Painel de Formulário (Grid Layout para os campos)
        JPanel formPanel = new JPanel(new GridLayout(8, 2, 8, 8));
        formPanel.add(new JLabel("Nome:"));
        formPanel.add(txtFirstName);
        formPanel.add(new JLabel("Sobrenome:"));
        formPanel.add(txtLastName);
        formPanel.add(new JLabel("Endereço:"));
        formPanel.add(txtAddress);
        formPanel.add(new JLabel("Cidade:"));
        formPanel.add(txtCity);
        formPanel.add(new JLabel("Estado:"));
        formPanel.add(txtState);
        formPanel.add(new JLabel("CEP (Formatos: 12345-678 ou 12345678):"));
        formPanel.add(txtZip);
        formPanel.add(new JLabel("Telefone (Formato: (11) 91234-5678):"));
        formPanel.add(txtPhone);

        // Botão de validação
        JButton btnValidate = new JButton("Validar e Adicionar na Tabela");
        formPanel.add(new JLabel("")); // Espaço em branco no Grid
        formPanel.add(btnValidate);

        // Configuração da Tabela (JTable)
        String[] colunas = {"Nome", "Sobrenome", "Endereço", "Cidade", "Estado", "CEP", "Telefone"};
        tableModel = new DefaultTableModel(colunas, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table); 
        scrollPane.setBorder(BorderFactory.createTitledBorder("Cadastros Aprovados"));

        // Painel Inferior para a Tabela e Ações da Tabela
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Botão de Excluir Registro
        JButton btnDelete = new JButton("Excluir Linha Selecionada");
        btnDelete.setBackground(new Color(220, 53, 69)); // Cor vermelha para alerta visual
        btnDelete.setForeground(Color.WHITE); // Texto branco para contraste
        btnDelete.setFocusPainted(false);

        // Painel exclusivo para alinhar o botão de exclusão à direita
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.add(btnDelete);
        centerPanel.add(actionPanel, BorderLayout.SOUTH);

        // Organiza os painéis principais na janela
        mainPanel.add(formPanel, BorderLayout.NORTH); // Formulário no topo
        mainPanel.add(centerPanel, BorderLayout.CENTER); // Tabela e botão de excluir embaixo

        add(mainPanel);

        // Ação do Botão Validar
        btnValidate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                executarVerificacao();
            }
        });

        // Ação do Botão Excluir
        btnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                executarExclusao();
            }
        });
    }

    // Método que roda ao clicar no botão de validação
    private void executarVerificacao() {
        StringBuilder erros = new StringBuilder();

        String firstName = txtFirstName.getText().trim();
        String lastName = txtLastName.getText().trim();
        String address = txtAddress.getText().trim();
        String city = txtCity.getText().trim();
        String state = txtState.getText().trim();
        String zip = txtZip.getText().trim();
        String phone = txtPhone.getText().trim();

        if (!validateFirstName(firstName)) {
            erros.append("- Nome inválido (Inicie com maiúscula)\n");
        }
        if (!validateLastName(lastName)) {
            erros.append("- Sobrenome inválido\n");
        }
        if (!validateAddress(address)) {
            erros.append("- Endereço inválido\n");
        }
        if (!validateCity(city)) {
            erros.append("- Cidade inválida\n");
        }
        if (!validateState(state)) {
            erros.append("- Estado inválido\n");
        }
        if (!validateZip(zip)) {
            erros.append("- CEP inválido BR (Ex: 12345-678)\n");
        }
        if (!validatePhone(phone)) {
            erros.append("- Telefone celular inválido (Ex: (11) 98888-7777)\n");
        }

        if (erros.length() > 0) {
            JOptionPane.showMessageDialog(this, "Erros encontrados:\n\n" + erros.toString(), "Falha na Validação", JOptionPane.ERROR_MESSAGE);
        } else {
            Object[] novaLinha = {firstName, lastName, address, city, state, zip, phone};
            tableModel.addRow(novaLinha);
            limparCampos();
            JOptionPane.showMessageDialog(this, "Cadastro validado e adicionado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Método que roda ao clicar no botão de exclusão
    private void executarExclusao() {
        int linhaSelecionada = table.getSelectedRow();

        // Verifica se o usuário realmente selecionou uma linha (-1 significa nenhuma seleção)
        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, selecione uma linha na tabela para excluir.", "Nenhuma Linha Selecionada", JOptionPane.WARNING_MESSAGE);
        } else {
            // Confirmação de segurança antes de apagar
            int resposta = JOptionPane.showConfirmDialog(this, "Tem certeza que deseja excluir o registro selecionado?", "Confirmar Exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            
            if (resposta == JOptionPane.YES_OPTION) {
                tableModel.removeRow(linhaSelecionada);
                JOptionPane.showMessageDialog(this, "Registro excluído com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void limparCampos() {
        txtFirstName.setText("");
        txtLastName.setText("");
        txtAddress.setText("");
        txtCity.setText("");
        txtState.setText("");
        txtZip.setText("");
        txtPhone.setText("");
    }

    // ================= MÉTODOS DE VALIDAÇÃO REVISADOS =================

    public static boolean validateFirstName(String firstName) {
        return firstName.matches("[A-ZÁÉÍÓÚÂÊÔÇ][a-zA-Záéíóúâêôãõç]*");
    }    

    public static boolean validateLastName(String lastName) {
        return lastName.matches("[a-zA-ZáéíóúâêôãõçÁÉÍÓÚÂÊÔÇ]+(['-][a-zA-ZáéíóúâêôãõçÁÉÍÓÚÂÊÔÇ]+)*");
    }

    public static boolean validateAddress(String address) {
        return address.matches("[a-zA-ZáéíóúâêôãõçÁÉÍÓÚÂÊÔÇ0-9\\s.,-]+");
    }

    public static boolean validateCity(String city) {
        return city.matches("[a-zA-ZáéíóúâêôãõçÁÉÍÓÚÂÊÔÇ\\s]+");
    }

    public static boolean validateState(String state) {
        return state.matches("[a-zA-ZáéíóúâêôãõçÁÉÍÓÚÂÊÔÇ\\s]+");
    }

    public static boolean validateZip(String zip) {
        return zip.matches("\\d{5}-?\\d{3}");
    }

    public static boolean validatePhone(String phone) {
        return phone.matches("^\\(?\\d{2}\\)?\\s?9\\d{4}-?\\d{4}$");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ValidateInput().setVisible(true);
            }
        });
    }
}
