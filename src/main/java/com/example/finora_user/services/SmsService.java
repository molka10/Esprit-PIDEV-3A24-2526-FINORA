package com.example.finora_user.services;

public interface SmsService {
    void send(String toPhoneE164, String message);
}