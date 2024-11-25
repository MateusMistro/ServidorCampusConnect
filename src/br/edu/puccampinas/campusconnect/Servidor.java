package br.edu.puccampinas.campusconnect;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class Servidor {
    public static final String PORTA_PADRAO = "4000";

    public static void main(String[] args) {
        int porta = Integer.parseInt(PORTA_PADRAO);
        try (ServerSocket serverSocket = new ServerSocket(porta)) {
            System.out.println("Servidor iniciado na porta " + porta);

            // Loop para aceitar múltiplas conexões
            while (true) {
                System.out.println("Aguardando conexão...");
                Socket clienteSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clienteSocket.getInetAddress());

                // Processar cliente em uma nova thread
                new Thread(() -> processarCliente(clienteSocket)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void processarCliente(Socket clienteSocket) {
        try (
                // Criação automática de recursos com try-with-resources
                ObjectInputStream inputStream = new ObjectInputStream(clienteSocket.getInputStream());
                ObjectOutputStream outputStream = new ObjectOutputStream(clienteSocket.getOutputStream())
        ) {
            // Lê o objeto enviado pelo cliente
            Object obj = inputStream.readObject();
            if (obj instanceof Estabelecimento) {
                Estabelecimento estabelecimento = (Estabelecimento) obj;
                System.out.println("Recebido do cliente: " + estabelecimento);

                // Valida o estabelecimento
                if (estabelecimento.isValid()) {
                    // Conecta ao MongoDB
                    try (MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb+srv://proejtointegrador:123456qwerty@cluster0.zpeb5.mongodb.net/campusconnect?retryWrites=true&w=majority&appName=Cluster0"))) {
                        MongoDatabase database = mongoClient.getDatabase("campusconnect");
                        MongoCollection<Document> collection = database.getCollection("establishments");

                        // Cria um documento e insere no MongoDB
                        Document doc = new Document("cnpj", estabelecimento.getCnpj())
                                .append("name", estabelecimento.getName())
                                .append("photo", estabelecimento.getPhoto())
                                .append("description", estabelecimento.getDescription())
                                .append("openingHours", estabelecimento.getOpeningHours())
                                .append("ownerId", estabelecimento.getOwnerId());
                        collection.insertOne(doc);

                        // Retorna mensagem de sucesso
                        Resultado resultado = new Resultado("Estabelecimento salvo com sucesso!");
                        outputStream.writeObject(resultado);
                    }
                } else {
                    // Caso não seja válido, envia as mensagens de erro
                    List<String> erros = estabelecimento.getValidationErrors();
                    Resultado resultado = new Resultado("Não válido", erros);
                    outputStream.writeObject(resultado);
                }
                outputStream.flush();
            } else {
                System.err.println("Erro ao processar objeto: " + obj.getClass().getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                clienteSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
