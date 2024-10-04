package com.efarm.efarmbackend.controller

import com.efarm.efarmbackend.security.jwt.JwtUtils
import com.efarm.efarmbackend.service.auth.AuthFacade
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.http.HttpStatus
import spock.lang.Specification
import spock.lang.Subject


class AuthControllerSpec extends Specification {

    def authFacade = Mock(AuthFacade)
    def jwtUtils = Mock(JwtUtils)

    @Subject
    AuthController authController = new AuthController(
            authFacade: authFacade,
            jwtUtils: jwtUtils
    )

    def setup() {
        SecurityContextHolder.clearContext()
        }
        
    def "should sign out user and return expected response"() {
        given:
        ResponseCookie cookie = ResponseCookie.from("jwtTokenName", "")
                .path("/api")
                .build()

        jwtUtils.getCleanJwtCookie() >> cookie

        when:
        ResponseEntity<?> result = authController.logoutUser()

        then:
        result.getStatusCode() == HttpStatus.OK
        result.headers.getFirst(HttpHeaders.SET_COOKIE) == "jwtTokenName=; Path=/api"
        result.body.message == "You've been signed out!"
    }
}