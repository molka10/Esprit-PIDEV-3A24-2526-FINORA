package com.example.finora.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class SpeechToTextService {

    private boolean listening = false;

    public interface Listener {
        void onTextRecognized(String text);
    }

    public boolean isListening() {
        return listening;
    }

    public void startListening(Listener listener) {

        if (listening) return;
        listening = true;

        new Thread(() -> {
            try {

                ProcessBuilder builder = new ProcessBuilder(
                        "powershell",
                        "-Command",
                        "Add-Type -AssemblyName System.Speech; " +
                                "$rec = New-Object System.Speech.Recognition.SpeechRecognitionEngine; " +
                                "$rec.SetInputToDefaultAudioDevice(); " +
                                "$rec.LoadGrammar((New-Object System.Speech.Recognition.DictationGrammar)); " +
                                "$result = $rec.Recognize(); " +
                                "if ($result) { Write-Output $result.Text }"
                );

                builder.redirectErrorStream(true);

                Process process = builder.start();

                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(process.getInputStream()));

                String line = reader.readLine();

                System.out.println("STT RAW OUTPUT: " + line);

                if (line != null && !line.isBlank()) {
                    listener.onTextRecognized(line.trim());
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                listening = false;
            }
        }).start();
    }

    public void stopListening() {
        listening = false;
    }
}