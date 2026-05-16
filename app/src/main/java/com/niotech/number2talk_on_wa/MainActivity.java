package com.niotech.number2talk_on_wa;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private TextView txtStatus, txtLogs;
    private StringBuilder logBuilder = new StringBuilder();
    private View aba1, aba2, aba3;
    private Button btnAba1, btnAba2, btnAba3;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        txtStatus = findViewById(R.id.txt_status);
        txtLogs = findViewById(R.id.txt_logs);
        
        // Abas
        aba1 = findViewById(R.id.aba1_content);
        aba2 = findViewById(R.id.aba2_content);
        aba3 = findViewById(R.id.aba3_content);
        
        btnAba1 = findViewById(R.id.btn_aba1);
        btnAba2 = findViewById(R.id.btn_aba2);
        btnAba3 = findViewById(R.id.btn_aba3);
        
        // Configurar abas
        btnAba1.setOnClickListener(v -> mostrarAba(1));
        btnAba2.setOnClickListener(v -> mostrarAba(2));
        btnAba3.setOnClickListener(v -> mostrarAba(3));
        
        // Botões da Aba 1
        Button btnColar = findViewById(R.id.btn_colar);
        btnColar.setOnClickListener(v -> processarClipboard());
        
        // Botões da Aba 2
        Button btnDoacao = findViewById(R.id.btn_doacao);
        btnDoacao.setOnClickListener(v -> abrirDoacao());
        
        // Botões da Aba 3
        Button btnVerificar = findViewById(R.id.btn_verificar_permissao);
        Button btnTestarClipboard = findViewById(R.id.btn_testar_clipboard);
        Button btnEnviarLog = findViewById(R.id.btn_enviar_log);
        
        btnVerificar.setOnClickListener(v -> verificarPermissao());
        btnTestarClipboard.setOnClickListener(v -> testarClipboard());
        btnEnviarLog.setOnClickListener(v -> enviarLogWhatsApp());
        
        addLog("🚀 App iniciado - Versão 2.0");
        addLog("📱 Android " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")");
        
        // Verifica permissão e inicia serviço
        verificarEIniciarServico();
        
        // Mostra aba 1 por padrão
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
    
    private void processarClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        
        if (clipboard != null && clipboard.hasPrimaryClip()) {
            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
            if (item != null && item.getText() != null) {
                String textoCopiado = item.getText().toString();
                addLog("📋 Colar executado - Texto: " + textoCopiado);
                
                String apenasNumeros = textoCopiado.replaceAll("[^0-9]", "");
                
                if (apenasNumeros.length() >= 10) {
                    if (apenasNumeros.length() == 10 || apenasNumeros.length() == 11) {
                        apenasNumeros = "55" + apenasNumeros;
                    }
                    addLog("✅ Número processado: " + apenasNumeros);
                    abrirWhatsAppDireto(apenasNumeros);
                } else {
                    addLog("❌ Número inválido: " + apenasNumeros);
                    Toast.makeText(this, "❌ Número inválido!", Toast.LENGTH_LONG).show();
                }
            }
        } else {
            addLog("⚠️ Clipboard vazio");
            Toast.makeText(this, "📋 Área de transferência vazia!", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void abrirWhatsAppDireto(String numero) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://wa.me/" + numero));
            intent.setPackage("com.whatsapp");
            
            PackageManager pm = getPackageManager();
            if (intent.resolveActivity(pm) != null) {
                startActivity(intent);
                addLog("📱 Abrindo WhatsApp para: " + numero);
            } else {
                intent.setPackage(null);
                startActivity(intent);
                addLog("⚠️ WhatsApp não instalado, abrindo navegador");
                Toast.makeText(this, "⚠️ WhatsApp não encontrado", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            addLog("❌ Erro ao abrir WhatsApp: " + e.getMessage());
            Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void abrirDoacao() {
        addLog("☕ Abrindo informações de doação");
        
        // Mostra informações via Toast
        String msg = "Chave PIX: soletrepix@gmail.com\nTitular: Enio Alves Borges\nBanco do Brasil";
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        
        // Opcional: abrir navegador com tutorial PIX
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.google.com/search?q=como+pagar+por+pix+soletrepix%40gmail.com"));
        startActivity(intent);
    }
    
    private void verificarPermissao() {
        addLog("🔍 Verificando permissão de sobreposição...");
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(this)) {
                addLog("✅ Permissão de sobreposição já concedida");
                txtStatus.setText("✅ Status: Serviço ativo com permissão");
                txtStatus.setTextColor(0xFF4CAF50);
                Toast.makeText(this, "✅ Permissão OK! Balão funcionará.", Toast.LENGTH_SHORT).show();
            } else {
                addLog("⚠️ Sem permissão. Abrindo configurações...");
                Toast.makeText(this, "⚠️ Ative a permissão 'Sobrepor outros apps'", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }
    }
    
    private void testarClipboard() {
        addLog("📋 Testando leitura do clipboard...");
        
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null && clipboard.hasPrimaryClip()) {
            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
            if (item != null && item.getText() != null) {
                String text = item.getText().toString();
                addLog("✅ Clipboard contém: " + text);
                Toast.makeText(this, "Clipboard: " + text, Toast.LENGTH_LONG).show();
            } else {
                addLog("⚠️ Clipboard está vazio");
                Toast.makeText(this, "⚠️ Clipboard vazio", Toast.LENGTH_SHORT).show();
            }
        } else {
            addLog("❌ Não foi possível acessar o clipboard");
            Toast.makeText(this, "❌ Erro ao acessar clipboard", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void enviarLogWhatsApp() {
        addLog("📤 Preparando envio de relatório para WhatsApp...");
        
        // Monta relatório completo
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        String dataHora = sdf.format(new Date());
        
        StringBuilder relatorio = new StringBuilder();
        relatorio.append("📊 *RELATÓRIO DEBUG - Number2Talk* 📊\n\n");
        relatorio.append("📅 *Data/Hora:* ").append(dataHora).append("\n");
        relatorio.append("📱 *Dispositivo:* ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL).append("\n");
        relatorio.append("🤖 *Android:* ").append(Build.VERSION.RELEASE).append(" (API ").append(Build.VERSION.SDK_INT).append(")\n");
        relatorio.append("🔧 *App Versão:* 2.0\n\n");
        
        relatorio.append("📋 *Permissão de Sobreposição:* ");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            relatorio.append(Settings.canDrawOverlays(this) ? "✅ CONCEDIDA" : "❌ NEGADA");
        } else {
            relatorio.append("✅ AUTOMÁTICA (Android < 6.0)");
        }
        relatorio.append("\n\n");
        
        relatorio.append("📝 *LOGS DO SISTEMA:*\n");
        relatorio.append("─────────────────────────────\n");
        relatorio.append(logBuilder.toString());
        relatorio.append("\n─────────────────────────────\n\n");
        relatorio.append("👨‍💻 *Desenvolvedor:* Enio Alves Borges\n");
        relatorio.append("🐙 *GitHub:* github.com/niopolimathtechnical-00110011\n");
        relatorio.append("☕ *Chave PIX:* soletrepix@gmail.com\n\n");
        relatorio.append("🐠🐟 *Sua contribuição ajuda a manter o projeto!*\n");
        
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String mensagem = relatorio.toString().replace(" ", "%20").replace("\n", "%0A");
            intent.setData(Uri.parse("https://wa.me/5566984328877?text=" + mensagem));
            intent.setPackage("com.whatsapp");
            
            PackageManager pm = getPackageManager();
            if (intent.resolveActivity(pm) != null) {
                startActivity(intent);
                addLog("✅ Relatório enviado para WhatsApp (66984328877)");
                Toast.makeText(this, "✅ Enviando relatório...", Toast.LENGTH_SHORT).show();
            } else {
                addLog("⚠️ WhatsApp não instalado");
                Toast.makeText(this, "⚠️ Instale o WhatsApp para enviar o relatório", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            addLog("❌ Erro ao enviar: " + e.getMessage());
            Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void verificarEIniciarServico() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            addLog("⚠️ Permissão de sobreposição NÃO concedida");
            txtStatus.setText("⚠️ Status: Ative a permissão 'Sobrepor outros apps'");
            txtStatus.setTextColor(0xFFFF9800);
            Toast.makeText(this, "⚠️ Ative a permissão de sobreposição para o balão funcionar", Toast.LENGTH_LONG).show();
        } else {
            addLog("✅ Serviço iniciado com sucesso!");
            txtStatus.setText("✅ Status: Serviço em execução");
            txtStatus.setTextColor(0xFF4CAF50);
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
        
        // Limita o tamanho do log para performance
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
