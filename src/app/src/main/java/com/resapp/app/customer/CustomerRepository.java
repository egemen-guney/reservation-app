package com.resapp.app.customer;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CustomerRepository {
    private final JdbcClient jdbcClient;

    public CustomerRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<Customer> findByCustomerId(UUID customerId) {
        return jdbcClient.sql("SELECT * FROM customer WHERE customer_id = :id")
                .param("id", customerId)
                .query(Customer.class)
                .optional();
    }

    public Optional<Customer> findByAccountId(UUID accountId) {
        return jdbcClient.sql("SELECT * FROM customer WHERE account_id = :id")
                .param("id", accountId)
                .query(Customer.class)
                .optional();
    }

    public List<Customer> findAll() {
        return jdbcClient.sql("SELECT * FROM customer")
                .query(Customer.class)
                .list();
    }

    public void create(Customer customer) {
        var updated = jdbcClient.sql("INSERT INTO customer (customer_id, account_id, first_name, last_name) " +
                "VALUES (:customerId, :accountId, :fName, :lName)")
                .param("customerId", customer.getCustomerId())
                .param("accountId", customer.getAccountId())
                .param("fName", customer.getFirstName())
                .param("lName", customer.getLastName())
                .update();

        Assert.state(updated == 1, "Failed to create customer profile for account with ID: " + customer.getAccountId());
    }

    public void update(Customer customer) {
        var updated = jdbcClient.sql("UPDATE customer SET first_name = :fName, last_name = :lName WHERE customer_id = :id")
                .param("id", customer.getCustomerId())
                .param("fName", customer.getFirstName())
                .param("lName", customer.getLastName())
                .update();

        Assert.state(updated == 1, "Failed to update customer with ID: " + customer.getCustomerId());
    }

    public void delete(UUID customerId) {
        var updated = jdbcClient.sql("DELETE FROM customer WHERE customer_id = :id")
                .param("id", customerId)
                .update();

        Assert.state(updated == 1, "Failed to delete customer profile with ID: " + customerId);
    }
}
