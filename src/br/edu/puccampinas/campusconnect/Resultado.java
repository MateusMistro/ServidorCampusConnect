package br.edu.puccampinas.campusconnect;

import java.io.Serializable;
import java.util.List;

public class Resultado implements Serializable {
    private static final long serialVersionUID = 8556072105244890025L;

    private String mensagem;
    private List<String> erros;

    public Resultado(String mensagem) {
        this.mensagem = mensagem;
    }

    public Resultado(String mensagem, List<String> erros) {
        this.mensagem = mensagem;
        this.erros = erros;
    }

    public String getMensagem() {
        return mensagem;
    }

    public List<String> getErros() {
        return erros;
    }

    @Override
    public String toString() {
        return "Resultado{" +
                "mensagem='" + mensagem + '\'' +
                ", erros=" + erros +
                '}';
    }
}
