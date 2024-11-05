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

        // Coleta de dados para validação do estabelecimento
        String cnpj = "";
        String name = "";
        String photo = "";
        String description = "";
        String openingHours = "";

        try {
            // Coleta dos dados do estabelecimento
            System.out.print("Digite o CNPJ (formato XX.XXX.XXX/XXXX-XX): ");
            cnpj = Teclado.getUmString();

            System.out.print("Digite o nome: ");
            name = Teclado.getUmString();

            System.out.print("Digite a URL da foto: ");
            photo = Teclado.getUmString();

            System.out.print("Digite a descrição (até 100 caracteres): ");
            description = Teclado.getUmString();

            System.out.print("Digite o horário de funcionamento (formato HH:MM-HH:MM): ");
            openingHours = Teclado.getUmString();

            // Envia o pedido de validação de estabelecimento para o servidor
            servidor.receba(new PedidoDeValidacaoDeEstabelecimento(cnpj, name, photo, description, openingHours));

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
