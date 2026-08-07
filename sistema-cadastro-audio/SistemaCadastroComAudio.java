import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
// Removed unused import: java.awt.event.ActionListener
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class SistemaCadastroComAudio extends JFrame {

    // Componentes do Formulário de Cadastro
    private JTextField txtFirstName = new JTextField(15);
    private JTextField txtLastName = new JTextField(15);
    private JTextField txtAddress = new JTextField(15);
    private JTextField txtCity = new JTextField(15);
    private JTextField txtState = new JTextField(15);
    private JTextField txtZip = new JTextField(15);
    private JTextField txtPhone = new JTextField(15);

    // Componentes da Tabela
    private JTable table;
    private DefaultTableModel tableModel;

    // Componentes do Monitor de Áudio
    private JProgressBar progressBar;
    private JLabel lblVolumeValue;
    private JSlider sliderSensitivity;
    private TargetDataLine audioLine;
    private boolean isAudioRunning = true;

    public SistemaCadastroComAudio() {
        setTitle("Sistema de Cadastro com Assistente de Áudio");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Painel Principal (BorderLayout)
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ================= PANELS SUPERIORES (FORMULÁRIO + ÁUDIO) =================
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 5, 0, 5);

        // 1. Painel do Formulário de Cadastro (Esquerda)
        JPanel formPanel = new JPanel(new GridLayout(8, 2, 6, 6));
        formPanel.setBorder(BorderFactory.createTitledBorder("Dados do Usuário"));
        formPanel.add(new JLabel("Nome:")); formPanel.add(txtFirstName);
        formPanel.add(new JLabel("Sobrenome:")); formPanel.add(txtLastName);
        formPanel.add(new JLabel("Endereço:")); formPanel.add(txtAddress);
        formPanel.add(new JLabel("Cidade:")); formPanel.add(txtCity);
        formPanel.add(new JLabel("Estado:")); formPanel.add(txtState);
        formPanel.add(new JLabel("CEP:")); formPanel.add(txtZip);
        formPanel.add(new JLabel("Telefone:")); formPanel.add(txtPhone);
        
        JButton btnValidate = new JButton("Validar e Cadastrar");
        formPanel.add(new JLabel("")); 
        formPanel.add(btnValidate);

        // 2. Painel do Assistente de Áudio (Direita)
        JPanel audioPanel = new JPanel();
        audioPanel.setLayout(new BoxLayout(audioPanel, BoxLayout.Y_AXIS));
        audioPanel.setBorder(BorderFactory.createTitledBorder("Assistente de Som"));
        audioPanel.setPreferredSize(new Dimension(300, 250));

        JLabel lblAudioInstruction = new JLabel("Teste seu microfone antes de prosseguir:");
        lblAudioInstruction.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setForeground(new Color(40, 167, 69));
        progressBar.setMaximumSize(new Dimension(260, 25));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblVolumeValue = new JLabel("Volume RMS: 0");
        lblVolumeValue.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSliderTitle = new JLabel("Sensibilidade (Ganho):");
        lblSliderTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // JSlider: Mapeia de 1 (sem ganho adicional) até 10 (multiplica por 10)
        sliderSensitivity = new JSlider(1, 10, 3);
        sliderSensitivity.setMajorTickSpacing(1);
        sliderSensitivity.setPaintTicks(true);
        sliderSensitivity.setPaintLabels(true);
        sliderSensitivity.setMaximumSize(new Dimension(260, 45));
        sliderSensitivity.setAlignmentX(Component.CENTER_ALIGNMENT);

        audioPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        audioPanel.add(lblAudioInstruction);
        audioPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        audioPanel.add(progressBar);
        audioPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        audioPanel.add(lblVolumeValue);
        audioPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        audioPanel.add(lblSliderTitle);
        audioPanel.add(sliderSensitivity);

        // Adiciona formulário e áudio lado a lado no painel superior
        gbc.gridx = 0; gbc.weightx = 0.65; topPanel.add(formPanel, gbc);
        gbc.gridx = 1; gbc.weightx = 0.35; topPanel.add(audioPanel, gbc);

        // ================= PAINEL CENTRAL/INFERIOR (TABELA + EXCLUSÃO) =================
        String[] colunas = {"Nome", "Sobrenome", "Endereço", "Cidade", "Estado", "CEP", "Telefone"};
        tableModel = new DefaultTableModel(colunas, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Cadastros Aprovados"));

        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        JButton btnDelete = new JButton("Excluir Linha Selecionada");
        btnDelete.setBackground(new Color(220, 53, 69));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setFocusPainted(false);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.add(btnDelete);
        centerPanel.add(actionPanel, BorderLayout.SOUTH);

        // Montagem final da janela
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        add(mainPanel);

        // ================= EVENTOS E LISTENERS =================
        btnValidate.addActionListener(e -> executarVerificacao());
        btnDelete.addActionListener(e -> executarExclusao());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                isAudioRunning = false;
                if (audioLine != null) {
                    audioLine.stop();
                    audioLine.close();
                }
            }
        });

        // Inicia captura de áudio em segundo plano
        startAudioCaptureThread();
    }

    private void executarVerificacao() {
        StringBuilder erros = new StringBuilder();
        String firstName = txtFirstName.getText().trim();
        String lastName = txtLastName.getText().trim();
        String address = txtAddress.getText().trim();
        String city = txtCity.getText().trim();
        String state = txtState.getText().trim();
        String zip = txtZip.getText().trim();
        String phone = txtPhone.getText().trim();

        if (!validateFirstName(firstName)) erros.append("- Nome inválido\n");
        if (!validateLastName(lastName)) erros.append("- Sobrenome inválido\n");
        if (!validateAddress(address)) erros.append("- Endereço inválido\n");
        if (!validateCity(city)) erros.append("- Cidade inválida\n");
        if (!validateState(state)) erros.append("- Estado inválido\n");
        if (!validateZip(zip)) erros.append("- CEP inválido\n");
        if (!validatePhone(phone)) erros.append("- Telefone inválido\n");

        if (erros.length() > 0) {
            JOptionPane.showMessageDialog(this, "Erros:\n" + erros, "Falha na Validação", JOptionPane.ERROR_MESSAGE);
        } else {
            tableModel.addRow(new Object[]{firstName, lastName, address, city, state, zip, phone});
            limparCampos();
            JOptionPane.showMessageDialog(this, "Adicionado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void executarExclusao() {
        int linha = table.getSelectedRow();
        if (linha == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma linha para excluir.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Deseja excluir?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            tableModel.removeRow(linha);
        }
    }

    private void limparCampos() {
        txtFirstName.setText(""); txtLastName.setText(""); txtAddress.setText("");
        txtCity.setText(""); txtState.setText(""); txtZip.setText(""); txtPhone.setText("");
    }

    private void startAudioCaptureThread() {
        Thread audioThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    AudioFormat format = new AudioFormat(44100.0f, 16, 1, true, false);
                    DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

                    if (!AudioSystem.isLineSupported(info)) {
                        return;
                    }

                    audioLine = (TargetDataLine) AudioSystem.getLine(info);
                    audioLine.open(format);
                    audioLine.start();

                    int bufferSize = (int) (format.getSampleRate() * 0.02) * format.getFrameSize();
                    byte[] buffer = new byte[bufferSize];

                    while (isAudioRunning) {
                        int bytesRead = audioLine.read(buffer, 0, buffer.length);
                        if (bytesRead > 0) {
                            double volumeRMS = calculateVolumeRMS(buffer, bytesRead);

                            // aplica ganho do slider
                            int sensitivityFactor = sliderSensitivity.getValue();
                            double boostedVolume = volumeRMS * sensitivityFactor;

                            // mapa para 0-100
                            int percentage = (int) (boostedVolume / 100.0);
                            final int finalPercentage = Math.min(Math.max(percentage, 0), 100);
                            final double finalVolume = volumeRMS;

                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setValue(finalPercentage);
                                    lblVolumeValue.setText(String.format("Volume RMS: %.0f", finalVolume));
                                    if (finalPercentage > 85) {
                                        progressBar.setForeground(new Color(220, 53, 69));
                                    } else {
                                        progressBar.setForeground(new Color(40, 167, 69));
                                    }
                                }
                            });
                        }
                    }
                } catch (LineUnavailableException e) {
                    e.printStackTrace();
                }
            }
        });
        audioThread.setDaemon(true);
        audioThread.start();
    }

    private double calculateVolumeRMS(byte[] audioBytes, int bytesRead) {
        double sum = 0;
        int samplesCount = bytesRead / 2;
        for (int i = 0; i < bytesRead; i += 2) {
            short sample = (short) ((audioBytes[i + 1] << 8) | (audioBytes[i] & 0xFF));
            sum += (double) sample * sample;
        }
        return samplesCount == 0 ? 0 : Math.sqrt(sum / samplesCount);
    }

    // REGEX VALIDATIONS
    public static boolean validateFirstName(String f) {
        return f.matches("[A-ZÁÉÍÓÚÂÊÔÇ][a-zA-Záéíóúâêôãõç]+");
    }

    public static boolean validateLastName(String l) {
        return l.matches("[a-zA-ZáéíóúâêôãõçÁÉÍÓÚÂÊÔÇ]+(['-][a-zA-ZáéíóúâêôãõçÁÉÍÓÚÂÊÔÇ]+)*");
    }

    public static boolean validateAddress(String a) {
        return a.matches("[a-zA-ZáéíóúâêôãõçÁÉÍÓÚÂÊÔÇ0-9\\s.,-]+");
    }

    public static boolean validateCity(String c) {
        return c.matches("[a-zA-ZáéíóúâêôãõçÁÉÍÓÚÂÊÔÇ\\s]+");
    }

    public static boolean validateState(String s) {
        return s.matches("[a-zA-ZáéíóúâêôãõçÁÉÍÓÚÂÊÔÇ\\s]+");
    }

    public static boolean validateZip(String z) {
        return z.matches("\\d{5}-?\\d{3}");
    }

    public static boolean validatePhone(String p) {
        return p.matches("^\\(?\\d{2}\\)?\\s?9\\d{4}-?\\d{4}$");
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new SistemaCadastroComAudio().setVisible(true));
    }
}

