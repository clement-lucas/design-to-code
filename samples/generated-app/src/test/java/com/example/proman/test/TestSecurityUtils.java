package com.example.proman.test;

import com.example.proman.entity.SystemAccount;
import com.example.proman.entity.Users;
import com.example.proman.security.LoginUserDetails;

import java.time.LocalDate;

/**
 * テスト用セキュリティユーティリティ
 */
public class TestSecurityUtils {

    public static LoginUserDetails createPmUser() {
        Users user = new Users();
        user.setUserId(1);
        user.setKanjiName("山田太郎");
        user.setKanaName("ヤマダタロウ");
        user.setPmFlag(true);

        SystemAccount account = new SystemAccount();
        account.setUserId(1);
        account.setLoginId("admin");
        account.setUserPassword("dummy");
        account.setPasswordExpirationDate(LocalDate.of(2099, 12, 31));
        account.setFailedCount((short) 0);
        account.setEffectiveDateFrom(LocalDate.of(2020, 1, 1));
        account.setEffectiveDateTo(LocalDate.of(2099, 12, 31));

        return new LoginUserDetails(account, user);
    }

    public static LoginUserDetails createNormalUser() {
        Users user = new Users();
        user.setUserId(2);
        user.setKanjiName("田中花子");
        user.setKanaName("タナカハナコ");
        user.setPmFlag(false);

        SystemAccount account = new SystemAccount();
        account.setUserId(2);
        account.setLoginId("member");
        account.setUserPassword("dummy");
        account.setPasswordExpirationDate(LocalDate.of(2099, 12, 31));
        account.setFailedCount((short) 0);
        account.setEffectiveDateFrom(LocalDate.of(2020, 1, 1));
        account.setEffectiveDateTo(LocalDate.of(2099, 12, 31));

        return new LoginUserDetails(account, user);
    }
}
