package com.resapp.app.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    private UUID accountId;
    private String email;
    private String phone;
    private String passwordHash;
    private AccountRole role;
    private OffsetDateTime createdAt;
}
