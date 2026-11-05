package com.superpower.official;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {
    public static final String POLICY_VERSION = "2026-11-05";
    private final JdbcTemplate jdbc;
    private final boolean enabled;
    // Same algorithm, cost and prefix as the desktop browser's AuthService.
    private final BCryptPasswordEncoder passwords = new BCryptPasswordEncoder();

    public RegistrationService(JdbcTemplate jdbc, @Value("${app.registration-enabled:false}") boolean enabled) {
        this.jdbc = jdbc; this.enabled = enabled;
    }

    public boolean enabled() { return enabled; }

    public record Request(String username, String password, Boolean acceptedTerms, String policyVersion) {
        // Never let an accidental log statement expose credentials.
        @Override public String toString() { return "RegistrationRequest[redacted]"; }
    }

    @Transactional
    public Map<String, String> register(Request request) {
        if (!enabled) throw new RegistrationFailure(503, "公开注册尚未开放，请联系微信 15675826196 申请体验。");
        String username = request.username() == null ? "" : request.username().trim();
        String password = request.password() == null ? "" : request.password();
        if (!username.matches("[A-Za-z0-9][A-Za-z0-9_.-]{3,39}"))
            throw new RegistrationFailure(400, "账号需为 4–40 位字母、数字、下划线、点或短横线，并以字母或数字开头。");
        if (password.length() < 12 || password.getBytes(StandardCharsets.UTF_8).length > 72)
            throw new RegistrationFailure(400, "密码至少 12 位，且 UTF-8 编码不超过 72 字节。");
        if (!Boolean.TRUE.equals(request.acceptedTerms()) || !POLICY_VERSION.equals(request.policyVersion()))
            throw new RegistrationFailure(400, "请阅读并同意当前版本的使用说明和隐私说明。");
        if (jdbc.queryForObject("SELECT COUNT(*) FROM app_user WHERE username = ?", Integer.class, username) > 0)
            throw new RegistrationFailure(409, "该账号已存在，请更换账号或直接登录客户端。");

        // All four writes share one transaction. No existing tenant or permission can be chosen by the caller.
        String tenantId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        String hash = passwords.encode(password);
        jdbc.update("INSERT INTO tenant(id,name) VALUES(?,?)", tenantId, username + " 的工作空间");
        jdbc.update("INSERT INTO app_user(id,tenant_id,username,password_hash,display_name,owner,enabled) VALUES(?,?,?,?,?,TRUE,TRUE)",
                userId, tenantId, username, hash, username);
        jdbc.update("INSERT INTO wallet(tenant_id,balance_cents) VALUES(?,0)", tenantId);
        jdbc.update("INSERT INTO audit_event(id,tenant_id,actor_id,action,resource_id,detail) VALUES(?,?,?,?,?,?)",
                UUID.randomUUID().toString(), tenantId, userId, "account.register", userId,
                "{\"source\":\"official-web\",\"acceptedTerms\":true,\"policyVersion\":\"" + POLICY_VERSION + "\"}");
        return Map.of("id", userId, "username", username);
    }
}
