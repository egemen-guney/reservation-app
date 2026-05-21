package com.resapp.app.account;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.Optional;
import java.util.UUID;

@Repository
public class AccountRepository {
    private final JdbcClient jdbcClient;

    public AccountRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<Account> findById(UUID accountId) {
        return jdbcClient.sql("SELECT * FROM account WHERE account_id = :id")
                .param("id", accountId)
                .query(Account.class)
                .optional();
    }

    public Optional<Account> findByEmail(String email) {
        return jdbcClient.sql("SELECT * FROM account WHERE email = :email")
                .param("email", email)
                .query(Account.class)
                .optional();
    }

    public Optional<Account> findByPhone(String phone) {
        return jdbcClient.sql("SELECT * FROM account WHERE phone = :phone")
                .param("phone", phone)
                .query(Account.class)
                .optional();
    }

    public Optional<Account> findByEmailOrPhone(String identifier) {
        return jdbcClient.sql("SELECT * FROM account WHERE email = :identifier OR phone = :identifier")
                .param("identifier", identifier)
                .query(Account.class)
                .optional();
    }

    public void create(Account account) {
        var updated = jdbcClient.sql("INSERT INTO account (account_id, email, phone, password_hash, role) " +
                        "VALUES (:id, :email, :phone, :passwordHash, :role)")
                .param("id", account.getAccountId())
                .param("email", account.getEmail())
                .param("phone", account.getPhone())
                .param("passwordHash", account.getPasswordHash())
                .param("role", account.getRole().name())
                .update();

        Assert.state(updated == 1, "Failed to create account for " + account.getEmail() + "/" + account.getPhone());
    }

    public void update(Account account) {
        var updated = jdbcClient.sql("UPDATE account SET email = :email, phone = :phone, password_hash = :passwordHash, role = :role WHERE account_id = :id")
                .param("id", account.getAccountId())
                .param("email", account.getEmail())
                .param("phone", account.getPhone())
                .param("passwordHash", account.getPasswordHash())
                .param("role", account.getRole().name())
                .update();

        Assert.state(updated == 1, "Failed to update account with ID: " + account.getAccountId());
    }

    public void delete(UUID accountId) {
        var updated = jdbcClient.sql("DELETE FROM account WHERE account_id = :id")
                .param("id", accountId)
                .update();

        Assert.state(updated == 1, "Failed to delete account with ID: " + accountId);
    }
}
