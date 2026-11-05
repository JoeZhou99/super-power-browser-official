package com.superpower.official;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RegistrationController {
    private final RegistrationService registration;
    public RegistrationController(RegistrationService registration) { this.registration = registration; }

    @GetMapping("/health") public Map<String, String> health() {
        return Map.of("status", "UP", "service", "super-power-browser-official-server");
    }
    @GetMapping("/auth/registration-status") public Map<String, Object> status() {
        return Map.of("enabled", registration.enabled(), "policyVersion", RegistrationService.POLICY_VERSION);
    }
    @PostMapping(value = "/auth/register", consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> register(@RequestBody RegistrationService.Request request) {
        return registration.register(request);
    }
}
