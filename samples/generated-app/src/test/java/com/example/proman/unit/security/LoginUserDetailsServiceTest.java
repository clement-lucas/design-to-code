package com.example.proman.unit.security;

import com.example.proman.entity.SystemAccount;
import com.example.proman.entity.Users;
import com.example.proman.repository.SystemAccountRepository;
import com.example.proman.repository.UsersRepository;
import com.example.proman.security.LoginUserDetails;
import com.example.proman.security.LoginUserDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginUserDetailsService 単体テスト")
class LoginUserDetailsServiceTest {

    @Mock
    private SystemAccountRepository systemAccountRepository;
    @Mock
    private UsersRepository usersRepository;

    @InjectMocks
    private LoginUserDetailsService loginUserDetailsService;

    @Test
    @DisplayName("U08: loadUserByUsername - 存在するloginIdでLoginUserDetails返却")
    void loadUserByUsername_existingUser_returnsDetails() {
        SystemAccount account = new SystemAccount();
        account.setUserId(1);
        account.setLoginId("admin");
        account.setUserPassword("$2a$10$hash");
        account.setPasswordExpirationDate(LocalDate.of(2099, 12, 31));
        account.setFailedCount((short) 0);
        account.setEffectiveDateFrom(LocalDate.of(2020, 1, 1));
        account.setEffectiveDateTo(LocalDate.of(2099, 12, 31));

        Users user = new Users();
        user.setUserId(1);
        user.setKanjiName("山田太郎");
        user.setKanaName("ヤマダタロウ");
        user.setPmFlag(true);

        when(systemAccountRepository.findByLoginId("admin")).thenReturn(Optional.of(account));
        when(usersRepository.findById(1)).thenReturn(Optional.of(user));

        UserDetails result = loginUserDetailsService.loadUserByUsername("admin");

        assertThat(result).isInstanceOf(LoginUserDetails.class);
        assertThat(result.getUsername()).isEqualTo("admin");
        assertThat(result.getAuthorities()).extracting("authority")
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_PROJECT_MANAGER");
    }

    @Test
    @DisplayName("U09: loadUserByUsername - 存在しないloginIdでUsernameNotFoundException")
    void loadUserByUsername_nonExistingUser_throwsException() {
        when(systemAccountRepository.findByLoginId("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginUserDetailsService.loadUserByUsername("unknown"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("unknown");
    }
}
