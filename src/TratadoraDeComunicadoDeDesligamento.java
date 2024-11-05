public class TratadoraDeComunicadoDeDesligamento extends Thread{
    private Parceiro servidor;

    public TratadoraDeComunicadoDeDesligamento(Parceiro s){
        this.servidor = s;
    }
}
