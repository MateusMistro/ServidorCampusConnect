import java.net.*;
import java.io.*;

public class Cliente {
    public static final String HOST_PADRAO = "localhost";
    public static final int PORTA_PADRAO = 4000;

    public static void main(String[] args) {
        // Verifica se os argumentos estão corretos (host e porta)
        if (args.length > 2) {
            System.err.println("Uso esperado: java Cliente [HOST [PORTA]]");
            return;
        }

        // Declaração do socket, transmissores e receptores
        Socket conexao = null;
        try {
            String host = HOST_PADRAO;
            int porta = PORTA_PADRAO;

            if (args.length > 0) host = args[0];
            if (args.length == 2) porta = Integer.parseInt(args[1]);

            // Conecta ao servidor
            conexao = new Socket(host, porta);
        } catch (Exception erro) {
            System.err.println("Indique o servidor e a porta corretos!\n");
            return;
        }

        // Criação dos streams para comunicação
        ObjectOutputStream transmissor = null;
        try {
            transmissor = new ObjectOutputStream(conexao.getOutputStream());
        } catch (Exception erro) {
            System.err.println("Erro ao obter o transmissor de objetos.\n");
            return;
        }

        ObjectInputStream receptor = null;
        try {
            receptor = new ObjectInputStream(conexao.getInputStream());
        } catch (Exception erro) {
            System.err.println("Erro ao obter o receptor de objetos.\n");
            return;
        }

        // Inicialização da classe Parceiro
        Parceiro servidor = null;
        try {
            servidor = new Parceiro(conexao, receptor, transmissor);
        } catch (Exception erro) {
            System.err.println("Erro ao inicializar o parceiro.\n");
            return;
        }

        // Coleta de dados do usuário
        String nome = "";
        String email = "";
        String senha = "";
        String opcao = "";

        try {
            System.out.print("Escolha uma opção (1 - Registrar, 2 - Login): ");
            opcao = Teclado.getUmString();

            if (opcao.equals("1")) {
                // Registro de novos usuários
                System.out.print("Digite seu nome: ");
                nome = Teclado.getUmString();

                System.out.print("Digite seu email: ");
                email = Teclado.getUmString();

                System.out.print("Digite sua senha: ");
                senha = Teclado.getUmString();

                // Envia os dados do usuário para o servidor
                servidor.receba(new PedidoDeRegistro(nome, email, senha));
            } else if (opcao.equals("2")) {
                // Login de usuários já registrados
                System.out.print("Digite seu email: ");
                email = Teclado.getUmString();

                System.out.print("Digite sua senha: ");
                senha = Teclado.getUmString();

                // Envia os dados para o servidor verificar
                servidor.receba(new PedidoDeLogin(email, senha));
            } else {
                System.out.println("Opção inválida.");
                return;
            }

        } catch (IOException erro) {
            System.err.println("Erro de entrada de dados.\n");
            return;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        // Recebe a resposta do servidor
        try {
            Comunicado respostaComunicado = servidor.envie();


            if (respostaComunicado instanceof Resultado) {
                Resultado resultado = (Resultado) respostaComunicado;
                System.out.println("Resposta do servidor: " + resultado.getValorResultante());
            } else {
                System.err.println("Resposta inesperada do servidor.");
            }

        } catch (Exception erro) {
            System.err.println("Erro de comunicação com o servidor.\n");
            erro.printStackTrace();
        } finally {
            try {
                // Envia uma mensagem para o servidor informando que o cliente está saindo
                servidor.receba(new PedidoParaSair());
            } catch (Exception e) {
                System.err.println("Erro ao encerrar conexão com o servidor.\n");
            }
        }
    }
}
