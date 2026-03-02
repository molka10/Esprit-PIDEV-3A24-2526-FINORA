package com.example.finora.services;

public interface SmsService {
    void send(String toPhoneE164, String message);
}