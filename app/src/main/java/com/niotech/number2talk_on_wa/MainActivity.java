package com.niotech.number2talk_on_wa;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private TextView txtStatus, txtLogs;
    private StringBuilder logBuilder = new StringBuilder();
    private EditText edtTelefone, edtNome;
    private View aba1, aba2, aba3;
    private Button btnAba1, btnAba2, btnAba3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        txtStatus = findViewById(R.id.txt_status);
        txtLogs = findViewById(R.id.txt_debug_log);
        edtTelefone = findViewById(R.id.edt_telefone);
        edtNome = findViewById(R.id.edt_nome);
        
        // Abas
        aba1 = findViewById(R.id.aba1_content);
        aba2 = findViewById(R.id.aba2_content);
        aba3 = findViewById(R.id.aba3_content);
        
        btnAba1 = findViewById(R.id.btn_aba1);
        btnAba2 = findViewById(R.id.btn_aba2);
        btnAba3 = findViewById(R.id.btn_aba3);
        
        btnAba1.setOnClickListener(v -> mostrarAba(1));
        btnAba2.setOnClickListener(v -> mostrarAba(2));
        btnAba3.setOnClickListener(v -> mostrarAba(3));
        
        // Botões da Aba 1 - Conversa Rápida
        Button btnConversar = findViewById(R.id.btn_conversar);
        Button btnLimpar = findViewById(R.id.btn_limpar);
        Button btnColar = findViewById(R.id.btn_colar);
        
        btnConversar.setOnClickListener(v -> conversarComNumero());
        btnLimpar.setOnClickListener(v -> limparCampos());
        btnColar.setOnClickListener(v -> processarClipboard());
        
        // Botão da Aba 2 - Doação
        Button btnDoacao = findViewById(R.id.btn_doacao);
        btnDoacao.setOnClickListener(v -> copiarChavePix());
        
        // Botões da Aba 3 - Ferramentas
        Button btnHistorico = findViewById(R.id.btn_historico);
        Button btnVerificar = findViewById(R.id.btn_verificar);
        Button btnTestarClipboard = findViewById(R.id.btn_testar_clipboard);
        Button btnReportarErro = findViewById(R.id.btn_reportar_erro);
        
        btnHistorico.setOnClickListener(v -> mostrarHistorico());
        btnVerificar.setOnClickListener(v -> verificarPermissao());
        btnTestarClipboard.setOnClickListener(v -> testarClipboard());
        btnReportarErro.setOnClickListener(v -> mostrarDialogoReportarErro());
        
        addLog("🚀 Number2Talk v2.1 iniciado");
        addLog("📱 Android " + Build.VERSION.RELEASE);
        
        verificarWhatsAppInstalado();
        verificarEIniciarServico();
        mostrarAba(1);
    }
    
    private void mostrarAba(int aba) {
        aba1.setVisibility(View.GONE);
        aba2.setVisibility(View.GONE);
        aba3.setVisibility(View.GONE);
        
        if (aba == 1) aba1.setVisibility(View.VISIBLE);
        else if (aba == 2) aba2.setVisibility(View.VISIBLE);
        else if (aba == 3) aba3.setVisibility(View.VISIBLE);
    }
    
    private void limparCampos() {
        edtTelefone.setText("");
        edtNome.setText("");
        addLog("🗑️ Campos limpos");
        Toast.makeText(this, "Campos limpos!", Toast.LENGTH_SHORT).show();
    }
    
    private String validarNumero(String numero) {
        String apenasNumeros = numero.replaceAll("[^0-9]", "");
        
        if (apenasNumeros.length() == 10) {
            return apenasNumeros;
        } else if (apenasNumeros.length() == 11) {
            return apenasNumeros;
        } else if (apenasNumeros.length() == 12) {
            return apenasNumeros;
        } else if (apenasNumeros.length() == 13 && apenasNumeros.startsWith("55")) {
            return apenasNumeros;
        } else if (apenasNumeros.length() == 8) {
            return "669" + apenasNumeros;
        } else if (apenasNumeros.length() == 9 && !apenasNumeros.startsWith("9")) {
            return "66" + apenasNumeros;
        } else if (apenasNumeros.length() == 9 && apenasNumeros.startsWith("9")) {
            return "66" + apenasNumeros;
        }
        
        return null;
    }
    
    private void conversarComNumero() {
        String numero = edtTelefone.getText().toString();
        String nome = edtNome.getText().toString();
        
        if (numero == null || numero.trim().isEmpty()) {
            Toast.makeText(this, "⚠️ Digite um número de telefone!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String numeroValidado = validarNumero(numero);
        
        if (numeroValidado == null) {
            Toast.makeText(this, "⚠️ Número inválido!\nUse 10, 11 ou 13 dígitos (ex: 66984328877)", Toast.LENGTH_LONG).show();
            return;
        }
        
        String nomeFinal = (nome == null || nome.trim().isEmpty()) ? "nome não informado" : nome;
        TelefoneHelper.salvarHistorico(this, numeroValidado, nomeFinal);
        
        addLog("📞 Conversar com: " + numeroValidado + " (" + nomeFinal + ")");
        
        abrirWhatsApp(numeroValidado);
    }
    
    private void processarClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        
        if (clipboard != null && clipboard.hasPrimaryClip()) {
            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
            if (item != null && item.getText() != null) {
                String texto = item.getText().toString();
                addLog("📋 Copiado: " + texto);
                
                String apenasNumeros = texto.replaceAll("[^0-9]", "");
                
                if (apenasNumeros.length() >= 8) {
                    String numeroValidado = validarNumero(apenasNumeros);
                    if (numeroValidado != null) {
                        edtTelefone.setText(numeroValidado);
                        addLog("📞 Número colado: " + numeroValidado);
                        Toast.makeText(this, "Número colado: " + numeroValidado, Toast.LENGTH_SHORT).show();
                    } else {
                        addLog("❌ Número inválido");
                        Toast.makeText(this, "Número inválido! Use 10, 11 ou 13 dígitos.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    addLog("❌ Número muito curto");
                    Toast.makeText(this, "Número muito curto!", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            addLog("📋 Clipboard vazio");
        }
    }
    
    private void mostrarHistorico() {
        String historico = TelefoneHelper.lerHistorico(this);
        
        if (historico.equals("Nenhum histórico encontrado.")) {
            Toast.makeText(this, "Nenhum histórico encontrado.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("📜 Histórico de Conversas");
        
        TextView textView = new TextView(this);
        textView.setText(historico);
        textView.setPadding(40, 20, 40, 20);
        textView.setTextSize(12);
        textView.setClickable(true);
        textView.setMovementMethod(new android.text.method.ScrollingMovementMethod());
        
        final String[] linhas = historico.split("\n");
        
        builder.setView(textView);
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.setNeutralButton("Limpar Histórico", (dialog, which) -> {
            limparHistorico();
        });
        
        AlertDialog dialog = builder.create();
        dialog.show();
        
        textView.setOnTouchListener((v, event) -> {
            android.text.Layout layout = ((TextView) v).getLayout();
            if (layout != null) {
                int line = layout.getLineForVertical((int) event.getY());
                if (line >= 0 && line < linhas.length) {
                    String linhaSelecionada = linhas[line];
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Histórico", linhaSelecionada);
                    clipboard.setPrimaryClip(clip);
                    addLog("📋 Copiado: " + linhaSelecionada);
                    Toast.makeText(this, "✅ Copiado: " + linhaSelecionada, Toast.LENGTH_LONG).show();
                    return true;
                }
            }
            return false;
        });
        
        addLog("📜 Histórico visualizado");
    }
    
    private void limparHistorico() {
        try {
            java.io.File dir = new java.io.File(getExternalMediaDirs()[0], "Number2Talk");
            java.io.File historicoFile = new java.io.File(dir, "historico.txt");
            if (historicoFile.exists()) {
                historicoFile.delete();
                addLog("🗑️ Histórico limpo");
                Toast.makeText(this, "Histórico limpo com sucesso!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            addLog("❌ Erro ao limpar histórico: " + e.getMessage());
        }
    }
    
    private void mostrarDialogoReportarErro() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🐞 Reportar Erro");
        
        final EditText inputDescricao = new EditText(this);
        inputDescricao.setHint("Descreva o erro que ocorreu...");
        inputDescricao.setPadding(40, 20, 40, 20);
        inputDescricao.setMinHeight(150);
        
        builder.setView(inputDescricao);
        builder.setPositiveButton("Enviar", (dialog, which) -> {
            String descricao = inputDescricao.getText().toString();
            enviarRelatorioErro(descricao);
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
        
        addLog("🐞 Diálogo de reporte de erro aberto");
    }
    
    private void enviarRelatorioErro(String descricao) {
        addLog("📤 Enviando relatório de erro...");
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        String dataHora = sdf.format(new Date());
        
        StringBuilder relatorio = new StringBuilder();
        relatorio.append("🐞 *RELATÓRIO DE ERRO - Number2Talk* 🐞\n\n");
        relatorio.append("📅 *Data/Hora:* ").append(dataHora).append("\n");
        relatorio.append("📱 *Modelo:* ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL).append("\n");
        relatorio.append("🤖 *Android:* ").append(Build.VERSION.RELEASE).append("\n\n");
        
        relatorio.append("📝 *DESCRIÇÃO DO ERRO:*\n");
        relatorio.append(descricao.isEmpty() ? "Nenhuma descrição fornecida" : descricao).append("\n\n");
        
        relatorio.append("📋 *LOGS DO SISTEMA:*\n");
        relatorio.append(logBuilder.toString());
        relatorio.append("\n\n---\n");
        relatorio.append("📱 App: Number2Talk v2.1\n");
        relatorio.append("👨‍💻 Enviado automaticamente pelo app");
        
        String numeroDev = "5566984328877";
        String texto = relatorio.toString().replace(" ", "%20").replace("\n", "%0A").replace("&", "%26");
        
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://wa.me/" + numeroDev + "?text=" + texto));
            intent.setPackage("com.whatsapp");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            PackageManager pm = getPackageManager();
            if (intent.resolveActivity(pm) != null) {
                startActivity(intent);
                addLog("✅ Relatório de erro enviado para " + numeroDev);
                Toast.makeText(this, "✅ Relatório enviado! Aguarde o suporte.", Toast.LENGTH_LONG).show();
            } else {
                addLog("❌ WhatsApp não encontrado");
                Toast.makeText(this, "WhatsApp não encontrado!\nInstale o WhatsApp para enviar o relatório.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            addLog("❌ Erro no envio: " + e.getMessage());
            Toast.makeText(this, "Erro ao enviar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void copiarChavePix() {
        String chavePix = "soletrepix@gmail.com";
        
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Chave PIX", chavePix);
        clipboard.setPrimaryClip(clip);
        
        addLog("☕ Chave PIX copiada: " + chavePix);
        
        Toast.makeText(this, "✅ Chave PIX copiada!\n" + chavePix + "\n\nAbra seu app bancário e cole no PIX.", Toast.LENGTH_LONG).show();
    }
    
    private boolean isWhatsAppInstalled() {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES);
            addLog("✅ WhatsApp detectado");
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            addLog("⚠️ WhatsApp não detectado");
            return false;
        }
    }
    
    private void verificarWhatsAppInstalado() {
        if (isWhatsAppInstalled()) {
            txtStatus.setText("✅ WhatsApp OK | Serviço ativo");
        } else {
            txtStatus.setText("⚠️ WhatsApp não instalado!");
        }
    }
    
    private void abrirWhatsApp(String numero) {
        String numeroFormatado = numero.replaceAll("[^0-9]", "");
        if (!numeroFormatado.startsWith("55") && numeroFormatado.length() <= 11) {
            numeroFormatado = "55" + numeroFormatado;
        }
        
        addLog("📞 Abrindo WhatsApp: " + numeroFormatado);
        
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://wa.me/" + numeroFormatado));
            intent.setPackage("com.whatsapp");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            PackageManager pm = getPackageManager();
            if (intent.resolveActivity(pm) != null) {
                startActivity(intent);
                addLog("✅ WhatsApp aberto com sucesso!");
            } else {
                addLog("❌ WhatsApp não encontrado");
                Toast.makeText(this, "WhatsApp não encontrado!", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            addLog("❌ Erro: " + e.getMessage());
        }
    }
    
    private void testarClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null && clipboard.hasPrimaryClip()) {
            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
            if (item != null && item.getText() != null) {
                String text = item.getText().toString();
                addLog("📋 Clipboard: " + text);
                Toast.makeText(this, "Clipboard: " + text, Toast.LENGTH_LONG).show();
            } else {
                addLog("⚠️ Clipboard vazio");
            }
        }
    }
    
    private void verificarPermissao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(this)) {
                addLog("✅ Permissão de sobreposição OK");
                Toast.makeText(this, "Permissão OK! Balão funcionará.", Toast.LENGTH_SHORT).show();
            } else {
                addLog("⚠️ Abrindo configurações de permissão");
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }
    }
    
    private void verificarEIniciarServico() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            addLog("⚠️ Permissão de sobreposição NÃO concedida");
            txtStatus.setText("⚠️ Ative a permissão de sobreposição");
        } else {
            addLog("✅ Serviço de monitoramento iniciado");
            txtStatus.setText("✅ Serviço ativo | Monitorando clipboard");
            iniciarServico();
        }
    }
    
    private void iniciarServico() {
        Intent serviceIntent = new Intent(this, ClipboardMonitorService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }
    
    private void addLog(String log) {
        String timestamp = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        logBuilder.insert(0, "[" + timestamp + "] " + log + "\n");
        
        if (logBuilder.length() > 5000) {
            logBuilder.setLength(5000);
        }
        
        runOnUiThread(() -> {
            if (txtLogs != null) {
                txtLogs.setText(logBuilder.toString());
            }
        });
    }
}
