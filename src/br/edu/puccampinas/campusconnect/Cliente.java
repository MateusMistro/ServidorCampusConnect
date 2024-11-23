package br.edu.puccampinas.campusconnect;

public class Cliente {
    public static void main(String[] args) {
        String servidor = "localhost"; // Endereço do servidor (localhost)
        int porta = 4000; // Porta usada pelo servidor

        try (
                // Conecta ao servidor
                java.net.Socket socket = new java.net.Socket(servidor, porta);
                java.io.ObjectOutputStream outputStream = new java.io.ObjectOutputStream(socket.getOutputStream());
                java.io.ObjectInputStream inputStream = new java.io.ObjectInputStream(socket.getInputStream())
        ) {
            System.out.println("Conectado ao servidor!");

            // Captura os dados para o construtor do objeto Estabelecimento
            System.out.println("Digite as informações do estabelecimento:");

            System.out.print("CNPJ: ");
            String cnpj = Teclado.getUmString();

            System.out.print("Nome: ");
            String name = Teclado.getUmString();

            System.out.print("Foto (URL): ");
            String photo = Teclado.getUmString();

            System.out.print("Descrição (máximo 100 caracteres): ");
            String description = Teclado.getUmString();

            System.out.print("Horário de funcionamento (ex: 08:00-18:00): ");
            String openingHours = Teclado.getUmString();

            // Cria o objeto Estabelecimento utilizando o construtor
            Estabelecimento estabelecimento = new Estabelecimento(cnpj, name, photo, description, openingHours);
            System.out.println("Estabelecimento criado: " + estabelecimento);

            // Envia o objeto para o servidor
            outputStream.writeObject(estabelecimento);
            outputStream.flush();
            System.out.println("Estabelecimento enviado!");

            // Recebe a resposta do servidor
            Object resposta = inputStream.readObject();
            if (resposta instanceof Resultado) {
                Resultado resultado = (Resultado) resposta;
                System.out.println("Resposta do servidor: " + resultado.getMensagem());
                if (resultado.getErros() != null && !resultado.getErros().isEmpty()) {
                    System.out.println("Erros de validação:");
                    for (String erro : resultado.getErros()) {
                        System.out.println("- " + erro);
                    }
                }
            } else {
                System.err.println("Resposta inesperada do servidor: " + resposta);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
