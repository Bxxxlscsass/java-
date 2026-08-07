import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class SistemaCompletoMenu extends JFrame {

    // Gerenciador de Telas
    private JPanel cardsPanel;
    private CardLayout cardLayout;

    // Definição da Paleta de Cores do Tema Escuro
    private final Color COLOR_BG_MAIN = new Color(43, 43, 43);      // #2B2B2B
    private final Color COLOR_BG_PANEL = new Color(60, 63, 65);     // #3C3F41
    private final Color COLOR_TEXT_LIGHT = new Color(230, 230, 230); // Branco Fosco
    private final Color COLOR_TEXT_DARK = new Color(180, 180, 180);  // Cinza Claro
    private final Color COLOR_BORDER = new Color(85, 85, 85);        // Bordas Suaves

    // --- Componentes da Tela de Áudio ---
    private JProgressBar progressBarAudio;
    private JLabel lblVolumeValue;
    private JSlider sliderSensitivity;
    private TargetDataLine audioLine;
    private boolean isAudioRunning = true;

    // --- Componentes da Tela de Backup ---
    private JTextField txtOrigem = new JTextField(25);
    private JTextField txtDestino = new JTextField(25);
    private JTextArea txtConsoleLog = new JTextArea(15, 50);
    private JButton btnIniciarBackup = new JButton("Iniciar Backup");
    private JButton btnLimparLog = new JButton("Limpar Logs");
    private JProgressBar progressBarBackup = new JProgressBar();

    // --- Componentes da Tela de Desempenho ---
    private JProgressBar progressBarCPU;
    private JProgressBar progressBarRAM;
    private JLabel lblCPUText;
    private JLabel lblRAMText;
    private Timer performanceTimer;
    private OperatingSystemMXBean osBean;

    public SistemaCompletoMenu() {
        setTitle("Central de Utilitários Corporativos (Dark Edition)");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        // Painel Raiz (Menu Lateral + Área de Conteúdo)
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(COLOR_BG_MAIN);

        // 1. Barra Lateral (Sidebar) Dark
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new GridLayout(5, 1, 5, 5));
        sidebar.setPreferredSize(new Dimension(180, 700));
        sidebar.setBackground(new Color(30, 30, 30)); // Mais escuro que o fundo principal
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton btnInicio = criarBotaoMenu("Início / Painel");
        JButton btnAudio = criarBotaoMenu("Assistente de Som");
        JButton btnBackup = criarBotaoMenu("Gerenciador Backup");
        JButton btnDesempenho = criarBotaoMenu("Monitor de Sistema");
        JButton btnSobre = criarBotaoMenu("Sobre o Sistema");

        sidebar.add(btnInicio); sidebar.add(btnAudio); sidebar.add(btnBackup);
        sidebar.add(btnDesempenho); sidebar.add(btnSobre);

        // 2. Painel Central (CardLayout)
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        cardsPanel.setBackground(COLOR_BG_MAIN);

        cardsPanel.add(criarTelaInicio(), "INICIO");
        cardsPanel.add(criarTelaAudio(), "AUDIO");
        cardsPanel.add(criarTelaBackup(), "BACKUP");
        cardsPanel.add(criarTelaDesempenho(), "DESEMPENHO");
        cardsPanel.add(criarTelaSobre(), "SOBRE");

        // 3. Configuração de Eventos dos Botões do Menu
        btnInicio.addActionListener(e -> cardLayout.show(cardsPanel, "INICIO"));
        btnAudio.addActionListener(e -> cardLayout.show(cardsPanel, "AUDIO"));
        btnBackup.addActionListener(e -> cardLayout.show(cardsPanel, "BACKUP"));
        btnDesempenho.addActionListener(e -> cardLayout.show(cardsPanel, "DESEMPENHO"));
        btnSobre.addActionListener(e -> cardLayout.show(cardsPanel, "SOBRE"));

        rootPanel.add(sidebar, BorderLayout.WEST);
        rootPanel.add(cardsPanel, BorderLayout.CENTER);
        add(rootPanel);

        // Gerenciamento de fechamento seguro do hardware e timers
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                isAudioRunning = false;
                if (audioLine != null) { audioLine.stop(); audioLine.close(); }
                if (performanceTimer != null) { performanceTimer.stop(); }
            }
        });

        startAudioCaptureThread();
        startPerformanceMonitoring();
    }

    // Método customizado para estilizar os componentes com Dark Mode
    private void estilizarPainelEscuro(JPanel panel, String titulo) {
        panel.setBackground(COLOR_BG_PANEL);
        if (titulo != null) {
            TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(COLOR_BORDER), titulo);
            border.setTitleColor(COLOR_TEXT_LIGHT);
            border.setTitleFont(new Font("Arial", Font.BOLD, 12));
            panel.setBorder(border);
        }
    }

    private void estilizarCampoTextoDark(JTextField field) {
        field.setBackground(COLOR_BG_MAIN);
        field.setForeground(COLOR_TEXT_LIGHT);
        field.setCaretColor(COLOR_TEXT_LIGHT);
        field.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
    }

    private JButton criarBotaoMenu(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setForeground(COLOR_TEXT_LIGHT);
        btn.setBackground(COLOR_BG_PANEL);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        return btn;
    }

    // Seleciona diretório e preenche o campo (moved earlier to avoid forward-reference issues)
    private void selecionarDiretorio(JTextField targetField) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            targetField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    // ================= TELA 1: INÍCIO (DASHBOARD) =================
    private JPanel criarTelaInicio() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_BG_MAIN);
        
        JLabel lblBoasVindas = new JLabel("Painel Administrativo de Utilitários");
        lblBoasVindas.setFont(new Font("Arial", Font.BOLD, 24));
        lblBoasVindas.setForeground(COLOR_TEXT_LIGHT);
        
        JLabel lblSub = new JLabel("Navegue pelas ferramentas de hardware, backup e telemetria usando o menu lateral.");
        lblSub.setFont(new Font("Arial", Font.PLAIN, 14));
        lblSub.setForeground(COLOR_TEXT_DARK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(10, 10, 10, 10);
        panel.add(lblBoasVindas, gbc);
        gbc.gridy = 1;
        panel.add(lblSub, gbc);
        return panel;
    }

    // ================= TELA 2: ASSISTENTE DE SOM =================
    private JPanel criarTelaAudio() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_BG_MAIN);

        JPanel audioPanel = new JPanel();
        audioPanel.setLayout(new BoxLayout(audioPanel, BoxLayout.Y_AXIS));
        estilizarPainelEscuro(audioPanel, "Configuração do Microfone");
        audioPanel.setPreferredSize(new Dimension(400, 320));

        JLabel lblAudioInstruction = new JLabel("Fale no microfone para testar os níveis de captação:");
        lblAudioInstruction.setForeground(COLOR_TEXT_LIGHT);
        lblAudioInstruction.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        progressBarAudio = new JProgressBar(0, 100);
        progressBarAudio.setStringPainted(true);
        progressBarAudio.setBackground(COLOR_BG_MAIN);
        progressBarAudio.setForeground(new Color(40, 167, 69));
        progressBarAudio.setMaximumSize(new Dimension(350, 30));
        progressBarAudio.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblVolumeValue = new JLabel("Volume RMS: 0");
        lblVolumeValue.setForeground(COLOR_TEXT_DARK);
        lblVolumeValue.setAlignmentX(Component.CENTER_ALIGNMENT);

        sliderSensitivity = new JSlider(1, 10, 3);
        sliderSensitivity.setBackground(COLOR_BG_PANEL);
        sliderSensitivity.setForeground(COLOR_TEXT_LIGHT);
        sliderSensitivity.setMajorTickSpacing(1);
        sliderSensitivity.setPaintTicks(true);
        sliderSensitivity.setPaintLabels(true);
        sliderSensitivity.setMaximumSize(new Dimension(350, 50));
        sliderSensitivity.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblGanho = new JLabel("Sensibilidade Dinâmica (Multiplicador de Ganho):");
        lblGanho.setForeground(COLOR_TEXT_LIGHT);
        lblGanho.setAlignmentX(Component.CENTER_ALIGNMENT);

        audioPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        audioPanel.add(lblAudioInstruction);
        audioPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        audioPanel.add(progressBarAudio);
        audioPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        audioPanel.add(lblVolumeValue);
        audioPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        audioPanel.add(lblGanho);
        audioPanel.add(sliderSensitivity);

        panel.add(audioPanel);
        return panel;
    }

    // ================= TELA 3: GERENCIADOR DE BACKUP =================
    private JPanel criarTelaBackup() {
        JPanel backupPanel = new JPanel(new BorderLayout(15, 15));
        backupPanel.setBackground(COLOR_BG_MAIN);
        backupPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel caminhosPanel = new JPanel(new GridBagLayout());
        estilizarPainelEscuro(caminhosPanel, "Configuração do Robocopy");
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JButton btnSelecionarOrigem = criarBotaoMenu("Procurar...");
        JButton btnSelecionarDestino = criarBotaoMenu("Procurar...");
        estilizarCampoTextoDark(txtOrigem);
        estilizarCampoTextoDark(txtDestino);
        
        JLabel lblO = new JLabel("Pasta de Origem:"); lblO.setForeground(COLOR_TEXT_LIGHT);
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0; caminhosPanel.add(lblO, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; caminhosPanel.add(txtOrigem, gbc);
        gbc.gridx = 2; gbc.weightx = 0.0; caminhosPanel.add(btnSelecionarOrigem, gbc);
        
        JLabel lblD = new JLabel("Pasta de Destino:"); lblD.setForeground(COLOR_TEXT_LIGHT);
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0; caminhosPanel.add(lblD, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; caminhosPanel.add(txtDestino, gbc);
        gbc.gridx = 2; gbc.weightx = 0.0; caminhosPanel.add(btnSelecionarDestino, gbc);
        
        txtConsoleLog.setEditable(false);
        txtConsoleLog.setBackground(Color.BLACK);
        txtConsoleLog.setForeground(new Color(0, 255, 65));
        txtConsoleLog.setFont(new Font("Consolas", Font.PLAIN, 12));
        JScrollPane scrollConsole = new JScrollPane(txtConsoleLog);
        scrollConsole.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        
        progressBarBackup.setIndeterminate(false);
        progressBarBackup.setStringPainted(true);
        progressBarBackup.setBackground(COLOR_BG_MAIN);
        progressBarBackup.setForeground(new Color(0, 120, 215));
        progressBarBackup.setString("Aguardando Operação");
        
        JPanel inferiorPanel = new JPanel(new BorderLayout(5, 5));
        inferiorPanel.setBackground(COLOR_BG_MAIN);
        JPanel acoesBackupPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        acoesBackupPanel.setBackground(COLOR_BG_MAIN);
        
        btnIniciarBackup.setFont(new Font("Arial", Font.BOLD, 14));
        btnLimparLog.setFont(new Font("Arial", Font.PLAIN, 14));
        btnIniciarBackup.setBackground(COLOR_BG_PANEL); 
        btnIniciarBackup.setForeground(COLOR_TEXT_LIGHT);
        btnLimparLog.setBackground(COLOR_BG_PANEL); 
        btnLimparLog.setForeground(COLOR_TEXT_LIGHT);
        
        acoesBackupPanel.add(btnIniciarBackup);
        acoesBackupPanel.add(btnLimparLog);
        inferiorPanel.add(progressBarBackup, BorderLayout.NORTH);
        inferiorPanel.add(acoesBackupPanel, BorderLayout.SOUTH);
        backupPanel.add(caminhosPanel, BorderLayout.NORTH);
        backupPanel.add(scrollConsole, BorderLayout.CENTER);
        backupPanel.add(inferiorPanel, BorderLayout.SOUTH);
        btnSelecionarOrigem.addActionListener(e -> selecionarDiretorio(txtOrigem));
        btnSelecionarDestino.addActionListener(e -> selecionarDiretorio(txtDestino));
        btnIniciarBackup.addActionListener(e -> acionarBackupBackground());
        btnLimparLog.addActionListener(e -> txtConsoleLog.setText(""));
        return backupPanel;
    }
    
// ================= TELA 4: MONITOR DE DESEMPENHO (MOTO ESCURO + GC) =================
    private JPanel criarTelaDesempenho() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(COLOR_BG_MAIN);
        JPanel containerPanel = new JPanel();
        containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));
        estilizarPainelEscuro(containerPanel, "Telemetria de Hardware (Tempo Real)");
        containerPanel.setPreferredSize(new Dimension(500, 360));
        
        // CPU
        lblCPUText = new JLabel("Uso de Processador (CPU): 0%");
        lblCPUText.setForeground(COLOR_TEXT_LIGHT);
        lblCPUText.setFont(new Font("Arial", Font.BOLD, 13));
        lblCPUText.setAlignmentX(Component.CENTER_ALIGNMENT);
        progressBarCPU = new JProgressBar(0, 100);
        progressBarCPU.setStringPainted(true);
        progressBarCPU.setBackground(COLOR_BG_MAIN);
        progressBarCPU.setForeground(new Color(0, 120, 215));
        progressBarCPU.setMaximumSize(new Dimension(440, 35));
        progressBarCPU.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // RAM
        lblRAMText = new JLabel("Uso de Memória RAM: 0 MB / 0 MB");
        lblRAMText.setForeground(COLOR_TEXT_LIGHT);
        lblRAMText.setFont(new Font("Arial", Font.BOLD, 13));
        lblRAMText.setAlignmentX(Component.CENTER_ALIGNMENT);
        progressBarRAM = new JProgressBar(0, 100);
        progressBarRAM.setStringPainted(true);
        progressBarRAM.setBackground(COLOR_BG_MAIN);
        progressBarRAM.setForeground(new Color(114, 34, 131));
        progressBarRAM.setMaximumSize(new Dimension(440, 35));
        progressBarRAM.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // NOVO: Botão de Otimização de RAM
        JButton btnOptimizeRAM = new JButton("Otimizar Memória RAM");
        btnOptimizeRAM.setFont(new Font("Arial", Font.BOLD, 13));
        btnOptimizeRAM.setBackground(new Color(40, 167, 69)); // Botão verde para ação positiva
        btnOptimizeRAM.setForeground(Color.WHITE);
        btnOptimizeRAM.setFocusPainted(false);
        btnOptimizeRAM.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnOptimizeRAM.setMaximumSize(new Dimension(220, 35));
        
        // Lógica de acionamento do coletor de lixo
        btnOptimizeRAM.addActionListener(e -> {
            txtConsoleLog.append("[JVM] Chamando Coletor de Lixo (System.gc())...\n");
            System.gc(); // Força a liberação da memória morta no ecossistema Java
            atualizarDadosInterfaceDesempenho(); // Força atualização visual imediata
        });
        
        JLabel lblAviso = new JLabel("Atualizando a cada 1000ms automaticamente.");
        lblAviso.setFont(new Font("Arial", Font.ITALIC, 11));
        lblAviso.setForeground(Color.GRAY);
        lblAviso.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        containerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        containerPanel.add(lblCPUText);
        containerPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        containerPanel.add(progressBarCPU);
        containerPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        containerPanel.add(lblRAMText);
        containerPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        containerPanel.add(progressBarRAM);
        containerPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        containerPanel.add(btnOptimizeRAM);
        containerPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        containerPanel.add(lblAviso);
        
        mainPanel.add(containerPanel);
        return mainPanel;
    }
    
    // ================= TELA 5: SOBRE O SISTEMA =================
    private JPanel criarTelaSobre() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_BG_MAIN);
        JTextArea txtInfo = new JTextArea("Sistema de Utilitários, Diagnóstico e Desempenho v3.5\n\n" +
                "Desenvolvido em Java Swing nativo com Interface Dark.\n\n" +
                "Recursos Implementados:\n" +
                "- Coleta de Lixo Forçada (Otimização reativa de heap RAM da JVM)\n" +
                "- Telemetria Estendida (Monitor de consumo dinâmico com threads em EDF)\n" +
                "- Java Sound API (Cálculo RMS e sensibilidade via hardware)\n" +
                "- Microsoft Robocopy Engine (Espaço em disco com Java NIO corporativo)\n\n" +
                "© 2026 Todos os direitos reservados.");
        txtInfo.setEditable(false);
        txtInfo.setBackground(COLOR_BG_MAIN);
        txtInfo.setForeground(COLOR_TEXT_LIGHT);
        txtInfo.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(txtInfo);
        return panel;
    }
    
    // ================= LÓGICAS OPERACIONAIS E BACKGROUND =================
    private void startPerformanceMonitoring() {
        performanceTimer = new Timer(1000, e -> atualizarDadosInterfaceDesempenho());
        performanceTimer.start();
    }
    
    private void atualizarDadosInterfaceDesempenho() {
        double cpuLoad = osBean.getCpuLoad();
        int cpuPercentage = (int) (cpuLoad * 100);
        if (cpuPercentage < 0) cpuPercentage = 0;
        
        long totalRamBytes = osBean.getTotalMemorySize();
        long livreRamBytes = osBean.getFreeMemorySize();
        long usadaRamBytes = totalRamBytes - livreRamBytes;
        long totalRAM_MB = totalRamBytes / (1024 * 1024);
        long usadaRAM_MB = usadaRamBytes / (1024 * 1024);
        int ramPercentage = (int) (((double) usadaRamBytes / totalRamBytes) * 100);
        
        progressBarCPU.setValue(cpuPercentage);
        lblCPUText.setText("Uso de Processador (CPU): " + cpuPercentage + "%");
        progressBarRAM.setValue(ramPercentage);
        lblRAMText.setText(String.format("Uso de Memória RAM: %d MB / %d MB (%d%%)", usadaRAM_MB, totalRAM_MB, ramPercentage));
        
        gerenciarCorDesempenho(progressBarCPU, cpuPercentage);
        gerenciarCorDesempenho(progressBarRAM, ramPercentage);
    }
    
    private void gerenciarCorDesempenho(JProgressBar bar, int pct) {
        if (pct > 85) {
            bar.setForeground(new Color(220, 53, 69)); // Vermelho
        } else if (pct > 60) {
            bar.setForeground(new Color(255, 193, 7)); // Amarelo
        } else {
            if (bar == progressBarCPU) bar.setForeground(new Color(0, 120, 215));
            else bar.setForeground(new Color(114, 34, 131));
        }
    }
    
    private void acionarBackupBackground() {
        String origemStr = txtOrigem.getText();
        String destinoStr = txtDestino.getText();
        if (origemStr.isEmpty() || destinoStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecione as pastas.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            long tamanhoOrigem = Files.walk(Paths.get(origemStr)).filter(p -> p.toFile().isFile()).mapToLong(p -> p.toFile().length()).sum();
            long espacoLivre = new File(destinoStr).getUsableSpace();
            if (tamanhoOrigem > espacoLivre) {
                JOptionPane.showMessageDialog(this, "Espaço em disco insuficiente no destino!", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (Exception ex) {
            txtConsoleLog.append("[AVISO] Falha ao checar espaço em disco.\n");
        }
        
        btnIniciarBackup.setEnabled(false);
        progressBarBackup.setIndeterminate(true);
        progressBarBackup.setString("Copiando Arquivos...");
        
        SwingWorker<Integer, String> worker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() throws Exception {
                Process process = new ProcessBuilder("CMD", "/C", "ROBOCOPY", origemStr, destinoStr, "/E", "/Z", "/MT:16").redirectErrorStream(true).start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String linha;
                while ((linha = reader.readLine()) != null) { publish(linha); }
                return process.waitFor();
            }
            
            @Override
            protected void process(List<String> chunks) {
                for (String linha : chunks) { txtConsoleLog.append(linha + "\n"); }
            }
            
            @Override
            protected void done() {
                btnIniciarBackup.setEnabled(true);
                progressBarBackup.setIndeterminate(false);
                progressBarBackup.setValue(100);
                progressBarBackup.setString("Cópia Finalizada");
            }
        };
        worker.execute();
    }
    
    private void startAudioCaptureThread() {
        new Thread(() -> {
            try {
                AudioFormat format = new AudioFormat(44100.0f, 16, 1, true, false);
                audioLine = (TargetDataLine) AudioSystem.getLine(new DataLine.Info(TargetDataLine.class, format));
                audioLine.open(format);
                audioLine.start();
                byte[] buffer = new byte[(int) (format.getSampleRate() * 0.02) * format.getFrameSize()];
                while (isAudioRunning) {
                    int bytesRead = audioLine.read(buffer, 0, buffer.length);
                    if (bytesRead > 0) {
                        double sum = 0;
                        for (int i = 0; i < bytesRead; i += 2) {
                            short sample = (short) ((buffer[i + 1] << 8) | (buffer[i] & 0xFF));
                            sum += (double) sample * sample;
                        }
                        double rms = Math.sqrt(sum / (bytesRead / 2.0));
                        int pct = Math.min(Math.max((int) (rms * sliderSensitivity.getValue() / 100), 0), 100);
                        SwingUtilities.invokeLater(() -> {
                            progressBarAudio.setValue(pct);
                            lblVolumeValue.setText(String.format("Volume RMS: %.0f", rms));
                            progressBarAudio.setForeground(pct > 85 ? new Color(220, 53, 69) : new Color(40, 167, 69));
                        });
                    }
                }
            } catch (Exception ignored) {}
        }).start();
    }
    
    public static void main(String[] args) {
        // Alinha os componentes do Look and Feel para responderem bem às customizações manuais de cores
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new SistemaCompletoMenu().setVisible(true));
    }
}