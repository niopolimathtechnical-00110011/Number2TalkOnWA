package com.niotech.number2talk_on_wa;

import android.content.Context;
import android.os.Environment;
import android.widget.EditText;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TelefoneHelper {
    
    public static String formatarNumeroBrasil(String numero) {
        if (numero == null || numero.isEmpty()) return "";
        
        // Remove tudo que não é número
        String apenasNumeros = numero.replaceAll("[^0-9]", "");
        
        if (apenasNumeros.length() == 10) {
            // Formato: (XX) XXXX-XXXX (fixo)
            return String.format("(%s) %s-%s",
                apenasNumeros.substring(0, 2),
                apenasNumeros.substring(2, 6),
                apenasNumeros.substring(6, 10));
        } else if (apenasNumeros.length() == 11) {
            // Formato: (XX) 9 XXXX-XXXX (celular)
            if (apenasNumeros.charAt(2) == '9') {
                return String.format("(%s) %s %s-%s",
                    apenasNumeros.substring(0, 2),
                    apenasNumeros.charAt(2),
                    apenasNumeros.substring(3, 7),
                    apenasNumeros.substring(7, 11));
            } else {
                return String.format("(%s) %s-%s",
                    apenasNumeros.substring(0, 2),
                    apenasNumeros.substring(2, 6),
                    apenasNumeros.substring(6, 11));
            }
        } else if (apenasNumeros.length() == 13 && apenasNumeros.startsWith("55")) {
            // Número com código do país
            String semPais = apenasNumeros.substring(2);
            return formatarNumeroBrasil(semPais);
        }
        
        return numero;
    }
    
    public static String ajustarNumeroBrasil(String numero) {
        String apenasNumeros = numero.replaceAll("[^0-9]", "");
        
        // Se tiver 10 dígitos (sem o 9), adiciona o 9
        if (apenasNumeros.length() == 10) {
            String ddd = apenasNumeros.substring(0, 2);
            String resto = apenasNumeros.substring(2);
            apenasNumeros = ddd + "9" + resto;
        }
        
        // Se tiver 11 dígitos mas o terceiro não for 9, adiciona 9
        if (apenasNumeros.length() == 11 && apenasNumeros.charAt(2) != '9') {
            String ddd = apenasNumeros.substring(0, 2);
            String resto = apenasNumeros.substring(2);
            apenasNumeros = ddd + "9" + resto;
        }
        
        // Se tiver 13 dígitos e começar com 55, processa
        if (apenasNumeros.length() == 13 && apenasNumeros.startsWith("55")) {
            String semPais = apenasNumeros.substring(2);
            return ajustarNumeroBrasil(semPais);
        }
        
        // Se tiver apenas 8 dígitos (número local), adiciona DDD padrão 66
        if (apenasNumeros.length() == 8) {
            apenasNumeros = "669" + apenasNumeros;
        }
        
        return apenasNumeros;
    }
    
    public static void salvarHistorico(Context context, String numero, String nome) {
        try {
            File dir = new File(context.getExternalMediaDirs()[0], "Number2Talk");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            File historicoFile = new File(dir, "historico.txt");
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
            String dataHora = sdf.format(new Date());
            
            String nomeFinal = (nome == null || nome.isEmpty()) ? "nome não informado" : nome;
            String linha = dataHora + " | " + numero + " | " + nomeFinal + "\n";
            
            FileWriter fw = new FileWriter(historicoFile, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(linha);
            bw.close();
            fw.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static String lerHistorico(Context context) {
        try {
            File dir = new File(context.getExternalMediaDirs()[0], "Number2Talk");
            File historicoFile = new File(dir, "historico.txt");
            
            if (!historicoFile.exists()) {
                return "Nenhum histórico encontrado.";
            }
            
            java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(historicoFile));
            StringBuilder sb = new StringBuilder();
            String linha;
            while ((linha = br.readLine()) != null) {
                sb.append(linha).append("\n");
            }
            br.close();
            return sb.toString();
            
        } catch (IOException e) {
            return "Erro ao ler histórico: " + e.getMessage();
        }
    }
}
