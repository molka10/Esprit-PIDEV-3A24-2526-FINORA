package tn.finora.services;

import java.io.IOException;

public class SpeechService {

    private Process process;

    public void speak(String text) {

        try {
            stop();

            // Escape single quotes to prevent PowerShell crash
            String safeText = text
                    .replace("'", " ")
                    .replace("\"", " ")
                    .replace("\n", " ");

            String command =
                    "PowerShell -Command \"Add-Type -AssemblyName System.Speech; " +
                            "$speak = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                            "$speak.Rate = 0; " +   // Speed control (-10 to 10)
                            "$speak.Speak('" + safeText + "');\"";

            process = Runtime.getRuntime().exec(command);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (process != null) {
            process.destroy();
        }
    }
}