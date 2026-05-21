package com.resapp.app.address;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.Optional;
import java.util.UUID;

@Repository
public class AddressRepository {
    private final JdbcClient jdbcClient;

    public AddressRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<Address> findById(UUID addressId) {
        return jdbcClient.sql("SELECT * FROM address WHERE address_id = :id")
                .param("id", addressId)
                .query(Address.class)
                .optional();
    }

    public void create(Address address) {
        var updated = jdbcClient.sql("INSERT INTO address (address_id, street, city, state, zip_code, country) " +
                        "VALUES (:id, :street, :city, :state, :zipCode, :country)")
                .param("id", address.getAddressId())
                .param("street", address.getStreet())
                .param("city", address.getCity())
                .param("state", address.getState())
                .param("zipCode", address.getZipCode())
                .param("country", address.getCountry())
                .update();

        Assert.state(updated == 1, "Failed to create address with ID: " + address.getAddressId());
    }

    public void update(Address address) {
        var updated = jdbcClient.sql("UPDATE address SET street = :street, city = :city, state = :state, zip_code = :zipCode, country = :country " +
                "WHERE address_id = :id")
                .param("id", address.getAddressId())
                .param("street", address.getStreet())
                .param("city", address.getCity())
                .param("state", address.getState())
                .param("zipCode", address.getZipCode())
                .param("country", address.getCountry())
                .update();

        Assert.state(updated == 1, "Failed to update address with ID: " + address.getAddressId());
    }

    public void delete(UUID addressId) {
        var updated = jdbcClient.sql("DELETE FROM address WHERE address_id = :id")
                .param("id", addressId)
                .update();

        Assert.state(updated == 1, "Failed to delete address with ID: " + addressId);
    }
}
