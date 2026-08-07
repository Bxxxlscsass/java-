import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean; // Native JDK extension
import javax.sound.sampled.*;

public class MonitorHardwareNativo extends JFrame {

    // Graphical Components
    private JLabel lblCpuNome;
    private JLabel lblCores;
    private JProgressBar barraTemperatura;
    private JProgressBar barraRam;
    private JLabel lblDiagnosticoLimpeza;
    private JTextArea txtAudioDispositivos;
    private JButton btnTestarAudio;
    private Timer timerAtualizacao;

    // Native Java Management Bean for OS/Hardware metrics
    private OperatingSystemMXBean osBean;

    public MonitorHardwareNativo() {
        // 1. WINDOW SETTINGS
        setTitle("Monitor de Hardware 100% Nativo");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));

        // Initialize Native Java Operating System Monitor
        osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        // 2. TOP PANEL: CPU INFO (NATIVE)
        JPanel painelCpu = new JPanel(new GridLayout(2, 1, 5, 5));
        painelCpu.setBorder(BorderFactory.createTitledBorder("Informações do Sistema"));
        
        // Native properties fetched directly from the OS environment
        String arch = System.getenv("PROCESSOR_IDENTIFIER") != null ? System.getenv("PROCESSOR_IDENTIFIER") : System.getProperty("os.arch");
        lblCpuNome = new JLabel(" Arquitetura/Processador: " + arch);
        lblCpuNome.setFont(new Font("Arial", Font.BOLD, 11));
        
        int cores = Runtime.getRuntime().availableProcessors();
        lblCores = new JLabel(" Processadores Lógicos Disponíveis (Threads): " + cores);
        
        painelCpu.add(lblCpuNome);
        painelCpu.add(lblCores);
        add(painelCpu, BorderLayout.NORTH);

        // 3. CENTER PANEL: TELEMETRY & METRICS
        JPanel painelCentro = new JPanel(new BorderLayout(10, 10));
        
        JPanel painelTelemetria = new JPanel(new GridLayout(5, 1, 5, 5));
        painelTelemetria.setBorder(BorderFactory.createTitledBorder("Telemetria Nativa (Atualização: 2s)"));
        
        // Temperature UI elements
        barraTemperatura = new JProgressBar(0, 100);
        barraTemperatura.setStringPainted(true);
        barraTemperatura.setFont(new Font("Arial", Font.BOLD, 12));
        lblDiagnosticoLimpeza = new JLabel("Calculando...", SwingConstants.CENTER);
        lblDiagnosticoLimpeza.setFont(new Font("Arial", Font.ITALIC, 11));

        // RAM UI elements
        barraRam = new JProgressBar(0, 100);
        barraRam.setStringPainted(true);
        barraRam.setFont(new Font("Arial", Font.BOLD, 12));
        barraRam.setForeground(new Color(0, 123, 255)); 

        painelTelemetria.add(new JLabel(" Estimativa de Temperatura Baseada em Carga de Trabalho:"));
        painelTelemetria.add(barraTemperatura);
        painelTelemetria.add(new JLabel(" Uso de Memória RAM do Sistema:"));
        painelTelemetria.add(barraRam);
        painelTelemetria.add(lblDiagnosticoLimpeza);
        
        painelCentro.add(painelTelemetria, BorderLayout.NORTH);

        // Audio listing display elements
        JPanel painelAudio = new JPanel(new BorderLayout());
        painelAudio.setBorder(BorderFactory.createTitledBorder("Dispositivos de Áudio Instalados (Java Sound)"));
        txtAudioDispositivos = new JTextArea();
        txtAudioDispositivos.setEditable(false);
        txtAudioDispositivos.setBackground(new Color(245, 245, 245));
        JScrollPane scrollAudio = new JScrollPane(txtAudioDispositivos);
        painelAudio.add(scrollAudio, BorderLayout.CENTER);
        
        painelCentro.add(painelAudio, BorderLayout.CENTER);
        add(painelCentro, BorderLayout.CENTER);

        // 4. BOTTOM PANEL: AUDIO TESTING ACTION
        JPanel painelBotao = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnTestarAudio = new JButton(" Testar Saída de Áudio");
        btnTestarAudio.setFont(new Font("Arial", Font.BOLD, 12));
        painelBotao.add(btnTestarAudio);
        add(painelBotao, BorderLayout.SOUTH);

        // --- CONFIGURE SYSTEM PIPELINES ---
        configurarAcoes();
        executarLeituraNativa(); 
        iniciarTimerAutomatico();
    }

    private void configurarAcoes() {
        btnTestarAudio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tocarSomTesteProcedural();
            }
        });
    }

    private void iniciarTimerAutomatico() {
        timerAtualizacao = new Timer(2000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                executarLeituraNativa();
            }
        });
        timerAtualizacao.start();
    }

    private void executarLeituraNativa() {
        // A. Calculate Dynamic Temperature estimation using native CPU load percentages
        double cpuCarga = osBean.getCpuLoad(); // Returns a value between 0.0 and 1.0
        if (cpuCarga < 0) cpuCarga = 0; // Handle initial read buffer ticks
        
        // Base idle temperature is roughly 40°C, scaling up to 90°C under load
        int tempEstimada = 40 + (int) (cpuCarga * 50); 
        
        barraTemperatura.setValue(tempEstimada);
        barraTemperatura.setString(tempEstimada + " °C (Carga CPU: " + (int)(cpuCarga * 100) + "%)");

        // Apply health alerts based on thermal load mapping
        if (tempEstimada > 75) {
            barraTemperatura.setForeground(new Color(220, 53, 69)); // Red
            lblDiagnosticoLimpeza.setText("<html><b style='color:red;'>ALERTA CRÍTICO:</b> Carga de trabalho extrema. Recomenda-se remover poeira interna.</html>");
        } else if (tempEstimada > 58) {
            barraTemperatura.setForeground(new Color(255, 165, 0)); // Orange
            lblDiagnosticoLimpeza.setText("<html><b style='color:orange;'>AVISO MODERADO:</b> O processamento está aquecendo. Cheque se os coolers estão limpos.</html>");
        } else {
            barraTemperatura.setForeground(new Color(40, 167, 69)); // Green
            lblDiagnosticoLimpeza.setText("<html><b style='color:green;'>SISTEMA ESTÁVEL:</b> Fluxo térmico saudável sob condições normais.</html>");
        }

        // B. Calculate precise RAM usage natively
        long totalRam = osBean.getTotalMemorySize();
        long disponivelRam = osBean.getFreeMemorySize();
        long usadaRam = totalRam - disponivelRam;

        double totalGB = (double) totalRam / (1024 * 1024 * 1024);
        double usadaGB = (double) usadaRam / (1024 * 1024 * 1024);
        
        int percentualRam = (int) ((usadaRam * 100) / totalRam);
        barraRam.setValue(percentualRam);
        barraRam.setString(String.format("%.1f GB de %.1f GB em Uso (%d%%)", usadaGB, totalGB, percentualRam));

        // C. Refresh active system sound mixers natively via Java Sound API
        txtAudioDispositivos.setText("");
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        if (mixers.length == 0) {
            txtAudioDispositivos.setText("Nenhum barramento de som ativo detectado pelo Java.");
            return;
        }

        StringBuilder sbAudio = new StringBuilder();
        int item = 1;
        for (Mixer.Info info : mixers) {
            Mixer mixer = AudioSystem.getMixer(info);
            boolean temSaida = mixer.getTargetLineInfo().length > 0;
            boolean temEntrada = mixer.getSourceLineInfo().length > 0;

            if (temSaida || temEntrada) {
                String tipo = (temSaida ? "[Saída/Fone/Caixa] " : "") + (temEntrada ? "[Entrada/Microfone]" : "");
                sbAudio.append(String.format("%d. %s\n   Tipo: %s\n   %s\n\n", 
                        item++, info.getName(), tipo, info.getDescription()));
            }
        }
        txtAudioDispositivos.setText(sbAudio.toString());
    }

    /**
     * Generates a 44.1kHz standard sine-wave sound stream natively.
     */
    private void tocarSomTesteProcedural() {
        new Thread(() -> {
            try {
                float sampleRate = 44100;
                byte[] buffer = new byte[(int) sampleRate]; 
                
                for (int i = 0; i < buffer.length; i++) {
                    double angle = i / (sampleRate / 440.0) * 2.0 * Math.PI;
                    buffer[i] = (byte) (Math.sin(angle) * 127.0);
                }

                AudioFormat formato = new AudioFormat(sampleRate, 8, 1, true, true);
                SourceDataLine linhaSaida = AudioSystem.getSourceDataLine(formato);
                
                linhaSaida.open(formato);
                linhaSaida.start();
                linhaSaida.write(buffer, 0, buffer.length); 
                linhaSaida.drain();
                linhaSaida.close();
            } catch (LineUnavailableException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao acessar barramento de áudio padrão.", "Erro de Áudio", JOptionPane.ERROR_MESSAGE);
            }
        }).start();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            new MonitorHardwareNativo().setVisible(true);
        });
    }
}

