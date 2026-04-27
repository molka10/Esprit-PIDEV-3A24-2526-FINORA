package com.example.finora.services;

import java.util.List;

public record DuplicateMatch(
        int userId,
        String username,
        String email,
        double similarity,
        List<String> reasons
) {}