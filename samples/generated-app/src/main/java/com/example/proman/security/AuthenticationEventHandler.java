package com.example.proman.security;

import com.example.proman.entity.SystemAccount;
import com.example.proman.repository.SystemAccountRepository;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class AuthenticationEventHandler {

    private final SystemAccountRepository systemAccountRepository;

    public AuthenticationEventHandler(SystemAccountRepository systemAccountRepository) {
        this.systemAccountRepository = systemAccountRepository;
    }

    @EventListener
    @Transactional
    public void handleAuthenticationSuccess(AuthenticationSuccessEvent event) {
        if (event.getAuthentication().getPrincipal() instanceof LoginUserDetails userDetails) {
            SystemAccount account = systemAccountRepository.findById(userDetails.getUserId()).orElse(null);
            if (account != null) {
                account.setFailedCount((short) 0);
                account.setLastLoginDateTime(LocalDateTime.now());
                systemAccountRepository.save(account);
            }
        }
    }

    @EventListener
    @Transactional
    public void handleAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
        String loginId = (String) event.getAuthentication().getPrincipal();
        systemAccountRepository.findByLoginId(loginId).ifPresent(account -> {
            account.setFailedCount((short) (account.getFailedCount() + 1));
            systemAccountRepository.save(account);
        });
    }
}
