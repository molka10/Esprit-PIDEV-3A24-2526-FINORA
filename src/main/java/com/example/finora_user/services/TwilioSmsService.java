package com.example.finora_user.services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

/**
 * Twilio SMS implementation.
 *
 * Supports:
 *  - 8-digit local numbers (auto converts to +216XXXXXXXX)
 *  - Already formatted E.164 numbers (+216...)
 *
 * Requires environment variables:
 *  TWILIO_ACCOUNT_SID
 *  TWILIO_AUTH_TOKEN
 *  TWILIO_FROM_NUMBER
 */
public class TwilioSmsService implements SmsService {

    private static final String DEFAULT_COUNTRY_CODE = "+216"; // Tunisia

    private final String from;

    public TwilioSmsService() {
        String sid = System.getenv("TWILIO_ACCOUNT_SID");
        String token = System.getenv("TWILIO_AUTH_TOKEN");
        from = System.getenv("TWILIO_FROM_NUMBER");

        if (sid == null || token == null || from == null) {
            throw new IllegalStateException(
                    "Twilio env vars missing (TWILIO_ACCOUNT_SID / TWILIO_AUTH_TOKEN / TWILIO_FROM_NUMBER)");
        }

        Twilio.init(sid, token);
    }

    @Override
    public void send(String rawPhone, String message) {
        String e164 = toE164(rawPhone);

        Message.creator(
                new PhoneNumber(e164),
                new PhoneNumber(from),
                message
        ).create();
    }

    /**
     * Converts:
     *   12345678      -> +21612345678
     *   +21612345678  -> +21612345678
     */
    private String toE164(String phone) {

        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Numéro vide.");
        }

        String cleaned = phone.replaceAll("\\s+", "");

        // Already E.164
        if (cleaned.startsWith("+")) {
            if (!cleaned.matches("^\\+\\d{8,15}$")) {
                throw new IllegalArgumentException("Numéro E.164 invalide.");
            }
            return cleaned;
        }

        // Local 8-digit Tunisian number
        if (cleaned.matches("^\\d{8}$")) {
            return DEFAULT_COUNTRY_CODE + cleaned;
        }

        throw new IllegalArgumentException(
                "Format numéro invalide. Stockez 8 chiffres (Tunisie) ou +216XXXXXXXX");
    }
}