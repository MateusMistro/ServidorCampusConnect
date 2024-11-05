import com.mongodb.client.MongoCollection;

import org.bson.Document;
import java.io.IOException;
import java.net.*;
import java.util.*;

public class AceitadoraDeConexao extends Thread{
    private ServerSocket pedido;
    private ArrayList<Parceiro> usuarios;
    private MongoCollection<Document> collection;

    public AceitadoraDeConexao(String porta, ArrayList<Parceiro> usuarios, MongoCollection<Document> collection)throws Exception{
        if(porta == null)
            throw new Exception("Porta ausente");
        try {
            this.pedido = new ServerSocket(Integer.parseInt(porta));
        } catch (Exception erro) {
            throw new Exception("Porta inválida");
        }
        if(usuarios==null)
            throw new Exception("Usuários ausentes");
        this.usuarios = usuarios;
        this.collection = collection;
    }

    public void run(){
        for (;;){
            Socket conexao = null;
            try {
                conexao = this.pedido.accept();
            }
            catch (Exception erro){
                continue;
            }

            SupervisoraDeConexao supervisoraDeConexao = null;
            try {
                supervisoraDeConexao = new SupervisoraDeConexao(conexao,usuarios,collection);
            } catch (Exception erro) {}

            supervisoraDeConexao.start();
        }
    }
}
