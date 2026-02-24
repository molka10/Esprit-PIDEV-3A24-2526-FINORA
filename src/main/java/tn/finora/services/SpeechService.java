package tn.finora.services;

import java.io.IOException;

public class SpeechService {

    private Process process;
    private int rate = 0; // speech speed

    // ✅ ADD THIS METHOD
    public void setRate(int rate) {
        this.rate = rate;
    }

    public void speak(String text) {

        try {
            stop();

            String safeText = text
                    .replace("'", " ")
                    .replace("\"", " ")
                    .replace("\n", " ");

            String command =
                    "PowerShell -Command \"Add-Type -AssemblyName System.Speech; " +
                            "$speak = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                            "$speak.Rate = " + rate + "; " +
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