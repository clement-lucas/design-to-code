package com.example.proman.security;

import com.example.proman.entity.SystemAccount;
import com.example.proman.entity.Users;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LoginUserDetails implements UserDetails {

    @Serial
    private static final long serialVersionUID = 1L;

    private final SystemAccount account;
    private final Users user;
    private final List<GrantedAuthority> authorities;

    public LoginUserDetails(SystemAccount account, Users user) {
        this.account = account;
        this.user = user;
        this.authorities = new ArrayList<>();
        this.authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        if (Boolean.TRUE.equals(user.getPmFlag())) {
            this.authorities.add(new SimpleGrantedAuthority("ROLE_PROJECT_MANAGER"));
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return account.getUserPassword();
    }

    @Override
    public String getUsername() {
        return account.getLoginId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return account.getEffectiveDateTo() == null ||
               !java.time.LocalDate.now().isAfter(account.getEffectiveDateTo());
    }

    @Override
    public boolean isAccountNonLocked() {
        return account.getFailedCount() < 5;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return account.getPasswordExpirationDate() == null ||
               !java.time.LocalDate.now().isAfter(account.getPasswordExpirationDate());
    }

    @Override
    public boolean isEnabled() {
        return account.getEffectiveDateFrom() == null ||
               !java.time.LocalDate.now().isBefore(account.getEffectiveDateFrom());
    }

    public Integer getUserId() {
        return account.getUserId();
    }

    public String getKanjiName() {
        return user.getKanjiName();
    }

    public Boolean isPm() {
        return user.getPmFlag();
    }

    public Users getUser() {
        return user;
    }

    public SystemAccount getAccount() {
        return account;
    }
}
