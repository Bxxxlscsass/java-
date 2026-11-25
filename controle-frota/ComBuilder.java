import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

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

    public void mostra() {
        System.out.println("Modelo: " + (modelo != null && !modelo.isBlank() ? modelo : "Não informado"));
        System.out.println("Marca: " + (marca != null && !marca.isBlank() ? marca : "Não informada"));
        System.out.println("Data de Aquisição: " + (dataAquisicao != null ? dataAquisicao.format(FORMATO_BR) : "Não informada"));
        System.out.println("Data de Fabricação: " + (dataFabricacao != null ? dataFabricacao.format(FORMATO_BR) : "Não informada"));
        System.out.println("Tipo: " + (tipo != null && !tipo.isBlank() ? tipo : "Não informado"));
        
        if (produtosAutomotivos.isEmpty()) {
            System.out.println("Produtos Automotivos: Nenhum cadastrado");
        } else {
            System.out.println("Produtos Automotivos:");
            for (ProdutoAutomotivo p : produtosAutomotivos) {
                System.out.println("  - " + p.formatarResumo(MOEDA_BR));
            }
            System.out.println("Valor Total dos Produtos: " + MOEDA_BR.format(calcularTotalProdutos()));
        }
        System.out.println("----------------------------");
    }

    public static class Builder {
        private String modelo;
        private String marca;
        private LocalDate dataAquisicao;
        private LocalDate dataFabricacao;
        private String tipo;
        private final List<ProdutoAutomotivo> produtosAutomotivos = new ArrayList<>();

        public Builder modelo(String modelo) {
            this.modelo = modelo;
            return this;
        }

        public Builder marca(String marca) {
            this.marca = marca;
            return this;
        }

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

        public Builder tipo(String tipo) {
            this.tipo = tipo;
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

public class ComBuilder {
    private static final DateTimeFormatter FORMATO_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat MOEDA_BR = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<Automovel> frota = carregarFrotaInicial();
        int opcao = -1;

        while (opcao != 0) {
            System.out.println("\n=== SISTEMA DE CADASTRO DE AUTOMÓVEIS ===");
            System.out.println("1 - Cadastrar novo automóvel");
            System.out.println("2 - Listar automóveis cadastrados (" + frota.size() + ")");
            System.out.println("3 - Resumo financeiro geral da frota");
            System.out.println("0 - Sair");
            System.out.print("Escolha uma opção: ");

            try {
                opcao = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Opção inválida! Digite apenas números.");
                continue;
            }

            switch (opcao) {
                case 1 -> cadastrarAutomovel(scanner, frota);
                case 2 -> listarAutomoveis(frota);
                case 3 -> exibirResumoFinanceiro(frota);
                case 0 -> System.out.println("Encerrando o sistema...");
                default -> System.out.println("Opção inexistente. Tente novamente.");
            }
        }

        scanner.close();
    }

    private static List<Automovel> carregarFrotaInicial() {
        List<Automovel> lista = new ArrayList<>();

        lista.add(new Automovel.Builder()
            .marca("Toyota")
            .modelo("Corolla")
            .tipo("Sedan")
            .dataFabricacao(15, 2, 2024)
            .dataAquisicao(10, 5, 2024)
            .adicionarProduto("P001", "Película Insulfilm", 450.00)
            .adicionarProduto("P002", "Tapete de Borracha", 189.90)
            .adicionarProduto("P003", "Sensor de Ré", 320.50)
            .build());

        lista.add(new Automovel.Builder()
            .marca("Chevrolet")
            .modelo("Onix")
            .tipo("Hatch")
            .dataFabricacao(10, 6, 2025)
            .dataAquisicao(20, 8, 2025)
            .adicionarProduto("P004", "Kit Multimídia", 1250.00)
            .adicionarProduto("P005", "Alarme Antifurto", 290.00)
            .build());

        lista.add(new Automovel.Builder()
            .marca("Jeep")
            .modelo("Compass")
            .tipo("SUV")
            .dataFabricacao(5, 1, 2023)
            .dataAquisicao(18, 3, 2023)
            .adicionarProduto("P006", "Rack de Teto Transversal", 680.00)
            .build());

        lista.add(new Automovel.Builder()
            .marca("BYD")
            .modelo("Dolphin")
            .tipo("Elétrico")
            .dataFabricacao(12, 9, 2025)
            .dataAquisicao(2, 11, 2025)
            .build());

        return lista;
    }

    private static void cadastrarAutomovel(Scanner scanner, List<Automovel> frota) {
        System.out.println("\n--- Novo Cadastro (pressione ENTER para pular campos opcionais) ---");
        Automovel.Builder builder = new Automovel.Builder();

        System.out.print("Marca: ");
        String marca = scanner.nextLine();
        if (!marca.isBlank()) builder.marca(marca.trim());

        System.out.print("Modelo: ");
        String modelo = scanner.nextLine();
        if (!modelo.isBlank()) builder.modelo(modelo.trim());

        System.out.print("Tipo (ex.: Sedan, Hatch, SUV, Elétrico): ");
        String tipo = scanner.nextLine();
        if (!tipo.isBlank()) builder.tipo(tipo.trim());

        LocalDate dataFab = lerDataOpcional(scanner, "Data de Fabricação (dd/MM/aaaa): ");
        if (dataFab != null) builder.dataFabricacao(dataFab);

        LocalDate dataAqui = lerDataOpcional(scanner, "Data de Aquisição (dd/MM/aaaa): ");
        if (dataAqui != null) builder.dataAquisicao(dataAqui);

        System.out.print("Deseja adicionar produtos automotivos a este veículo? (S/N): ");
        String resposta = scanner.nextLine().trim();

        while (resposta.equalsIgnoreCase("S")) {

        try {
            System.out.print("  Código do produto: ");
            String codigo = scanner.nextLine();

            System.out.print("  Nome do produto: ");
            String nome = scanner.nextLine();

            System.out.print("  Preço (ex.: 199.90 ou 199,90): ");
            String precoTexto = scanner.nextLine().trim().replace(",", ".");
            BigDecimal preco = new BigDecimal(precoTexto);

            builder.adicionarProduto(new ProdutoAutomotivo(codigo, nome, preco));
            System.out.println("  -> Produto adicionado!");
        } catch (NumberFormatException e) {
            System.out.println("  [Erro] Preço inválido. O produto não foi adicionado.");
        } catch (IllegalArgumentException e) {
            System.out.println("  [Erro] " + e.getMessage());
        }
        System.out.print("Deseja adicionar outro produto? (S/N): ");
        resposta = scanner.nextLine().trim();
    }
    
    try {
        Automovel automovel = builder.build();
        frota.add(automovel);
        System.out.println("\nAutomóvel cadastrado com sucesso!");
    } catch (IllegalStateException e) {
        System.out.println("\nFalha ao cadastrar automóvel: " + e.getMessage());
    }
}


private static LocalDate lerDataOpcional(Scanner scanner, String mensagem) {
    while (true) {
        System.out.print(mensagem);
        String entrada = scanner.nextLine().trim();
        if (entrada.isEmpty()) {
            return null;
        }
        
        try {
            return LocalDate.parse(entrada, FORMATO_BR);
        } catch (DateTimeException e) {
            System.out.println("  [Erro] Data inválida! Use o formato dd/MM/aaaa (ex.: 15/03/2024) ou deixe em branco.");
        }
    }
}
private static void listarAutomoveis(List<Automovel> frota) {
    System.out.println("\n=== AUTOMÓVEIS CADASTRADOS ===");
    if (frota.isEmpty()) {
        System.out.println("Nenhum automóvel cadastrado até o momento.");
        return;
    }
    
    for (int i = 0; i < frota.size(); i++) {
        System.out.println("Registro #" + (i + 1));
        frota.get(i).mostra();
    }
}
    private static void exibirResumoFinanceiro(List<Automovel> frota) {
        System.out.println("\n=== RESUMO FINANCEIRO DA FROTA ===");
        if (frota.isEmpty()) {
            System.out.println("Nenhum automóvel cadastrado para calcular.");
            return;
        }
        BigDecimal totalGeral = BigDecimal.ZERO;
        int totalItens = 0;
        
        for (int i = 0; i < frota.size(); i++) {
            Automovel auto = frota.get(i);
            BigDecimal subtotal = auto.calcularTotalProdutos();
            int qtdItens = auto.getProdutosAutomotivos().size();
            
            totalGeral = totalGeral.add(subtotal);
            totalItens += qtdItens;
            
            String identificacao = (auto.getMarca() != null ? auto.getMarca() : "Marca N/I") + " " +
            (auto.getModelo() != null ? auto.getModelo() : "Modelo N/I");
            
            System.out.printf("#%d - %-20s | %d produto(s) | Subtotal: %s%n",
            i + 1, identificacao, qtdItens, MOEDA_BR.format(subtotal));
        }
        
        BigDecimal mediaPorVeiculo = totalGeral.divide(
            BigDecimal.valueOf(frota.size()), 2, RoundingMode.HALF_UP);
            
            System.out.println("------------------------------------------------------------");
            System.out.println("Total de veículos na frota: " + frota.size());
            System.out.println("Total de produtos instalados: " + totalItens);
            System.out.println("Investimento médio por veículo: " + MOEDA_BR.format(mediaPorVeiculo));
            System.out.println("VALOR TOTAL GERAL DA FROTA: " + MOEDA_BR.format(totalGeral));
        }
}

//linha 69 a 75 bigdecimal calcular total produtos é uma soma do total = total + preco dos produtos automotivos 
//percorre a lista de produtos de um único carro e acumula o preco de cada item 
//class comBuilder exibir resumo financeiro soma os subtotais de todos os automoveis cadastrados na lista quantidades em cada casa decimal 
//produtoAutomotivo verifica o preço caso for um valor menor que zero (preco < 0) impedido o cadastro de valores negativos 