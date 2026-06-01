package com.resapp.app.account;

import java.util.UUID;

// Serves as a session token
public record LoginResponse(
        String token,
        String role,
        UUID profileId
//        UUID accountId,
//        AccountRole role,
//        String message
) {
}
