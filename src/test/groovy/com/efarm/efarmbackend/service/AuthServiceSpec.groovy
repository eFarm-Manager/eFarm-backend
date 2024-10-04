package com.efarm.efarmbackend.service

import com.efarm.efarmbackend.payload.request.LoginRequest
import com.efarm.efarmbackend.model.farm.ActivationCode
import com.efarm.efarmbackend.model.farm.Farm
import com.efarm.efarmbackend.model.user.ERole
import com.efarm.efarmbackend.model.user.Role
import com.efarm.efarmbackend.model.user.User
import com.efarm.efarmbackend.payload.request.SignupFarmRequest
import com.efarm.efarmbackend.payload.request.UpdateActivationCodeRequest
import com.efarm.efarmbackend.security.services.BruteForceProtectionService;
import com.efarm.efarmbackend.payload.request.SignupRequest
import com.efarm.efarmbackend.payload.response.MessageResponse
import com.efarm.efarmbackend.repository.farm.ActivationCodeRepository
import com.efarm.efarmbackend.repository.farm.AddressRepository
import com.efarm.efarmbackend.repository.farm.FarmRepository
import com.efarm.efarmbackend.repository.user.UserRepository
import com.efarm.efarmbackend.security.services.UserDetailsImpl
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.context.SecurityContextImpl
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import java.time.LocalDate

class AuthServiceSpec extends Specification {

    def authenticationManager = Mock(AuthenticationManager)
    def bruteForceProtectionService = Mock(BruteForceProtectionService)

    @Subject
    AuthService authService = new AuthService(
            authenticationManager: authenticationManager,
            bruteForceProtectionService: bruteForceProtectionService
    )

    def setup() {
        SecurityContextHolder.clearContext()
    }
    
    def "should authenticate user by login request"() {
        given:
        LoginRequest loginRequest = new LoginRequest(username: "user", password: "password")
        UserDetailsImpl userDetails = Mock(UserDetailsImpl)
        userDetails.getAuthorities() >> [new SimpleGrantedAuthority("ROLE_FARM_MANAGER")]
        
        bruteForceProtectionService.isBlocked(loginRequest.getUsername()) >> false
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
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
                username: "user",
                password: "password"
        )
        Authentication authentication = Mock(Authentication)
        UserDetailsImpl userDetails = Mock(UserDetailsImpl)

        bruteForceProtectionService.isBlocked(loginRequest.getUsername()) >> false
        authenticationManager.authenticate(_ as UsernamePasswordAuthenticationToken) >> { throw new BadCredentialsException("Bad credentials") }

        when:
        authService.authenticateUserByLoginRequest(loginRequest)

        then:
        thrown(RuntimeException)
    }

    def "should get too many attempts and block user"() {
        given:
        LoginRequest loginRequest = new LoginRequest(
            username: "user",
            password: "password"
        )
        bruteForceProtectionService.isBlocked(loginRequest.getUsername()) >> true

        when:
        authService.authenticateUserByLoginRequest(loginRequest)

        then:
        thrown(RuntimeException)
    }

    def "authenticate user by update code request"() {
        given:
        UpdateActivationCodeRequest updateActivationCodeRequest = new UpdateActivationCodeRequest(username: "user", password: "password",newActivationCode: "newActivationCode")
        UserDetailsImpl userDetails = Mock(UserDetailsImpl)
        userDetails.getAuthorities() >> [new SimpleGrantedAuthority("ROLE_FARM_MANAGER")]
        
        bruteForceProtectionService.isBlocked(updateActivationCodeRequest.getUsername()) >> false
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(updateActivationCodeRequest.getUsername(), updateActivationCodeRequest.getPassword());

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authenticationManager.authenticate(authToken) >> authentication

        when:
        UserDetailsImpl result = authService.authenticateUserByUpdateCodeRequest(updateActivationCodeRequest)

        then:
        result == userDetails
        SecurityContextHolder.getContext().getAuthentication() == authentication
    }

    def "wrong credentials when update code"() {
        given:
        UpdateActivationCodeRequest updateActivationCodeRequest = new UpdateActivationCodeRequest(username: "user", password: "password",newActivationCode: "newActivationCode")

        Authentication authentication = Mock(Authentication)
        UserDetailsImpl userDetails = Mock(UserDetailsImpl)

        bruteForceProtectionService.isBlocked(updateActivationCodeRequest.getUsername()) >> false
        authenticationManager.authenticate(_ as UsernamePasswordAuthenticationToken) >> { throw new BadCredentialsException("Bad credentials") }

        when:
        authService.authenticateUserByLoginRequest(updateActivationCodeRequest)

        then:
        thrown(RuntimeException)
    }

    def "should get too many attempts when update code and block user"() {
        given:
        UpdateActivationCodeRequest updateActivationCodeRequest = new UpdateActivationCodeRequest(username: "user", password: "password",newActivationCode: "newActivationCode")

        bruteForceProtectionService.isBlocked(updateActivationCodeRequest.getUsername()) >> true

        when:
        authService.authenticateUserByLoginRequest(updateActivationCodeRequest)

        then:
        thrown(RuntimeException)
    }

    def "should correctly assume owner role"() {
        given:
        UserDetailsImpl userDetails = Mock(UserDetailsImpl)
        userDetails.getAuthorities() >> [new SimpleGrantedAuthority("ROLE_FARM_OWNER")]
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication) 

        when:
        boolean result = authService.hasCurrentUserRole("ROLE_FARM_OWNER")

        then:
        result == true
    }    

    def "should correctly assume manager role"() {
        given:
        UserDetailsImpl userDetails = Mock(UserDetailsImpl)
        userDetails.getAuthorities() >> [new SimpleGrantedAuthority("ROLE_FARM_MANAGER")]
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication) 

        when:
        boolean result = authService.hasCurrentUserRole("ROLE_FARM_MANAGER")

        then:
        result == true
    }

    def "should correctly assume operator role"() {
        given:
        UserDetailsImpl userDetails = Mock(UserDetailsImpl)
        userDetails.getAuthorities() >> [new SimpleGrantedAuthority("ROLE_FARM_EQUIPMENT_OPERATOR")]
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication) 

        when:
        boolean result = authService.hasCurrentUserRole("ROLE_FARM_EQUIPMENT_OPERATOR")

        then:
        result == true
    }

    def "should correctly assume not same role"() {
        given:
        UserDetailsImpl userDetails = Mock(UserDetailsImpl)
        userDetails.getAuthorities() >> [new SimpleGrantedAuthority("ROLE_FARM_MANAGER")]
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication) 

        when:
        boolean result = authService.hasCurrentUserRole("ROLE_FARM_OWNER")

        then:
        result == false
    }

    def "should correctly assume role with null authentication"() {
        given:
        SecurityContextHolder.getContext().setAuthentication(null) 

        when:
        boolean result = authService.hasCurrentUserRole("ROLE_FARM_OWNER")

        then:
        result == false
    }
}