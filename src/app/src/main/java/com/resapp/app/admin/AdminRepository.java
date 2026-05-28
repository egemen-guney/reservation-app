package com.resapp.app.admin;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AdminRepository {
    private final JdbcClient jdbcClient;

    public AdminRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<Admin> findByAdminId(UUID adminId) {
        return jdbcClient.sql("SELECT * FROM admin WHERE admin_id = :id")
                .param("id", adminId)
                .query(Admin.class)
                .optional();
    }

    public Optional<Admin> findByAccountId(UUID accountId) {
        return jdbcClient.sql("SELECT * FROM admin WHERE account_id = :id")
                .param("id", accountId)
                .query(Admin.class)
                .optional();
    }

    public List<Admin> findAll() {
        return jdbcClient.sql("SELECT * FROM admin")
                .query(Admin.class)
                .list();
    }

    public void create(Admin admin) {
        var updated = jdbcClient.sql("INSERT INTO admin (admin_id, account_id, first_name, last_name) " +
                        "VALUES (:adminId, :accountId, :fName, :lName)")
                .param("adminId", admin.getAdminId())
                .param("accountId", admin.getAccountId())
                .param("fName", admin.getFirstName())
                .param("lName", admin.getLastName())
                .update();

        Assert.state(updated == 1, "Failed to create admin profile for account with ID: " + admin.getAccountId());
    }

    public void update(Admin admin) {
        var updated = jdbcClient.sql("UPDATE admin SET first_name = :fName, last_name = :lName WHERE admin_id = :id")
                .param("id", admin.getAdminId())
                .param("fName", admin.getFirstName())
                .param("lName", admin.getLastName())
                .update();

        Assert.state(updated == 1, "Failed to update admin with ID: " + admin.getAdminId());
    }

    public void delete(UUID adminId) {
        var updated = jdbcClient.sql("DELETE FROM admin WHERE admin_id = :id")
                .param("id", adminId)
                .update();

        Assert.state(updated == 1, "Failed to delete admin profile with ID: " + adminId);
    }
}
