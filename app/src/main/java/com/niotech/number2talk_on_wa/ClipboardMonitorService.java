package com.niotech.number2talk_on_wa;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

public class ClipboardMonitorService extends Service {

    private WindowManager windowManager;
    private ImageView floatingButton;
    private ClipboardManager clipboardManager;
    private ClipboardManager.OnPrimaryClipChangedListener clipListener;
    private String ultimoNumeroDetectado = "";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Criar canal de notificação obrigatório para serviços em primeiro plano (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("n2t_service", 
                    "Monitor de Clipboard", NotificationManager.IMPORTANCE_MIN);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) manager.createNotificationChannel(channel);
            
            Notification notification = new Notification.Builder(this, "n2t_service")
                    .setContentTitle("Number2Talk ativo")
                    .setSmallIcon(R.drawable.folder_apps)
                    .build();
            startForeground(1, notification);
        }

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        // Ouvinte que monitora quando algo é copiado no celular
        clipListener = new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                if (clipboardManager.hasPrimaryClip()) {
                    ClipData clipData = clipboardManager.getPrimaryClip();
                    if (clipData != null && clipData.getItemCount() > 0) {
                        CharSequence text = clipData.getItemAt(0).getText();
                        if (text != null) {
                            verificarEExibirBalao(text.toString());
                        }
                    }
                }
            }
        };
        clipboardManager.addPrimaryClipChangedListener(clipListener);
    }

    private void verificarEExibirBalao(String textoCopiado) {
        // Limpa caracteres especiais do telefone
        final String apenasNumeros = textoCopiado.replaceAll("[^0-9]", "");

        // Valida se o conteúdo copiado tem estrutura de número de telefone válido
        if (apenasNumeros.length() >= 10 && !apenasNumeros.equals(ultimoNumeroDetectado)) {
            ultimoNumeroDetectado = apenasNumeros;
            
            // Evita duplicar o botão se ele já estiver visível na tela
            if (floatingButton != null) removerBalao();

            // Configura o formato do balão flutuante usando o ícone do projeto
            floatingButton = new ImageView(this);
            floatingButton.setImageResource(R.drawable.folder_apps);

            int LAYOUT_FLAG;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
            }

            final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    140, 140, // Largura e Altura do balão em pixels
                    LAYOUT_FLAG,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
            );

            params.gravity = Gravity.TOP | Gravity.END; // Aparece no canto superior direito
            params.x = 20;
            params.y = 300;

            // Permite ao usuário arrastar o botão pela tela ou apenas clicar
            floatingButton.setOnTouchListener(new View.OnTouchListener() {
                private int initialX, initialY;
                private float initialTouchX, initialTouchY;
                private boolean isClick = true;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            initialX = params.x;
                            initialY = params.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            isClick = true;
                            return true;
                        case MotionEvent.ACTION_UP:
                            if (isClick) {
                                // AÇÃO DO CLIQUE: Redireciona direto para o WhatsApp
                                String linkWa = "https://wa.me" + (apenasNumeros.length() <= 11 ? "55" + apenasNumeros : apenasNumeros);
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkWa));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                removerBalao(); // Some após abrir a mensagem
                            }
                            return true;
                        case MotionEvent.ACTION_MOVE:
                            params.x = initialX - (int) (event.getRawX() - initialTouchX);
                            params.y = initialY + (int) (event.getRawY() - initialTouchY);
                            windowManager.updateViewLayout(floatingButton, params);
                            if (Math.abs(event.getRawX() - initialTouchX) > 10 || Math.abs(event.getRawY() - initialTouchY) > 10) {
                                isClick = false; // Se arrastou, não dispara o clique
                            }
                            return true;
                    }
                    return false;
                }
            });

            windowManager.addView(floatingButton, params);
        }
    }

    private void removerBalao() {
        if (floatingButton != null && windowManager != null) {
            try {
                windowManager.removeView(floatingButton);
            } catch (Exception ignored) {}
            floatingButton = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removerBalao();
        if (clipboardManager != null && clipListener != null) {
            clipboardManager.removePrimaryClipChangedListener(clipListener);
        }
    }
}
