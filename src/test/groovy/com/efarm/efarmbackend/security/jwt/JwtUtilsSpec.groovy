package com.efarm.efarmbackend.security.jwt

import com.efarm.efarmbackend.security.services.UserDetailsImpl
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseCookie
import spock.lang.Specification

class JwtUtilsSpec extends Specification {

    String jwtCookie = 'jwtToken'
    String jwtSecret = '123hbfaegi32qgf7r6gh87wefawoyrg763ihr79g37hfo8a73riau'
    int jwtExpirationMs = 86400000


    //checking that if cookie is present then the jwt is returned correctly
    def "should return JWT from cookies when cookie is present - getJwtFromCookies"() {
        given:
        String jwtCookie = 'jwtToken'
        HttpServletRequest request = Mock(HttpServletRequest)
        Cookie cookie = new Cookie(jwtCookie, 'sampleJwtToken')
        request.getCookies() >> [cookie]

        JwtUtils jwtUtils = new JwtUtils()
        java.lang.reflect.Field fieldCookie = JwtUtils.class.getDeclaredField('jwtCookie')
        fieldCookie.setAccessible(true)
        fieldCookie.set(jwtUtils, jwtCookie)

        when:
        String jwtToken = jwtUtils.getJwtFromCookies(request)

        then:
        jwtToken == 'sampleJwtToken'
    }

    //checks that if no cookie present then there is no jwt token
    def "should return null when cookie is not present - getJwtFromCookies"() {
        given:
        HttpServletRequest request = Mock(HttpServletRequest)
        request.getCookies() >> null

        when:
        String jwtToken = new JwtUtils().getJwtFromCookies(request)

        then:
        jwtToken == null
    }

    //checks if request is null then IllegalArgument Exception is thrown
    def "should throw IllegalArgumentException when request null - getJwtFromCookies"() {
        given:
        HttpServletRequest request = null

        when:
        new JwtUtils().getJwtFromCookies(request)

        then:
        thrown(IllegalArgumentException)
    }

    //checks if jwt cookie is correctly generated with user details (username)
    def "should generate JWT cookie with correct properties - generateJwtCookie"() {
        given:
        UserDetailsImpl userPrincipal = Mock(UserDetailsImpl)
        userPrincipal.getUsername() >> 'user1'

        JwtUtils jwtUtils = new JwtUtils()
        java.lang.reflect.Field fieldCookie = JwtUtils.class.getDeclaredField('jwtCookie')
        fieldCookie.setAccessible(true)
        fieldCookie.set(jwtUtils, jwtCookie)
        java.lang.reflect.Field fieldSecret = JwtUtils.class.getDeclaredField('jwtSecret')
        fieldSecret.setAccessible(true)
        fieldSecret.set(jwtUtils, jwtSecret)
        java.lang.reflect.Field fieldExpiration = JwtUtils.class.getDeclaredField('jwtExpirationMs')
        fieldExpiration.setAccessible(true)
        fieldExpiration.set(jwtUtils, jwtExpirationMs)

        when:
        ResponseCookie cookie = jwtUtils.generateJwtCookie(userPrincipal)

        then:
        cookie.getName() == jwtCookie
        def tokenParts = cookie.getValue().split('\\.')
        tokenParts.size() == 3 // Header, Payload, Signature
        tokenParts[0] ==~ /^[A-Za-z0-9-_]+$/
        tokenParts[1] ==~ /^[A-Za-z0-9-_]+$/
        tokenParts[2] ==~ /^[A-Za-z0-9-_]+$/
        cookie.getPath() == '/api'
        cookie.getMaxAge() == java.time.Duration.ofSeconds(24 * 60 * 60)
        cookie.isHttpOnly()
    }

    //checks that generating jwt cookie with no user details throws NullPointer Exception
    def "should throw null NullPointer Exception when no user principal - generateJwtCookie"() {
        given:
        JwtUtils jwtUtils = new JwtUtils()
        java.lang.reflect.Field fieldCookie = JwtUtils.class.getDeclaredField('jwtCookie')
        fieldCookie.setAccessible(true)
        fieldCookie.set(jwtUtils, jwtCookie)
        java.lang.reflect.Field fieldSecret = JwtUtils.class.getDeclaredField('jwtSecret')
        fieldSecret.setAccessible(true)
        fieldSecret.set(jwtUtils, jwtSecret)

        when:
        jwtUtils.generateJwtCookie(null)

        then:
        thrown(NullPointerException)
    }

    //checks that jwt cookie is different for 2 different users
    def "should generate JWT cookie with different user principals - generateJwtCookie"() {
        given:
        UserDetailsImpl userPrincipal1 = Mock(UserDetailsImpl)
        userPrincipal1.getUsername() >> 'user1'

        UserDetailsImpl userPrincipal2 = Mock(UserDetailsImpl)
        userPrincipal2.getUsername() >> 'user2'

        JwtUtils jwtUtils = new JwtUtils()
        java.lang.reflect.Field fieldCookie = JwtUtils.class.getDeclaredField('jwtCookie')
        fieldCookie.setAccessible(true)
        fieldCookie.set(jwtUtils, jwtCookie)
        java.lang.reflect.Field fieldSecret = JwtUtils.class.getDeclaredField('jwtSecret')
        fieldSecret.setAccessible(true)
        fieldSecret.set(jwtUtils, jwtSecret)

        when:
        ResponseCookie cookie1 = jwtUtils.generateJwtCookie(userPrincipal1)
        ResponseCookie cookie2 = jwtUtils.generateJwtCookie(userPrincipal2)

        then:
        cookie1.getValue() != cookie2.getValue()
    }

    // checks that jwt cookie is generated as clean meaning ""
    def "should generate clean JWT cookie - getCleanJwtCookie"() {
        given:
        JwtUtils jwtUtils = new JwtUtils()
        java.lang.reflect.Field fieldCookie = JwtUtils.class.getDeclaredField('jwtCookie')
        fieldCookie.setAccessible(true)
        fieldCookie.set(jwtUtils, jwtCookie)

        when:
        ResponseCookie cookie = jwtUtils.getCleanJwtCookie()

        then:
        cookie.getName() == jwtCookie
        cookie.getValue() == ""
        cookie.getPath() == '/api'
    }

    //checks that username is returned from valid jwt token
    def "should return username from valid JWT token - getUserNameFromJwtToken"() {
        given:
        int jwtExpiration = 10000
        String username = 'user1'

        JwtUtils jwtUtils = new JwtUtils()
        java.lang.reflect.Field fieldSecret = JwtUtils.class.getDeclaredField('jwtSecret')
        fieldSecret.setAccessible(true)
        fieldSecret.set(jwtUtils, jwtSecret)
        java.lang.reflect.Field fieldExpiration = JwtUtils.class.getDeclaredField('jwtExpirationMs')
        fieldExpiration.setAccessible(true)
        fieldExpiration.set(jwtUtils, jwtExpiration)

        String token = jwtUtils.generateTokenFromUsername(username)

        when:
        String result = jwtUtils.getUserNameFromJwtToken(token)

        then:
        result == username
    }

    //checks that expired token makes it so that there is ExpiredJwt Exception
    def "should handle expired JWT token - getUserNameFromJwtToken"() {
        given:
        int jwtExpiration = 1
        String username = 'user1'

        JwtUtils jwtUtils = new JwtUtils()
        java.lang.reflect.Field fieldSecret = JwtUtils.class.getDeclaredField('jwtSecret')
        fieldSecret.setAccessible(true)
        fieldSecret.set(jwtUtils, jwtSecret)
        java.lang.reflect.Field fieldExpiration = JwtUtils.class.getDeclaredField('jwtExpirationMs')
        fieldExpiration.setAccessible(true)
        fieldExpiration.set(jwtUtils, jwtExpiration)

        String token = jwtUtils.generateTokenFromUsername(username)

        when:
        jwtUtils.getUserNameFromJwtToken(token)

        then:
        thrown(io.jsonwebtoken.ExpiredJwtException)
    }

    //checks that malformed jwt token makes it so that there is MalformedJwt Exception
    def "should handle malformed JWT token - getUserNameFromJwtToken"() {
        given:
        String malformedToken = 'malformed.token'

        JwtUtils jwtUtils = new JwtUtils()
        java.lang.reflect.Field fieldSecret = JwtUtils.class.getDeclaredField('jwtSecret')
        fieldSecret.setAccessible(true)
        fieldSecret.set(jwtUtils, jwtSecret)

        when:
        jwtUtils.getUserNameFromJwtToken(malformedToken)

        then:
        thrown(io.jsonwebtoken.MalformedJwtException)
    }

    //checks that empty jwt token makes it so that there is IllegalArgument Exception
    def "should handle empty JWT token - getUserNameFromJwtToken"() {
        given:
        String emptyToken = ''

        JwtUtils jwtUtils = new JwtUtils()
        java.lang.reflect.Field fieldSecret = JwtUtils.class.getDeclaredField('jwtSecret')
        fieldSecret.setAccessible(true)
        fieldSecret.set(jwtUtils, jwtSecret)

        when:
        jwtUtils.getUserNameFromJwtToken(emptyToken)

        then:
        thrown(IllegalArgumentException)
    }

    //checks validation of valid token
    def "should return true for valid JWT token - validateJwtToken"() {
        given:
        int jwtExpiration = 10000
        String username = 'user1'

        JwtUtils jwtUtils = new JwtUtils()
        java.lang.reflect.Field fieldSecret = JwtUtils.class.getDeclaredField('jwtSecret')
        fieldSecret.setAccessible(true)
        fieldSecret.set(jwtUtils, jwtSecret)
        java.lang.reflect.Field fieldExpiration = JwtUtils.class.getDeclaredField('jwtExpirationMs')
        fieldExpiration.setAccessible(true)
        fieldExpiration.set(jwtUtils, jwtExpiration)

        String validToken = jwtUtils.generateTokenFromUsername(username)

        when:
        boolean isValid = jwtUtils.validateJwtToken(validToken)

        then:
        isValid == true
    }

    //checks validation of expired token
    def "should return false for expired JWT token - validateJwtToken"() {
        given:
        int jwtExpiration = 1
        String username = 'user1'

        JwtUtils jwtUtils = new JwtUtils()
        java.lang.reflect.Field fieldSecret = JwtUtils.class.getDeclaredField('jwtSecret')
        fieldSecret.setAccessible(true)
        fieldSecret.set(jwtUtils, jwtSecret)
        java.lang.reflect.Field fieldExpiration = JwtUtils.class.getDeclaredField('jwtExpirationMs')
        fieldExpiration.setAccessible(true)
        fieldExpiration.set(jwtUtils, jwtExpiration)

        String expiredToken = jwtUtils.generateTokenFromUsername(username)

        when:
        boolean isValid = jwtUtils.validateJwtToken(expiredToken)

        then:
        isValid == false
    }

    //checks validation of malformed token
    def "should return false for malformed JWT token - validateJwtToken"() {
        given:
        String malformedToken = 'malformed.token'

        JwtUtils jwtUtils = new JwtUtils()
        java.lang.reflect.Field fieldSecret = JwtUtils.class.getDeclaredField('jwtSecret')
        fieldSecret.setAccessible(true)
        fieldSecret.set(jwtUtils, jwtSecret)

        when:
        boolean isValid = jwtUtils.validateJwtToken(malformedToken)

        then:
        isValid == false
    }

    //checks validation of unsupported token
    def "should return false for unsupported JWT token - validateJwtToken"() {
        given:
        JwtUtils jwtUtils = new JwtUtils()
        java.lang.reflect.Field fieldSecret = JwtUtils.class.getDeclaredField('jwtSecret')
        fieldSecret.setAccessible(true)
        fieldSecret.set(jwtUtils, jwtSecret)

        String unsupportedToken = "eyJhbGciOiJub25lIn0.eyJzdWIiOiJ1c2VyMSJ9.dwadz" // Fake JWT with "alg": "none"

        when:
        boolean isValid = jwtUtils.validateJwtToken(unsupportedToken)

        then:
        isValid == false
    }

    //checks validation of empty token
    def "should return false for empty JWT token - validateJwtToken"() {
        given:
        String emptyToken = ''

        JwtUtils jwtUtils = new JwtUtils()
        java.lang.reflect.Field fieldSecret = JwtUtils.class.getDeclaredField('jwtSecret')
        fieldSecret.setAccessible(true)
        fieldSecret.set(jwtUtils, jwtSecret)

        when:
        boolean isValid = jwtUtils.validateJwtToken(emptyToken)

        then:
        isValid == false
    }
}
