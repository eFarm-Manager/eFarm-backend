package com.efarm.efarmbackend.security.services;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class BruteForceProtectionService {

    private final long BLOCK_TIME = TimeUnit.MINUTES.toMillis(15);

    private static final Logger logger = LoggerFactory.getLogger(BruteForceProtectionService.class);

    private Map<String, LoginAttempt> attempts = new HashMap<>();

    public void loginFailed(String username) {
        LoginAttempt attempt = attempts.getOrDefault(username, new LoginAttempt());
        attempt.incrementAttempts();
        int MAX_ATTEMPTS = 5;
        if (attempt.getAttempts() >= MAX_ATTEMPTS) {
            attempt.setBlocked(true);
            attempt.setBlockTime(System.currentTimeMillis());
            logger.info("Blocked user : {}, too many failed attempts", username);
        }
        attempts.put(username, attempt);
    }

    public boolean isBlocked(String username) {
        LoginAttempt attempt = attempts.get(username);
        if (attempt != null && attempt.isBlocked()) {
            if (System.currentTimeMillis() - attempt.getBlockTime() > BLOCK_TIME) {
                attempts.remove(username);
                return false;
            }
            return true;
        }
        return false;
    }

    public void loginSucceeded(String username) {
        attempts.remove(username);
    }

    @Getter
    private static class LoginAttempt {
        private int attempts = 0;

        @Setter
        private boolean blocked = false;
        @Setter
        private long blockTime = 0;

        public void incrementAttempts() {
            this.attempts++;
        }
    }
}
