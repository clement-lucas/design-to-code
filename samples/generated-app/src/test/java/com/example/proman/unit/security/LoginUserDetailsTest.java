package com.example.proman.unit.security;

import com.example.proman.entity.SystemAccount;
import com.example.proman.entity.Users;
import com.example.proman.security.LoginUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LoginUserDetails 単体テスト")
class LoginUserDetailsTest {

    private SystemAccount createAccount(short failedCount, LocalDate effectiveTo,
                                         LocalDate effectiveFrom, LocalDate passwordExp) {
        SystemAccount account = new SystemAccount();
        account.setUserId(1);
        account.setLoginId("testuser");
        account.setUserPassword("$2a$10$dummyhash");
        account.setFailedCount(failedCount);
        account.setEffectiveDateTo(effectiveTo);
        account.setEffectiveDateFrom(effectiveFrom);
        account.setPasswordExpirationDate(passwordExp);
        return account;
    }

    private Users createUser(boolean pmFlag) {
        Users user = new Users();
        user.setUserId(1);
        user.setKanjiName("テストユーザ");
        user.setKanaName("テストユーザ");
        user.setPmFlag(pmFlag);
        return user;
    }

    @Test
    @DisplayName("U01: PMユーザはROLE_USERとROLE_PROJECT_MANAGER権限を持つ")
    void getAuthorities_pmUser_hasBothRoles() {
        LoginUserDetails details = new LoginUserDetails(
                createAccount((short) 0, LocalDate.of(2099, 12, 31),
                        LocalDate.of(2020, 1, 1), LocalDate.of(2099, 12, 31)),
                createUser(true));

        assertThat(details.getAuthorities()).extracting("authority")
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_PROJECT_MANAGER");
    }

    @Test
    @DisplayName("U02: 非PMユーザはROLE_USERのみ")
    void getAuthorities_nonPmUser_hasUserRoleOnly() {
        LoginUserDetails details = new LoginUserDetails(
                createAccount((short) 0, LocalDate.of(2099, 12, 31),
                        LocalDate.of(2020, 1, 1), LocalDate.of(2099, 12, 31)),
                createUser(false));

        assertThat(details.getAuthorities()).extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("U03: failedCount=4はロック解除状態")
    void isAccountNonLocked_failedCount4_returnsTrue() {
        LoginUserDetails details = new LoginUserDetails(
                createAccount((short) 4, LocalDate.of(2099, 12, 31),
                        LocalDate.of(2020, 1, 1), LocalDate.of(2099, 12, 31)),
                createUser(false));

        assertThat(details.isAccountNonLocked()).isTrue();
    }

    @Test
    @DisplayName("U04: failedCount=5はロック状態")
    void isAccountNonLocked_failedCount5_returnsFalse() {
        LoginUserDetails details = new LoginUserDetails(
                createAccount((short) 5, LocalDate.of(2099, 12, 31),
                        LocalDate.of(2020, 1, 1), LocalDate.of(2099, 12, 31)),
                createUser(false));

        assertThat(details.isAccountNonLocked()).isFalse();
    }

    @Test
    @DisplayName("U05: 有効期限が未来日ならアカウント有効")
    void isAccountNonExpired_futureDate_returnsTrue() {
        LoginUserDetails details = new LoginUserDetails(
                createAccount((short) 0, LocalDate.now().plusYears(1),
                        LocalDate.of(2020, 1, 1), LocalDate.of(2099, 12, 31)),
                createUser(false));

        assertThat(details.isAccountNonExpired()).isTrue();
    }

    @Test
    @DisplayName("U06: パスワード有効期限が未来日ならCredentials有効")
    void isCredentialsNonExpired_futureDate_returnsTrue() {
        LoginUserDetails details = new LoginUserDetails(
                createAccount((short) 0, LocalDate.of(2099, 12, 31),
                        LocalDate.of(2020, 1, 1), LocalDate.now().plusYears(1)),
                createUser(false));

        assertThat(details.isCredentialsNonExpired()).isTrue();
    }

    @Test
    @DisplayName("U07: 有効開始日が過去日ならEnabled")
    void isEnabled_pastEffectiveFrom_returnsTrue() {
        LoginUserDetails details = new LoginUserDetails(
                createAccount((short) 0, LocalDate.of(2099, 12, 31),
                        LocalDate.now().minusYears(1), LocalDate.of(2099, 12, 31)),
                createUser(false));

        assertThat(details.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("getKanjiName - ユーザの漢字名前を返す")
    void getKanjiName_returnsUserKanjiName() {
        LoginUserDetails details = new LoginUserDetails(
                createAccount((short) 0, LocalDate.of(2099, 12, 31),
                        LocalDate.of(2020, 1, 1), LocalDate.of(2099, 12, 31)),
                createUser(true));

        assertThat(details.getKanjiName()).isEqualTo("テストユーザ");
    }

    @Test
    @DisplayName("isPm - PMユーザはtrue")
    void isPm_pmUser_returnsTrue() {
        LoginUserDetails details = new LoginUserDetails(
                createAccount((short) 0, LocalDate.of(2099, 12, 31),
                        LocalDate.of(2020, 1, 1), LocalDate.of(2099, 12, 31)),
                createUser(true));

        assertThat(details.isPm()).isTrue();
    }
}
