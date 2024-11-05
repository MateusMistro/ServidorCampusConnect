import java.io.Serializable;

public class PedidoDeLogin extends Comunicado{
    private String email;
    private String senha;

    public PedidoDeLogin(String email, String senha) {
        this.email = email;
        this.senha = senha;
    }

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }
}
