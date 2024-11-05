import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;

public class SupervisoraDeConexao extends Thread {
    private Parceiro usuario;
    private Socket conexao;
    private ArrayList<Parceiro> usuarios;
    private MongoCollection<Document> collection;

    private static final Logger logger = Logger.getLogger(SupervisoraDeConexao.class.getName());

    public SupervisoraDeConexao(Socket conexao, ArrayList<Parceiro> usuarios, MongoCollection<Document> collection) throws Exception {
        if (conexao == null)
            throw new Exception("Conexão ausente");
        if (usuarios == null)
            throw new Exception("Usuários ausentes");
        this.conexao = conexao;
        this.usuarios = usuarios;
        this.collection = collection;
    }

    public void run() {
        ObjectOutputStream transmissor;
        try {
            transmissor = new ObjectOutputStream(this.conexao.getOutputStream());
        } catch (Exception erro) {
            logger.severe("Erro ao abrir transmissor: " + erro.getMessage());
            return;
        }

        ObjectInputStream receptor = null;
        try {
            receptor = new ObjectInputStream(this.conexao.getInputStream());
        } catch (Exception erro) {
            try {
                transmissor.close();
            } catch (Exception falha) {}
            logger.severe("Erro ao abrir receptor: " + erro.getMessage());
            return;
        }

        try {
            this.usuario = new Parceiro(this.conexao, receptor, transmissor);
        } catch (Exception erro) {
            logger.warning("Erro ao criar o parceiro: " + erro.getMessage());
            return;
        }

        try {
            synchronized (this.usuarios) {
                this.usuarios.add(this.usuario);
                logger.info("Novo usuário conectado: " + this.usuario);
            }

            for (;;) {
                Comunicado comunicado = this.usuario.envie();
                if (comunicado == null) {
                    logger.info("Conexão fechada pelo cliente.");
                    return;
                } else {
                    logger.info("Mensagem recebida: " + comunicado.getClass().getSimpleName());

                    if (comunicado instanceof PedidoDeRegistro) {
                        PedidoDeRegistro pedidoDeRegistro = (PedidoDeRegistro) comunicado;
                        String nome = pedidoDeRegistro.getNome();
                        String email = pedidoDeRegistro.getEmail();
                        String senha = pedidoDeRegistro.getSenha();

                        // Criar documento MongoDB para inserir no banco de dados
                        Document usuario = new Document("nome", nome)
                                .append("email", email)
                                .append("senha", senha);

                        // Inserir documento no MongoDB
                        collection.insertOne(usuario);
                        logger.info("Usuário inserido no MongoDB: " + usuario.toJson());

                        // Enviar confirmação de sucesso ao cliente
                        this.usuario.receba(new Resultado("Dados recebidos e inseridos com sucesso!"));

                    } else if (comunicado instanceof PedidoDeLogin) {
                        PedidoDeLogin pedidoDeLogin = (PedidoDeLogin) comunicado;
                        String email = pedidoDeLogin.getEmail();
                        String senha = pedidoDeLogin.getSenha();

                        // Verificar se o email e a senha existem no banco
                        Document usuario = collection.find(new Document("email", email).append("senha", senha)).first();
                        if (usuario != null) {
                            // Login bem-sucedido
                            logger.info("Login bem-sucedido para o email: " + email);
                            this.usuario.receba(new Resultado("Login bem-sucedido. Bem-vindo " + email));
                        } else {
                            // Login falhou
                            logger.warning("Erro de login: email ou senha inválidos para o email: " + email);
                            this.usuario.receba(new Resultado("Erro de login: email ou senha inválidos."));
                        }

                    } else if (comunicado instanceof PedidoParaSair) {
                        synchronized (this.usuarios) {
                            this.usuarios.remove(this.usuario);
                        }
                        logger.info("Usuário desconectado: " + this.usuario);
                        this.usuario.adeus();
                    }
                }
            }
        } catch (Exception erro) {
            logger.severe("Erro durante o processamento: " + erro.getMessage());
            try {
                transmissor.close();
                receptor.close();
            } catch (Exception falha) {
                logger.warning("Erro ao fechar os streams: " + falha.getMessage());
            }
            return;
        }
    }
}
