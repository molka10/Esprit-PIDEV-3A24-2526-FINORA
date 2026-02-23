package com.example.finora_user.services;

import java.util.List;

public record DuplicateMatch(
        int userId,
        String username,
        String email,
        double similarity,
        List<String> reasons
) {}