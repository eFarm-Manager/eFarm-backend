package com.efarm.efarmbackend.controller

import com.efarm.efarmbackend.payload.request.LoginRequest
import com.efarm.efarmbackend.security.jwt.JwtUtils
import com.efarm.efarmbackend.security.services.UserDetailsImpl
import com.efarm.efarmbackend.service.facades.AuthFacade
import com.efarm.efarmbackend.repository.user.UserRepository
import com.efarm.efarmbackend.repository.user.RoleRepository
import com.efarm.efarmbackend.repository.farm.FarmRepository
import com.efarm.efarmbackend.repository.farm.AddressRepository
import com.efarm.efarmbackend.repository.farm.ActivationCodeRepository
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import java.time.Duration
import org.springframework.security.authentication.BadCredentialsException


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
        result.statusCodeValue == 200
        result.headers.getFirst(HttpHeaders.SET_COOKIE) == "jwtTokenName=; Path=/api"
        result.body.message == "You've been signed out!"
    }
}