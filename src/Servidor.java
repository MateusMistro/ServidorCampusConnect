import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Servidor {
    public static final String PORTA_PADRAO = "4000";

    public static void main(String[] args) {
        if(args.length>1){
            System.err.println("Uso esperado: Java Servidor [PORTA]");
            return;
        }
        String porta = Servidor.PORTA_PADRAO;

        if(args.length==1)
            porta = args[0];

        // Conectar ao MongoDB
        MongoClient mongoClient = MongoClients.create("mongodb+srv://proejtointegrador:123456qwerty@cluster0.zpeb5.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0");
        MongoDatabase database = mongoClient.getDatabase("campusconnect");
        MongoCollection<Document> collection = database.getCollection("establishments");

        ArrayList<Parceiro> usuarios = new ArrayList<Parceiro>();

        AceitadoraDeConexao aceitadoraDeConexao = null;

        try {
            aceitadoraDeConexao = new AceitadoraDeConexao(porta, usuarios,collection);
            aceitadoraDeConexao.start();
        } catch (Exception erro) {
            System.err.println("Escolha uma porta apropriada");
            return;
        }

        for (;;){
            System.out.println("O servidor está ativo! Para desativá-lo,");
            System.out.println("use o comando \"desativar\"\n");
            System.out.print("> ");

            String comando = null;
            try {
                comando = Teclado.getUmString();
            } catch (Exception erro) {}

            if(comando.toLowerCase().equals("desativar")){
                synchronized (usuarios){
                    ComunicadoDeDesligamento comunicadoDeDesligamento = new ComunicadoDeDesligamento();

                    for (Parceiro usuario:usuarios){
                        try {
                            usuario.receba(comunicadoDeDesligamento);
                            usuario.adeus();
                        }
                        catch (Exception erro){}
                    }
                }
                System.out.println("O servidor foi desativado");
                System.exit(0);
            }else
                System.err.println("Comando inválido!\n");
        }
    }
}
