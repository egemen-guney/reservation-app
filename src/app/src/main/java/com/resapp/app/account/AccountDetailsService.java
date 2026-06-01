package com.resapp.app.account;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AccountDetailsService implements UserDetailsService {
    private final AccountRepository accountRepository;

    public AccountDetailsService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String emailOrPhone) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmailOrPhone(emailOrPhone)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with these credentials: " + emailOrPhone));

        return new AccountPrincipal(account, emailOrPhone);
    }
}
