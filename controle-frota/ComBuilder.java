import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class ProdutoAutomotivo {
    private final String codigo;
    private final String nome;
    private final BigDecimal preco;

    public ProdutoAutomotivo(String codigo, String nome, BigDecimal preco) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("O código do produto é obrigatório.");
        }
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do produto é obrigatório.");
        }
        if (preco == null || preco.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O preço do produto não pode ser nulo ou negativo.");
        }

        this.codigo = codigo.trim();
        this.nome = nome.trim();
        this.preco = preco;
    }

    public ProdutoAutomotivo(String codigo, String nome, double preco) {
        this(codigo, nome, BigDecimal.valueOf(preco));
    }

    public String getCodigo() { return codigo; }
    public String getNome() { return nome; }
    public BigDecimal getPreco() { return preco; }

    public String formatarResumo(NumberFormat moeda) {
        return String.format("[%s] %s - %s", codigo, nome, moeda.format(preco));
    }
}

class Automovel {
    private static final DateTimeFormatter FORMATO_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat MOEDA_BR = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));

    private final String modelo;
    private final String marca;
    private final LocalDate dataAquisicao;
    private final LocalDate dataFabricacao;
    private final String tipo;
    private final List<ProdutoAutomotivo> produtosAutomotivos;

    private Automovel(Builder builder) {
        this.modelo = builder.modelo;
        this.marca = builder.marca;
        this.dataAquisicao = builder.dataAquisicao;
        this.dataFabricacao = builder.dataFabricacao;
        this.tipo = builder.tipo;
        this.produtosAutomotivos = List.copyOf(builder.produtosAutomotivos);
    }

    public String getModelo() { return modelo; }
    public String getMarca() { return marca; }
    public LocalDate getDataAquisicao() { return dataAquisicao; }
    public LocalDate getDataFabricacao() { return dataFabricacao; }
    public String getTipo() { return tipo; }
    public List<ProdutoAutomotivo> getProdutosAutomotivos() { return produtosAutomotivos; }

    public BigDecimal calcularTotalProdutos() {
        BigDecimal total = BigDecimal.ZERO;
        for (ProdutoAutomotivo produto : produtosAutomotivos) {
            total = total.add(produto.getPreco());
        }
        return total;
    }

    public String formatarDetalhes() {
        StringBuilder sb = new StringBuilder();
        sb.append("Modelo: ").append(modelo != null && !modelo.isBlank() ? modelo : "Não informado").append("\n");
        sb.append("Marca: ").append(marca != null && !marca.isBlank() ? marca : "Não informada").append("\n");
        sb.append("Data de Aquisição: ").append(dataAquisicao != null ? dataAquisicao.format(FORMATO_BR) : "Não informada").append("\n");
        sb.append("Data de Fabricação: ").append(dataFabricacao != null ? dataFabricacao.format(FORMATO_BR) : "Não informada").append("\n");
        sb.append("Tipo: ").append(tipo != null && !tipo.isBlank() ? tipo : "Não informado").append("\n");

        if (produtosAutomotivos.isEmpty()) {
            sb.append("Produtos Automotivos: Nenhum cadastrado\n");
        } else {
            sb.append("Produtos Automotivos:\n");
            for (ProdutoAutomotivo p : produtosAutomotivos) {
                sb.append("  - ").append(p.formatarResumo(MOEDA_BR)).append("\n");
            }
            sb.append("Valor Total dos Produtos: ").append(MOEDA_BR.format(calcularTotalProdutos())).append("\n");
        }
        sb.append("----------------------------");
        return sb.toString();
    }

    public static class Builder {
        private String modelo;
        private String marca;
        private LocalDate dataAquisicao;
        private LocalDate dataFabricacao;
        private String tipo;
        private final List<ProdutoAutomotivo> produtosAutomotivos = new ArrayList<>();

        public Builder modelo(String modelo) { this.modelo = modelo; return this; }
        public Builder marca(String marca) { this.marca = marca; return this; }
        public Builder tipo(String tipo) { this.tipo = tipo; return this; }

        public Builder dataAquisicao(int dia, int mes, int ano) {
            this.dataAquisicao = LocalDate.of(ano, mes, dia);
            return this;
        }
        public Builder dataAquisicao(LocalDate dataAquisicao) {
            this.dataAquisicao = dataAquisicao;
            return this;
        }

        public Builder dataFabricacao(int dia, int mes, int ano) {
            this.dataFabricacao = LocalDate.of(ano, mes, dia);
            return this;
        }
        public Builder dataFabricacao(LocalDate dataFabricacao) {
            this.dataFabricacao = dataFabricacao;
            return this;
        }

        public Builder adicionarProduto(String codigo, String nome, double preco) {
            this.produtosAutomotivos.add(new ProdutoAutomotivo(codigo, nome, preco));
            return this;
        }

        public Builder adicionarProduto(ProdutoAutomotivo produto) {
            if (produto != null) {
                this.produtosAutomotivos.add(produto);
            }
            return this;
        }

        public Automovel build() {
            if (dataAquisicao != null && dataFabricacao != null) {
                if (dataAquisicao.isBefore(dataFabricacao)) {
                    throw new IllegalStateException(
                        "Erro de validação: a data de aquisição (" + dataAquisicao.format(FORMATO_BR) +
                        ") não pode ser anterior à data de fabricação (" + dataFabricacao.format(FORMATO_BR) + ")."
                    );
                }
            }
            return new Automovel(this);
        }
    }
}

public class ComBuilder extends JFrame {
    private static final DateTimeFormatter FORMATO_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm:ss");
    private static final NumberFormat MOEDA_BR = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));

    private final List<Automovel> frota;
    private final List<ProdutoAutomotivo> produtosEmCadastro = new ArrayList<>();

    // Campos do formulário do veículo
    private JTextField txtMarca;
    private JTextField txtModelo;
    private JTextField txtTipo;
    private JTextField txtDataFab;
    private JTextField txtDataAqui;

    // Campos do formulário de produto
    private JTextField txtProdCodigo;
    private JTextField txtProdNome;
    private JTextField txtProdPreco;
    private DefaultListModel<String> modeloListaProdutosTemp;

    // Tabela principal e rótulos de resumo
    private DefaultTableModel modeloTabelaFrota;
    private JTable tabelaFrota;
    private JLabel lblTotalVeiculos;
    private JLabel lblTotalProdutos;
    private JLabel lblMediaVeiculo;
    private JLabel lblTotalGeral;

    public ComBuilder() {
        this.frota = carregarFrotaInicial();
        configurarJanela();
        atualizarTabelaEResumo();
    }

    private void configurarJanela() {
        setTitle("Controle de Frota e Produtos Automotivos (Padrão Builder)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1080, 640);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel painelPrincipal = new JPanel(new BorderLayout(10, 10));
        painelPrincipal.setBorder(new EmptyBorder(10, 10, 10, 10));

        painelPrincipal.add(criarPainelFormulario(), BorderLayout.WEST);
        painelPrincipal.add(criarPainelTabela(), BorderLayout.CENTER);
        painelPrincipal.add(criarPainelRodape(), BorderLayout.SOUTH);

        add(painelPrincipal);
    }

    private JPanel criarPainelFormulario() {
        JPanel painelEsquerdo = new JPanel(new BorderLayout(8, 8));
        painelEsquerdo.setPreferredSize(new Dimension(370, 0));
        painelEsquerdo.setBorder(new TitledBorder("Novo Cadastro de Automóvel"));

        // Grade com os campos do veículo
        JPanel gradeVeiculo = new JPanel(new GridLayout(5, 2, 6, 6));
        gradeVeiculo.setBorder(new EmptyBorder(5, 5, 5, 5));

        txtMarca = new JTextField();
        txtModelo = new JTextField();
        txtTipo = new JTextField();
        txtDataFab = new JTextField();
        txtDataAqui = new JTextField();

        gradeVeiculo.add(new JLabel("Marca:"));
        gradeVeiculo.add(txtMarca);
        gradeVeiculo.add(new JLabel("Modelo:"));
        gradeVeiculo.add(txtModelo);
        gradeVeiculo.add(new JLabel("Tipo (Sedan/SUV...):"));
        gradeVeiculo.add(txtTipo);
        gradeVeiculo.add(new JLabel("Fabricação (dd/MM/aaaa):"));
        gradeVeiculo.add(txtDataFab);
        gradeVeiculo.add(new JLabel("Aquisição (dd/MM/aaaa):"));
        gradeVeiculo.add(txtDataAqui);

        // Subpainel de produtos para o veículo atual
        JPanel painelProdutos = new JPanel(new BorderLayout(5, 5));
        painelProdutos.setBorder(new TitledBorder("Produtos do Veículo Atual"));

        JPanel gradeProduto = new JPanel(new GridLayout(4, 2, 5, 5));
        txtProdCodigo = new JTextField();
        txtProdNome = new JTextField();
        txtProdPreco = new JTextField();
        
        JButton btnAddProd = new JButton("+ Adicionar Produto");
        gradeProduto.add(new JLabel("Código:"));
        gradeProduto.add(txtProdCodigo);
        gradeProduto.add(new JLabel("Nome:"));
        gradeProduto.add(txtProdNome);
        gradeProduto.add(new JLabel("Preço (R$):"));
        gradeProduto.add(txtProdPreco);
        gradeProduto.add(new JLabel(""));
        gradeProduto.add(btnAddProd);

        modeloListaProdutosTemp = new DefaultListModel<>();
        JList<String> listaProdutosTemp = new JList<>(modeloListaProdutosTemp);
        JScrollPane scrollListaTemp = new JScrollPane(listaProdutosTemp);
        
        btnAddProd.addActionListener(e -> adicionarProdutoTemporario());
        
        painelProdutos.add(gradeProduto, BorderLayout.NORTH);
        painelProdutos.add(scrollListaTemp, BorderLayout.CENTER);

        // Botões de ação do formulário
        JPanel painelBotoesForm = new JPanel(new GridLayout(1, 2, 8, 0));
        JButton btnLimpar = new JButton("Limpar Campos");
        JButton btnSalvar = new JButton("Salvar Automóvel");
        btnSalvar.setFont(btnSalvar.getFont().deriveFont(Font.BOLD));

        btnLimpar.addActionListener(e -> limparFormulario());
        btnSalvar.addActionListener(e -> salvarAutomovel());

        painelBotoesForm.add(btnLimpar);
        painelBotoesForm.add(btnSalvar);

        painelEsquerdo.add(gradeVeiculo, BorderLayout.NORTH);
        painelEsquerdo.add(painelProdutos, BorderLayout.CENTER);
        painelEsquerdo.add(painelBotoesForm, BorderLayout.SOUTH);
        return painelEsquerdo;
    }
    
    private JPanel criarPainelTabela() {
        JPanel painelCentro = new JPanel(new BorderLayout(8, 8));
        painelCentro.setBorder(new TitledBorder("Frota Cadastrada"));

        String[] colunas = {"#", "Marca", "Modelo", "Tipo", "Fabricação", "Aquisição", "Qtd. Prod.", "Total Produtos"};
        modeloTabelaFrota = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Impede edição direta na célula
            }
        };
        
        tabelaFrota = new JTable(modeloTabelaFrota);
        tabelaFrota.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaFrota.setRowHeight(24);
        tabelaFrota.getColumnModel().getColumn(0).setMaxWidth(40);
        
        JScrollPane scrollTabela = new JScrollPane(tabelaFrota);
        
        // Barra de ações sobre o item selecionado na tabela
        JPanel barraAcoesTabela = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnDetalhes = new JButton("Ver Detalhes / Produtos");
        JButton btnRemover = new JButton("Remover Selecionado");
        
        btnDetalhes.addActionListener(e -> verDetalhesSelecionado());
        btnRemover.addActionListener(e -> removerSelecionado());
        
        barraAcoesTabela.add(btnDetalhes);
        barraAcoesTabela.add(btnRemover);
        
        painelCentro.add(scrollTabela, BorderLayout.CENTER);
        painelCentro.add(barraAcoesTabela, BorderLayout.SOUTH);
        return painelCentro;
    }
    private JPanel criarPainelRodape() {
        JPanel painelRodape = new JPanel(new BorderLayout(10, 10));
        painelRodape.setBorder(new TitledBorder("Resumo Financeiro Geral da Frota"));
        
        JPanel painelMetricas = new JPanel(new GridLayout(1, 4, 15, 5));
        painelMetricas.setBorder(new EmptyBorder(5, 10, 5, 10));
        
        lblTotalVeiculos = new JLabel("Veículos: 0");
        lblTotalProdutos = new JLabel("Produtos: 0");
        lblMediaVeiculo = new JLabel("Média/Veículo: R$ 0,00");
        lblTotalGeral = new JLabel("TOTAL GERAL: R$ 0,00");
        lblTotalGeral.setFont(lblTotalGeral.getFont().deriveFont(Font.BOLD, 13f));

        painelMetricas.add(lblTotalVeiculos);
        painelMetricas.add(lblTotalProdutos);
        painelMetricas.add(lblMediaVeiculo);
        painelMetricas.add(lblTotalGeral);
        
        JButton btnExportar = new JButton("Exportar Relatório (.TXT)");
        btnExportar.addActionListener(e -> exportarRelatorioTxt());
        
        painelRodape.add(painelMetricas, BorderLayout.CENTER);
        painelRodape.add(btnExportar, BorderLayout.EAST);
        return painelRodape;
    }
    private void adicionarProdutoTemporario() {
        try {
            String codigo = txtProdCodigo.getText();
            String nome = txtProdNome.getText();
            String precoTexto = txtProdPreco.getText().trim().replace(",", ".");
            
            if (precoTexto.isEmpty()) {
                throw new IllegalArgumentException("Informe o preço do produto.");
            }
            BigDecimal preco = new BigDecimal(precoTexto);
            ProdutoAutomotivo produto = new ProdutoAutomotivo(codigo, nome, preco);
            
            produtosEmCadastro.add(produto);
            modeloListaProdutosTemp.addElement(produto.formatarResumo(MOEDA_BR));
            
            txtProdCodigo.setText("");
            txtProdNome.setText("");
            txtProdPreco.setText("");
            txtProdCodigo.requestFocus();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Preço inválido! Use apenas números (ex.: 199,90).", "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro de Validação", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void salvarAutomovel() {
        try {
            Automovel.Builder builder = new Automovel.Builder();
            
            if (!txtMarca.getText().isBlank()) builder.marca(txtMarca.getText().trim());
            if (!txtModelo.getText().isBlank()) builder.modelo(txtModelo.getText().trim());
            if (!txtTipo.getText().isBlank()) builder.tipo(txtTipo.getText().trim());
            
            if (!txtDataFab.getText().isBlank()) {
                builder.dataFabricacao(LocalDate.parse(txtDataFab.getText().trim(), FORMATO_BR));
            }

            if (!txtDataAqui.getText().isBlank()) {
                builder.dataAquisicao(LocalDate.parse(txtDataAqui.getText().trim(), FORMATO_BR));
            }

            for (ProdutoAutomotivo p : produtosEmCadastro) {
                builder.adicionarProduto(p);
            }
            
            Automovel novo = builder.build();
            frota.add(novo);
            
            limparFormulario();
            atualizarTabelaEResumo();
            JOptionPane.showMessageDialog(this, "Automóvel cadastrado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        } catch (DateTimeException ex) {
            JOptionPane.showMessageDialog(this, "Data inválida! Use o formato dd/MM/aaaa (ex.: 15/03/2024).", "Erro de Data", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Regra de Negócio", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void limparFormulario() {
        txtMarca.setText("");
        txtModelo.setText("");
        txtTipo.setText("");
        txtDataFab.setText("");
        txtDataAqui.setText("");
        txtProdCodigo.setText("");
        txtProdNome.setText("");
        txtProdPreco.setText("");
        produtosEmCadastro.clear();
        modeloListaProdutosTemp.clear();
    }
    
    private void verDetalhesSelecionado() {
        int linha = tabelaFrota.getSelectedRow();
        if (linha == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um veículo na tabela primeiro.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Automovel selecionado = frota.get(linha);
        JTextArea area = new JTextArea(selecionado.formatarDetalhes());
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(450, 260));
        JOptionPane.showMessageDialog(this, scroll, "Detalhes do Registro #" + (linha + 1), 
        JOptionPane.PLAIN_MESSAGE);
    }
    private void removerSelecionado() {
        int linha = tabelaFrota.getSelectedRow();
        if (linha == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um veículo na tabela para remover.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirma = JOptionPane.showConfirmDialog(this, "Deseja remover o registro #" + (linha + 1) + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirma == JOptionPane.YES_OPTION) {
            frota.remove(linha);
            atualizarTabelaEResumo();
        }
    }
    private void atualizarTabelaEResumo() {
        modeloTabelaFrota.setRowCount(0);
        
        BigDecimal totalGeral = BigDecimal.ZERO;
        int totalItens = 0;

        for (int i = 0; i < frota.size(); i++) {
            Automovel a = frota.get(i);
            BigDecimal subtotal = a.calcularTotalProdutos();
            int qtdItens = a.getProdutosAutomotivos().size();
            
            totalGeral = totalGeral.add(subtotal);
            totalItens += qtdItens;

            modeloTabelaFrota.addRow(new Object[]{
                i + 1,
                a.getMarca() != null ? a.getMarca() : "N/I",
                a.getModelo() != null ? a.getModelo() : "N/I",
                a.getTipo() != null ? a.getTipo() : "N/I",
                a.getDataFabricacao() != null ? a.getDataFabricacao().format(FORMATO_BR) : "N/I",
                a.getDataAquisicao() != null ? a.getDataAquisicao().format(FORMATO_BR) : "N/I",
                qtdItens,
                MOEDA_BR.format(subtotal)
            });
        }
        
        BigDecimal media = frota.isEmpty()
        ? BigDecimal.ZERO
        : totalGeral.divide(BigDecimal.valueOf(frota.size()), 2, RoundingMode.HALF_UP);
        
        lblTotalVeiculos.setText("Veículos: " + frota.size());
        lblTotalProdutos.setText("Produtos: " + totalItens);
        lblMediaVeiculo.setText("Média/Veículo: " + MOEDA_BR.format(media));
        lblTotalGeral.setText("TOTAL GERAL: " + MOEDA_BR.format(totalGeral));
    }
    
    private void exportarRelatorioTxt() {
        if (frota.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhum automóvel cadastrado para exportar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser seletor = new JFileChooser();
        seletor.setSelectedFile(new java.io.File("relatorio_frota.txt"));
        
        if (seletor.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            Path caminho = seletor.getSelectedFile().toPath();
            StringBuilder sb = new StringBuilder();
            sb.append("RELATÓRIO DE CONTROLE DE FROTA E PRODUTOS AUTOMOTIVOS\n");
            sb.append("Gerado em: ").append(LocalDateTime.now().format(FORMATO_DATA_HORA)).append("\n");
            sb.append("============================================================\n\n");
            for (int i = 0; i < frota.size(); i++) {
                
                sb.append("Registro #").append(i + 1).append("\n");
                sb.append(frota.get(i).formatarDetalhes()).append("\n");
            }
            sb.append("\n=== RESUMO FINANCEIRO GERAL ===\n");
            sb.append(lblTotalVeiculos.getText()).append(" | ")
            .append(lblTotalProdutos.getText()).append(" | ")
            .append(lblMediaVeiculo.getText()).append(" | ")
            .append(lblTotalGeral.getText()).append("\n");
            
            try {
                Files.writeString(caminho, sb.toString(), StandardCharsets.UTF_8);
                JOptionPane.showMessageDialog(this, "Relatório salvo com sucesso em:\n" + caminho.toAbsolutePath(), "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao salvar arquivo: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    private static List<Automovel> carregarFrotaInicial() {
        List<Automovel> lista = new ArrayList<>();
        lista.add(new Automovel.Builder()
        .marca("Toyota").modelo("Corolla").tipo("Sedan")
        .dataFabricacao(15, 2, 2024).dataAquisicao(10, 5, 2024)
        .adicionarProduto("P001", "Película Insulfilm", 450.00)
        .adicionarProduto("P002", "Tapete de Borracha", 189.90)
        .adicionarProduto("P003", "Sensor de Ré", 320.50)
        .build());

        lista.add(new Automovel.Builder()
        .marca("Chevrolet").modelo("Onix").tipo("Hatch")
        .dataFabricacao(10, 6, 2025).dataAquisicao(20, 8, 2025)
        .adicionarProduto("P004", "Kit Multimídia", 1250.00)
        .adicionarProduto("P005", "Alarme Antifurto", 290.00)
        .build());
        
        lista.add(new Automovel.Builder()
        .marca("Jeep").modelo("Compass").tipo("SUV")
        .dataFabricacao(5, 1, 2023).dataAquisicao(18, 3, 2023)
        .adicionarProduto("P006", "Rack de Teto Transversal", 680.00)
        .build());

        lista.add(new Automovel.Builder()
        .marca("BYD").modelo("Dolphin").tipo("Elétrico")
        .dataFabricacao(12, 9, 2025).dataAquisicao(2, 11, 2025)
        .build());

        return lista;
    }
    public static void main(String[] args) {
// Aplica o visual nativo do sistema operacional (Windows/Linux/macOS)
try {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
} catch (Exception ignored) {}
SwingUtilities.invokeLater(() -> new ComBuilder().setVisible(true));
}
}
