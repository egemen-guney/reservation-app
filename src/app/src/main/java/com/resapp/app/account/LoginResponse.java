package com.resapp.app.account;

import java.util.UUID;

// Serves as a session token
public record LoginResponse(
        UUID accountId,
        AccountRole role,
        String message
) {
}
