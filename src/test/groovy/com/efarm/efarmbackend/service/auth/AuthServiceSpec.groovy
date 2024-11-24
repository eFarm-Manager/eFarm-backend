package com.efarm.efarmbackend.service

import com.efarm.efarmbackend.payload.request.auth.LoginRequest
import com.efarm.efarmbackend.payload.request.auth.UpdateActivationCodeRequest
import com.efarm.efarmbackend.security.services.BruteForceProtectionService
import com.efarm.efarmbackend.security.services.UserDetailsImpl
import com.efarm.efarmbackend.service.auth.AuthService
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.BadCredentialsException
import spock.lang.Specification
import spock.lang.Subject

class AuthServiceSpec extends Specification {

    def authenticationManager = Mock(AuthenticationManager)
    def bruteForceProtectionService = Mock(BruteForceProtectionService)

    @Subject
    AuthService authService = new AuthService(
            authenticationManager,
            bruteForceProtectionService
    )

    def setup() {
        SecurityContextHolder.clearContext()
    }

    def "should authenticate user by login request"() {
        given:
        LoginRequest loginRequest = new LoginRequest(username: 'user', password: 'password')
        UserDetailsImpl userDetails = Mock(UserDetailsImpl)
        userDetails.getAuthorities() >> [new SimpleGrantedAuthority('ROLE_FARM_MANAGER')]

        bruteForceProtectionService.isBlocked(loginRequest.getUsername()) >> false
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        authenticationManager.authenticate(authToken) >> authentication

        when:
        UserDetailsImpl result = authService.authenticateUserByLoginRequest(loginRequest)

        then:
        result == userDetails
        SecurityContextHolder.getContext().getAuthentication() == authentication
    }

    def "wrong credentials when login"() {
        given:
        LoginRequest loginRequest = new LoginRequest(
                username: 'user',
                password: 'password'
        )
        bruteForceProtectionService.isBlocked(loginRequest.getUsername()) >> false
        authenticationManager.authenticate(_ as UsernamePasswordAuthenticationToken) >> { throw new BadCredentialsException('Bad credentials') }

        when:
        authService.authenticateUserByLoginRequest(loginRequest)

        then:
        thrown(RuntimeException)
    }

    def "should get too many attempts and block user"() {
        given:
        LoginRequest loginRequest = new LoginRequest(
                username: 'user',
                password: 'password'
        )
        bruteForceProtectionService.isBlocked(loginRequest.getUsername()) >> true

        when:
        authService.authenticateUserByLoginRequest(loginRequest)

        then:
        thrown(RuntimeException)
    }

    def "authenticate user by update code request"() {
        given:
        UpdateActivationCodeRequest updateActivationCodeRequest = new UpdateActivationCodeRequest(username: 'user', password: 'password', newActivationCode: 'newActivationCode')
        UserDetailsImpl userDetails = Mock(UserDetailsImpl)
        userDetails.getAuthorities() >> [new SimpleGrantedAuthority('ROLE_FARM_MANAGER')]

        bruteForceProtectionService.isBlocked(updateActivationCodeRequest.getUsername()) >> false
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(updateActivationCodeRequest.getUsername(), updateActivationCodeRequest.getPassword())

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        authenticationManager.authenticate(authToken) >> authentication

        when:
        UserDetailsImpl result = authService.authenticateUserByUpdateCodeRequest(updateActivationCodeRequest)

        then:
        result == userDetails
        SecurityContextHolder.getContext().getAuthentication() == authentication
    }

    def "wrong credentials when update code"() {
        given:
        UpdateActivationCodeRequest updateActivationCodeRequest = new UpdateActivationCodeRequest(username: 'user', password: 'password', newActivationCode: 'newActivationCode')

        bruteForceProtectionService.isBlocked(updateActivationCodeRequest.getUsername()) >> false
        authenticationManager.authenticate(_ as UsernamePasswordAuthenticationToken) >> { throw new BadCredentialsException('Bad credentials') }

        when:
        authService.authenticateUserByLoginRequest(updateActivationCodeRequest)

        then:
        thrown(RuntimeException)
    }

    def "should get too many attempts when update code and block user"() {
        given:
        UpdateActivationCodeRequest updateActivationCodeRequest = new UpdateActivationCodeRequest(username: 'user', password: 'password', newActivationCode: 'newActivationCode')

        bruteForceProtectionService.isBlocked(updateActivationCodeRequest.getUsername()) >> true

        when:
        authService.authenticateUserByLoginRequest(updateActivationCodeRequest)

        then:
        thrown(RuntimeException)
    }

    def "should correctly assume owner role"() {
        given:
        UserDetailsImpl userDetails = Mock(UserDetailsImpl)
        userDetails.getAuthorities() >> [new SimpleGrantedAuthority('ROLE_FARM_OWNER')]

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        SecurityContextHolder.getContext().setAuthentication(authentication)

        when:
        boolean result = authService.hasCurrentUserRole('ROLE_FARM_OWNER')

        then:
        result == true
    }

    def "should correctly assume manager role"() {
        given:
        UserDetailsImpl userDetails = Mock(UserDetailsImpl)
        userDetails.getAuthorities() >> [new SimpleGrantedAuthority('ROLE_FARM_MANAGER')]

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        SecurityContextHolder.getContext().setAuthentication(authentication)

        when:
        boolean result = authService.hasCurrentUserRole('ROLE_FARM_MANAGER')

        then:
        result == true
    }

    def "should correctly assume operator role"() {
        given:
        UserDetailsImpl userDetails = Mock(UserDetailsImpl)
        userDetails.getAuthorities() >> [new SimpleGrantedAuthority('ROLE_FARM_EQUIPMENT_OPERATOR')]

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        SecurityContextHolder.getContext().setAuthentication(authentication)

        when:
        boolean result = authService.hasCurrentUserRole('ROLE_FARM_EQUIPMENT_OPERATOR')

        then:
        result == true
    }

    def "should correctly assume not same role"() {
        given:
        UserDetailsImpl userDetails = Mock(UserDetailsImpl)
        userDetails.getAuthorities() >> [new SimpleGrantedAuthority('ROLE_FARM_MANAGER')]

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        SecurityContextHolder.getContext().setAuthentication(authentication)

        when:
        boolean result = authService.hasCurrentUserRole('ROLE_FARM_OWNER')

        then:
        result == false
    }

    def "should correctly assume role with null authentication"() {
        given:
        SecurityContextHolder.getContext().setAuthentication(null)

        when:
        boolean result = authService.hasCurrentUserRole('ROLE_FARM_OWNER')

        then:
        result == false
    }

}
