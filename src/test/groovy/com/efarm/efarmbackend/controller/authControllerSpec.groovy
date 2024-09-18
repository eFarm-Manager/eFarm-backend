package com.efarm.efarmbackend.controller

import com.efarm.efarmbackend.payload.request.LoginRequest
import com.efarm.efarmbackend.security.jwt.JwtUtils
import com.efarm.efarmbackend.security.services.UserDetailsImpl
import com.efarm.efarmbackend.service.AuthService
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

    def authenticationManager = Mock(AuthenticationManager)
    def userRepository = Mock(UserRepository)
    def roleRepository = Mock(RoleRepository)
    def farmRepository = Mock(FarmRepository)
    def addressRepository = Mock(AddressRepository)
    def activationCodeRepository = Mock(ActivationCodeRepository)
    def encoder = Mock(PasswordEncoder)
    def authService = Mock(AuthService)
    def jwtUtils = Mock(JwtUtils)

    @Subject
    AuthController authController = new AuthController(
            userRepository: userRepository,
            roleRepository: roleRepository,
            farmRepository: farmRepository,
            addressRepository: addressRepository,
            activationCodeRepository: activationCodeRepository,
            encoder: encoder,
            authService: authService,
            jwtUtils: jwtUtils,
            authenticationManager: authenticationManager
    )

    def setup() {
        SecurityContextHolder.clearContext()
    }

    @Unroll
    def "should authenticate user successfully"() {
        given:
        LoginRequest loginRequest = new LoginRequest(
                username: "user",
                password: "password"
        )

        UserDetailsImpl jwtUserDetails = Mock(UserDetailsImpl)
        jwtUserDetails.getId() >> 1
        jwtUserDetails.getUsername() >> "user"
        jwtUserDetails.getPassword() >> "password"
        jwtUserDetails.getEmail() >> "user@gmail.com"
        jwtUserDetails.getAuthorities() >> [new SimpleGrantedAuthority("ROLE_FARM_MANAGER")]


        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());

        Authentication auth = new UsernamePasswordAuthenticationToken(jwtUserDetails, null, jwtUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        ResponseCookie jwtCookie = ResponseCookie.from("jwtTokenName", "jwtTokenString")
                .path("/api")
                .httpOnly(true)
                .maxAge(Duration.ofSeconds(24 * 60 * 60))
                .build()

        authenticationManager.authenticate(authToken) >> auth
        jwtUtils.generateJwtCookie(jwtUserDetails) >> jwtCookie

        when:
        ResponseEntity result = authController.authenticateUser(loginRequest)
        println("result: ${result}")

        then:
        result.statusCodeValue == 200
        result.headers.getFirst(HttpHeaders.SET_COOKIE).contains("jwtTokenName=")
        result.headers.getFirst(HttpHeaders.SET_COOKIE).contains("Path=/api")
        result.headers.getFirst(HttpHeaders.SET_COOKIE).contains("Max-Age=86400")
        result.headers.getFirst(HttpHeaders.SET_COOKIE).contains("HttpOnly")
        result.body.id == 1
        result.body.username == "user"
        result.body.email == "user@gmail.com"
        result.body.roles.contains("ROLE_FARM_MANAGER")
    }

    @Unroll
    def "should fail to authenticate user due to password error"() {
        given:
        LoginRequest loginRequest = new LoginRequest(
                username: "user",
                password: "password"
        )

        authenticationManager.authenticate(_) >> { throw new BadCredentialsException("Bad credentials") }

        when:
        ResponseEntity<?> result = authController.authenticateUser(loginRequest)

        then:
        thrown(BadCredentialsException)
    }

    @Unroll
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