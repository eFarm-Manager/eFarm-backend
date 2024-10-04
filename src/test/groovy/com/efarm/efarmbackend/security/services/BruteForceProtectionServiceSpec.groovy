package com.efarm.efarmbackend.security.services

import spock.lang.Specification
import spock.lang.Subject

class BruteForceProtectionServiceSpec extends Specification {

    @Subject
    BruteForceProtectionService bruteForceProtectionService = new BruteForceProtectionService()

    def "should be blocked if at least 5 attempts"() {
        given:
        String username = "testUser"

        when:
        5.times {
            bruteForceProtectionService.loginFailed(username)
        }

        then:
        bruteForceProtectionService.attempts.get(username).isBlocked() == true
        bruteForceProtectionService.attempts.get(username).getAttempts() == 5
    } 

    def "should not be blocked if less than 5 attempts"() {
        given:
        String username = "testUser"

        when:
        4.times {
            bruteForceProtectionService.loginFailed(username)
        }

        then:
        bruteForceProtectionService.attempts.get(username).isBlocked() == false
        bruteForceProtectionService.attempts.get(username).getAttempts() == 4
    }

    def "should deny access for blocked user"() {
        given:
        String username = "testUser"
        5.times {
            bruteForceProtectionService.loginFailed(username)
        }

        when:
        boolean result = bruteForceProtectionService.isBlocked(username)

        then:
        result == true
    }

    def "should return false if user is blocked but block time has expired"() {
        given:
        String username = "testUser"
        5.times {
            bruteForceProtectionService.loginFailed(username)
        }
        bruteForceProtectionService.attempts.get(username).setBlockTime(System.currentTimeMillis() - (BruteForceProtectionService.BLOCK_TIME + 1000))

        when:
        boolean result = bruteForceProtectionService.isBlocked(username)

        then:
        result == false
    }

    def "should return false if user is not blocked"() {
        given:
        String username = "testUser"
        4.times {
            bruteForceProtectionService.loginFailed(username)
        }

        when:
        boolean result = bruteForceProtectionService.isBlocked(username)

        then:
        result == false
    }    

    def "should remove failed attempts after successful login"() {
        given:
        String username = "testUser"
        4.times {
            bruteForceProtectionService.loginFailed(username)
        }
        assert bruteForceProtectionService.attempts.containsKey(username) 

        when: 
        bruteForceProtectionService.loginSucceeded(username)

        then: 
        !bruteForceProtectionService.attempts.containsKey(username)
    }

    def "should do nothing if user had no failed attempts"() {
        given:
        String username = "testUser"
        assert !bruteForceProtectionService.attempts.containsKey(username)

        when: 
        bruteForceProtectionService.loginSucceeded(username)

        then: 
        !bruteForceProtectionService.attempts.containsKey(username)
    }
}