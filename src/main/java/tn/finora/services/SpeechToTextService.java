package tn.finora.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class SpeechToTextService {

    public interface Listener {
        void onTextRecognized(String text);
    }

    public void startListening(Listener listener) {

        new Thread(() -> {
            try {

                String command =
                        "PowerShell -Command \"Add-Type -AssemblyName System.Speech; " +
                                "$recognizer = New-Object System.Speech.Recognition.SpeechRecognitionEngine; " +
                                "$recognizer.SetInputToDefaultAudioDevice(); " +
                                "$recognizer.LoadGrammar((New-Object System.Speech.Recognition.DictationGrammar)); " +
                                "$result = $recognizer.Recognize(); " +
                                "if ($result -ne $null) { Write-Output $result.Text }\"";

                Process process = Runtime.getRuntime().exec(command);

                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(process.getInputStream()));

                String text = reader.readLine();

                if (text != null && !text.isBlank()) {
                    listener.onTextRecognized(text);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}