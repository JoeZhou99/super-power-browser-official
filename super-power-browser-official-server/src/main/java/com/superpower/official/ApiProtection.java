package com.superpower.official;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ApiProtection extends OncePerRequestFilter {
    private final RegistrationRateLimit limiter;
    public ApiProtection(RegistrationRateLimit limiter) { this.limiter = limiter; }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "same-origin");
        if ("/api/auth/register".equals(request.getRequestURI()) && "POST".equals(request.getMethod())) {
            if (request.getContentLengthLong() < 0) { reject(response, 411, "请求需要声明内容长度。"); return; }
            if (request.getContentLengthLong() > 8192) { reject(response, 413, "提交内容过长。"); return; }
            if (!limiter.allow(request.getRemoteAddr())) {
                response.setHeader("Retry-After", "900");
                reject(response, 429, "操作较频繁，请稍后再试。"); return;
            }
        }
        chain.doFilter(request, response);
    }
    private void reject(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status); response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
