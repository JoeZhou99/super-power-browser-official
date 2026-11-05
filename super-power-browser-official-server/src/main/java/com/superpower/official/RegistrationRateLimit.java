package com.superpower.official;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Bounded per-process limiter; the public reverse proxy also applies a shared ingress limit. */
@Component
public class RegistrationRateLimit {
    private record Window(long expiresAt, int count) {}
    private final Map<String, Window> addresses = new HashMap<>();
    private Window global = new Window(0, 0);
    private final int addressLimit, globalLimit;
    public RegistrationRateLimit(@Value("${app.rate-limit.per-address:10}") int addressLimit,
            @Value("${app.rate-limit.global-per-minute:60}") int globalLimit) {
        if (addressLimit < 1 || globalLimit < 1) throw new IllegalArgumentException("Rate limits must be positive");
        this.addressLimit = addressLimit; this.globalLimit = globalLimit;
    }
    public synchronized boolean allow(String address) {
        long now = System.currentTimeMillis();
        addresses.entrySet().removeIf(entry -> entry.getValue().expiresAt() <= now);
        if (global.expiresAt() <= now) global = new Window(now + 60_000, 0);
        Window local = addresses.get(address);
        if (global.count() >= globalLimit || (local != null && local.count() >= addressLimit)) return false;
        if (local == null && addresses.size() >= 10_000) return false;
        global = new Window(global.expiresAt(), global.count() + 1);
        addresses.put(address, new Window(local == null ? now + 900_000 : local.expiresAt(), local == null ? 1 : local.count() + 1));
        return true;
    }
}
