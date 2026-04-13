package com.example.proman.security;

import com.example.proman.entity.SystemAccount;
import com.example.proman.entity.Users;
import com.example.proman.repository.SystemAccountRepository;
import com.example.proman.repository.UsersRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginUserDetailsService implements UserDetailsService {

    private final SystemAccountRepository systemAccountRepository;
    private final UsersRepository usersRepository;

    public LoginUserDetailsService(SystemAccountRepository systemAccountRepository,
                                   UsersRepository usersRepository) {
        this.systemAccountRepository = systemAccountRepository;
        this.usersRepository = usersRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        SystemAccount account = systemAccountRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + loginId));

        Users user = usersRepository.findById(account.getUserId())
                .orElseThrow(() -> new UsernameNotFoundException("User entity not found for userId: " + account.getUserId()));

        return new LoginUserDetails(account, user);
    }
}
