package com.example.finora.services;

public class ConsoleSmsService implements SmsService {

    @Override
    public void send(String toPhoneE164, String message) {
        System.out.println("===== FINORA SMS (DEV MODE) =====");
        System.out.println("To: " + toPhoneE164);
        System.out.println("Message: " + message);
        System.out.println("================================");
    }
}