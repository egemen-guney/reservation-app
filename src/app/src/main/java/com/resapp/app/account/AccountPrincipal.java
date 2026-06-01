package com.resapp.app.account;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class AccountPrincipal implements UserDetails {
    private final Account account;
    private final String loginIdentifier;

    public AccountPrincipal(Account account, String loginIdentifier) {
        this.account = account;
        this.loginIdentifier = loginIdentifier;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String roleName = "ROLE_" + account.getRole().name();
        return Collections.singleton(new SimpleGrantedAuthority(roleName));
    }

    @Override
    public @Nullable String getPassword() {
        return account.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return loginIdentifier;
//        previous way
//        return account.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public Account getAccount() {
        return account;
    }
}
