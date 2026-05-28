package com.resapp.app.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Admin {
    private UUID adminId;
    private UUID accountId;
    private String firstName;
    private String lastName;
}
