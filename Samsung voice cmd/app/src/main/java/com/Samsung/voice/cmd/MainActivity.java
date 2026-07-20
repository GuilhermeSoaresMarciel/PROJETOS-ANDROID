package com.Samsung.voice.cmd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.hardware.ConsumerIrManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

//Deceloped by: GUILHERME SOARES MARCIEL

public class MainActivity extends Activity {

    private SpeechRecognizer speechRecognizer;
    private Intent speechIntent;
    private ConsumerIrManager irManager;
    private Button micButton;
    private Handler handler = new Handler();
    private boolean escutando;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        irManager = (ConsumerIrManager) getSystemService(Context.CONSUMER_IR_SERVICE);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR");
        speechIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    executarComando(matches.get(0).toLowerCase());
                }
                reiniciarEscuta();
            }

            @Override
            public void onError(int error) {
                reiniciarEscuta();
            }

            @Override public void onReadyForSpeech(Bundle params) {}
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {}
            @Override public void onPartialResults(Bundle partialResults) {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });

        LinearLayout root = new LinearLayout(this);
        root.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#2e2d2d"));

        LinearLayout titleBar = new LinearLayout(this);
        titleBar.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(56)));
        titleBar.setGravity(Gravity.CENTER);
        titleBar.setBackgroundColor(Color.parseColor("#141313"));

        TextView title = new TextView(this);
        title.setText("Samsung Voice CMD");
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(Color.parseColor("#2e2d2d"));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

        titleBar.addView(title);
        root.addView(titleBar);

        LinearLayout center = new LinearLayout(this);
        center.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        center.setGravity(Gravity.CENTER);

        micButton = new Button(this);
        micButton.setText("🎤");
        micButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
        micButton.setTextColor(Color.WHITE);
        micButton.setAllCaps(false);
        micButton.setEnabled(false);

        int size = dp(160);
        micButton.setLayoutParams(new LinearLayout.LayoutParams(size, size));

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(Color.parseColor("#1c1c1c"));
        bg.setStroke(dp(3), Color.WHITE);
        micButton.setBackground(bg);

        center.addView(micButton);
        root.addView(center);

        setContentView(root);

        if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO}, 1001);
        } else {
            iniciarEscuta();
        }
    }

    private void iniciarEscuta() {
        if (escutando) return;
        escutando = true;
        iniciarAnimacao();
        speechRecognizer.startListening(speechIntent);
    }

    private void reiniciarEscuta() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    speechRecognizer.cancel();
                } catch (Exception ignored) {}
                try {
                    speechRecognizer.startListening(speechIntent);
                } catch (Exception ignored) {}
            }
        }, 700);
    }

    private void iniciarAnimacao() {
        ScaleAnimation pulse = new ScaleAnimation(
                0.9f, 1.05f,
                0.9f, 1.05f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        pulse.setDuration(800);
        pulse.setRepeatMode(ScaleAnimation.REVERSE);
        pulse.setRepeatCount(ScaleAnimation.INFINITE);
        micButton.startAnimation(pulse);
    }

    private void executarComando(String comando) {
        Integer code = mapearComando(comando);
        if (code != null) {
            sendIR(code);
        } else {
            Toast.makeText(this, "Comando não reconhecido", Toast.LENGTH_SHORT).show();
        }
    }

    private Integer mapearComando(String c) {
        if (c.contains("ligar") || c.contains("desligar") || c.contains("power")) return 0xE0E040BF;
        if (c.contains("sbt")) return 0xE0E0906F;
        if (c.contains("globo")) return 0xE0E030CF;
        if (c.contains("volume mais")) return 0xE0E0E01F;
        if (c.contains("volume menos")) return 0xE0E0D02F;
        if (c.contains("menu")) return 0xE0E09E61;
        if (c.contains("confirmar")) return 0xE0E016E9;
        if (c.contains("voltar")) return 0xE0E01AE5;
        if (c.contains("sair")) return 0xE0E0B44B;
        if (c.contains("esquerda")) return 0xE0E0A659;
        if (c.contains("direita")) return 0xE0E046B9;
        if (c.contains("cima")) return 0xE0E006F9;
        if (c.contains("baixo")) return 0xE0E08679;
        
        return null;
    }

    private void sendIR(int code) {
        if (irManager == null || !irManager.hasIrEmitter()) {
            Toast.makeText(this, "Sem infravermelho", Toast.LENGTH_SHORT).show();
            return;
        }
        irManager.transmit(38000, buildPattern(code));
    }

    private int[] buildPattern(int code) {
        int[] pattern = new int[67];
        int i = 0;
        pattern[i++] = 9000;
        pattern[i++] = 4500;
        for (int bit = 0; bit < 32; bit++) {
            pattern[i++] = 560;
            pattern[i++] = ((code & 0x80000000) != 0) ? 1690 : 560;
            code <<= 1;
        }
        pattern[i] = 560;
        return pattern;
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                v,
                getResources().getDisplayMetrics());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}

//Deceloped by: GUILHERME SOARES MARCIEL