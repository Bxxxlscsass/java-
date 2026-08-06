import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

// --- MODELO DO PRODUTO ---
class Produto {
    private int id;
    private String nome;
    private double preco;

    public Produto(int id, String nome, double preco) {
        this.id = id;
        this.nome = nome;
        this.preco = preco;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public double getPreco() { return preco; }

    @Override
    public String toString() {
        return String.format("ID: %d | Nome: %-15s | Preço: R$ %.2f", id, nome, preco);
    }
}

// --- SISTEMA PRINCIPAL ---
public class SistemaEstoque {
    private static ArrayList<Produto> estoque = new ArrayList<>();
    private static Scanner scanner = new Scanner(System.in);
    private static int contadorId = 1;

    public static void main(String[] args) {
        int opcao = -1;

        while (opcao != 0) {
            exibirMenu();
            opcao = lerInteiroValido("Escolha uma opção: ");

            switch (opcao) {
                case 1 -> adicionarProduto();
                case 2 -> removerProduto();
                case 3 -> listarProdutos();
                case 0 -> System.out.println("\n--- Saída: Sistema encerrado! ---");
                default -> System.out.println("\n[Erro] Opção inválida! Tente novamente.");
            }
        }
    }

    private static void exibirMenu() {
        System.out.println("\n=== SISTEMA DE ESTOQUE ===");
        System.out.println("1. Adicionar Produto");
        System.out.println("2. Remover Produto");
        System.out.println("3. Listar Produtos");
        System.out.println("0. Sair");
    }

    // --- OPERAÇÕES ---

    private static void adicionarProduto() {
        System.out.println("\n--- Entrada: Adicionar Produto ---");
        
        System.out.print("Digite o nome do produto: ");
        String nome = scanner.nextLine().trim();
        
        // Validação de String vazia
        while (nome.isEmpty()) {
            System.out.print("[Erro] O nome não pode ser vazio. Digite novamente: ");
            nome = scanner.nextLine().trim();
        }

        // Validação de número decimal positivo
        double preco = lerDoubleValido("Digite o preço do produto (ex: 29,90): ");
        while (preco <= 0) {
            System.out.println("[Erro] O preço deve ser maior que zero.");
            preco = lerDoubleValido("Digite um preço válido: ");
        }

        Produto novoProduto = new Produto(contadorId++, nome, preco);
        estoque.add(novoProduto);
        System.out.println("\n--- Saída: Produto adicionado com sucesso! ---");
    }

    private static void removerProduto() {
        System.out.println("\n--- Entrada: Remover Produto ---");
        if (estoque.isEmpty()) {
            System.out.println("--- Saída: O estoque está vazio. Nada para remover. ---");
            return;
        }

        listarProdutos();
        int idAlvo = lerInteiroValido("Digite o ID do produto que deseja remover: ");

        // Lógica de remoção com validação de existência
        boolean removido = estoque.removeIf(p -> p.getId() == idAlvo);

        if (removido) {
            System.out.println("\n--- Saída: Produto removido com sucesso! ---");
        } else {
            System.out.println("\n--- Saída: [Erro] ID não encontrado no sistema. ---");
        }
    }

    private static void listarProdutos() {
        System.out.println("\n--- Saída: Lista de Produtos ---");
        if (estoque.isEmpty()) {
            System.out.println("Nenhum produto cadastrado.");
            return;
        }
        for (Produto p : estoque) {
            System.out.println(p);
        }
    }

    // --- FUNÇÕES DE VALIDAÇÃO DE ENTRADA (TRY-CATCH) ---

    private static int lerInteiroValido(String mensagem) {
        while (true) {
            try {
                System.out.print(mensagem);
                int numero = scanner.nextInt();
                scanner.nextLine(); // Limpa o buffer do teclado
                return numero;
            } catch (InputMismatchException e) {
                System.out.println("[Erro] Entrada inválida! Digite apenas números inteiros.");
                scanner.nextLine(); // Limpa o buffer após o erro
            }
        }
    }

    private static double lerDoubleValido(String mensagem) {
        while (true) {
            try {
                System.out.print(mensagem);
                double numero = scanner.nextDouble();
                scanner.nextLine(); // Limpa o buffer
                return numero;
            } catch (InputMismatchException e) {
                System.out.println("[Erro] Entrada inválida! Digite um número decimal (use vírgula ou ponto dependendo do seu sistema).");
                scanner.nextLine(); // Limpa o buffer
            }
        }
    }
}
