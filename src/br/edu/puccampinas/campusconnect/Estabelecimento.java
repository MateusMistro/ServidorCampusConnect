package br.edu.puccampinas.campusconnect;

import java.io.Serializable;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class Estabelecimento implements Serializable {
    private static final long serialVersionUID = 2511790574004449845L;

    private String cnpj;
    private String name;
    private String photo;
    private String description;
    private String openingHours;

    public Estabelecimento(String cnpj, String name,String photo, String description, String openingHours) {
        this.cnpj = cnpj;
        this.name = name;
        this.photo = photo;
        this.description = description;
        this.openingHours = openingHours;
    }

    public String getCnpj() {
        return this.cnpj;
    }

    public String getName(){
        return this.name;
    }

    public String getPhoto(){
        return this.photo;
    }

    public String getDescription() {
        return this.description;
    }

    public String getOpeningHours() {
        return openingHours;
    }

    public boolean isCnpjValid() {
        // Verifica se o CNPJ está no formato "XX.XXX.XXX/XXXX-XX"
        if (!cnpj.matches("\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}")) {
            return false;
        }

        String cnpjLimpo = cnpj.replaceAll("\\D", "");

        // Verifica se o CNPJ tem 14 dígitos
        if (cnpjLimpo.length() != 14) {
            return false;
        }

        // Verifica se todos os dígitos são iguais
        char firstDigit = cnpjLimpo.charAt(0);
        boolean allEqual = true;
        for (int i = 1; i < cnpjLimpo.length(); i++) {
            if (cnpjLimpo.charAt(i) != firstDigit) {
                allEqual = false;
                break;
            }
        }
        if (allEqual) {
            return false;
        }

        // Calcula o primeiro dígito verificador
        int soma = 0;
        int[] pesosPrimeiroDV = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        for (int i = 0; i < 12; i++) {
            soma += Character.getNumericValue(cnpjLimpo.charAt(i)) * pesosPrimeiroDV[i];
        }
        int resto = soma % 11;
        int digitoVerificador1 = (resto < 2) ? 0 : 11 - resto;

        // Verifica o primeiro dígito verificador
        if (Character.getNumericValue(cnpjLimpo.charAt(12)) != digitoVerificador1) {
            return false;
        }

        // Calcula o segundo dígito verificador
        soma = 0;
        int[] pesosSegundoDV = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        for (int i = 0; i < 13; i++) {
            soma += Character.getNumericValue(cnpjLimpo.charAt(i)) * pesosSegundoDV[i];
        }
        resto = soma % 11;
        int digitoVerificador2 = (resto < 2) ? 0 : 11 - resto;

        // Verifica o segundo dígito verificador
        return Character.getNumericValue(cnpjLimpo.charAt(13)) == digitoVerificador2;
    }


    public boolean isDescricaoValid() {
        return this.description != null && this.description.length() <= 100;
    }

    private boolean isOpeningHoursValid() {
        // Verifica se o formato está correto
        if (!openingHours.matches("\\d{2}:\\d{2}-\\d{2}:\\d{2}")) {
            return false; // formato inválido
        }

        String[] horarios = openingHours.split("-");
        try {
            LocalTime abertura = LocalTime.parse(horarios[0], DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime fechamento = LocalTime.parse(horarios[1], DateTimeFormatter.ofPattern("HH:mm"));

            return !fechamento.isBefore(abertura); // horário de fechamento não pode ser antes do horário de abertura
        } catch (DateTimeParseException e) {
            return false; // horário inválido
        }
    }

    public List<String> getValidationErrors() {
        List<String> errors = new ArrayList<>();

        if (!isCnpjValid()) {
            errors.add("CNPJ inválido. Formato esperado: XX.XXX.XXX/XXXX-XX.");
        }
        if (!isDescricaoValid()) {
            errors.add("Descrição inválida. Deve ter no máximo 100 caracteres.");
        }
        if (!isOpeningHoursValid()) {
            errors.add("Horário de funcionamento inválido. Formato esperado: HH:MM-HH:MM.");
        }

        return errors;
    }

    public boolean isValid() {
        return isCnpjValid() && isDescricaoValid() && isOpeningHoursValid();
    }

    @Override
    public String toString() {
        return "br.edu.puccampinas.campusconnect.Estabelecimento{" +
                "cnpj='" + cnpj + '\'' +
                ", name='" + name + '\'' +
                ", photo='" + photo + '\'' +
                ", description='" + description + '\'' +
                ", openingHours='" + openingHours + '\'' +
                '}';
    }
}
