package com.niotech.number2talk_on_wa;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

public class ClipboardMonitorService extends Service {
    private static final String TAG = "Number2Talk";
    private WindowManager windowManager;
    private ImageView floatingButton;
    private ClipboardManager clipboardManager;
    private String ultimoNumeroDetectado = "";
    private static final String WHATSAPP_PACKAGE = "com.whatsapp";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "🚀 Service iniciado");
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("n2t_service", 
                    "Number2Talk", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) manager.createNotificationChannel(channel);
            
            Notification notification = new Notification.Builder(this, "n2t_service")
                    .setContentTitle("Number2Talk")
                    .setContentText("Monitorando clipboard...")
                    .setSmallIcon(android.R.drawable.ic_menu_edit)
                    .build();
            startForeground(1, notification);
        }

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        ClipboardManager.OnPrimaryClipChangedListener clipListener = () -> {
            Log.d(TAG, "📋 Clipboard alterado!");
            if (clipboardManager.hasPrimaryClip()) {
                ClipData clipData = clipboardManager.getPrimaryClip();
                if (clipData != null && clipData.getItemCount() > 0) {
                    CharSequence text = clipData.getItemAt(0).getText();
                    if (text != null) {
                        verificarEExibirBalao(text.toString());
                    }
                }
            }
        };
        clipboardManager.addPrimaryClipChangedListener(clipListener);
        Log.d(TAG, "✅ Listener registrado");
    }

    private void verificarEExibirBalao(String textoCopiado) {
        String apenasNumeros = textoCopiado.replaceAll("[^0-9]", "");
        
        if (apenasNumeros.length() >= 10 && !apenasNumeros.equals(ultimoNumeroDetectado)) {
            ultimoNumeroDetectado = apenasNumeros;
            Log.d(TAG, "✅ Número detectado: " + apenasNumeros);
            
            if (floatingButton != null) {
                try {
                    windowManager.removeView(floatingButton);
                } catch (Exception e) {}
                floatingButton = null;
            }

            floatingButton = new ImageView(this);
            floatingButton.setImageResource(android.R.drawable.ic_menu_edit);
            floatingButton.setBackgroundColor(0x80075E54);
            floatingButton.setPadding(15, 15, 15, 15);

            int LAYOUT_FLAG;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
            }

            final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    100, 100, LAYOUT_FLAG,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    android.graphics.PixelFormat.TRANSLUCENT
            );

            params.gravity = Gravity.TOP | Gravity.END;
            params.x = 20;
            params.y = 300;

            floatingButton.setOnClickListener(v -> {
                Log.d(TAG, "🖱️ Balão clicado!");
                abrirWhatsApp(apenasNumeros);
                try {
                    windowManager.removeView(floatingButton);
                    floatingButton = null;
                } catch (Exception e) {}
            });

            try {
                windowManager.addView(floatingButton, params);
                Log.d(TAG, "✅ Balão exibido!");
            } catch (Exception e) {
                Log.e(TAG, "Erro ao exibir balão: " + e.getMessage());
            }
        }
    }
    
    private void abrirWhatsApp(String numero) {
        try {
            String numeroFormatado = numero;
            if (numeroFormatado.length() <= 11 && !numeroFormatado.startsWith("55")) {
                numeroFormatado = "55" + numeroFormatado;
            }
            
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://wa.me/" + numeroFormatado));
            intent.setPackage(WHATSAPP_PACKAGE);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            PackageManager pm = getPackageManager();
            if (intent.resolveActivity(pm) != null) {
                startActivity(intent);
                Log.d(TAG, "✅ WhatsApp aberto: " + numeroFormatado);
            } else {
                Log.d(TAG, "⚠️ WhatsApp não encontrado");
                Toast.makeText(this, "WhatsApp não encontrado!", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro: " + e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingButton != null && windowManager != null) {
            try {
                windowManager.removeView(floatingButton);
            } catch (Exception e) {}
        }
    }
}
