package com.efarm.efarmbackend.controller

import com.efarm.efarmbackend.model.farm.ActivationCode
import com.efarm.efarmbackend.model.farm.Farm
import com.efarm.efarmbackend.model.user.Role
import com.efarm.efarmbackend.model.user.ERole
import com.efarm.efarmbackend.model.user.User
import com.efarm.efarmbackend.payload.request.auth.LoginRequest
import com.efarm.efarmbackend.security.jwt.JwtUtils
import com.efarm.efarmbackend.security.services.UserDetailsImpl
import com.efarm.efarmbackend.service.auth.AuthFacade
import com.efarm.efarmbackend.service.farm.ActivationCodeService
import org.springframework.security.core.authority.SimpleGrantedAuthority
import com.efarm.efarmbackend.service.auth.AuthService
import com.efarm.efarmbackend.service.farm.FarmService
import com.efarm.efarmbackend.service.user.UserService
import com.efarm.efarmbackend.service.ValidationRequestService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.http.ResponseCookie
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.validation.BindingResult
import spock.lang.Specification
import spock.lang.Subject
import java.time.LocalDate
import java.nio.file.AccessDeniedException
import com.efarm.efarmbackend.exception.UnauthorizedException
import com.efarm.efarmbackend.payload.request.auth.*
import java.time.Duration
import java.time.temporal.ChronoUnit

class AuthControllerSpec extends Specification {

    def authFacade = Mock(AuthFacade)
    def jwtUtils = Mock(JwtUtils)
    def userService = Mock(UserService)
    def activationCodeService = Mock(ActivationCodeService)
    def farmService = Mock(FarmService)
    def authService = Mock(AuthService)
    def validationRequestService = Mock(ValidationRequestService)

    @Subject
    AuthController authController = new AuthController(
            authFacade: authFacade,
            jwtUtils: jwtUtils,
            userService: userService,
            activationCodeService: activationCodeService,
            farmService: farmService,
            authService: authService,
            validationRequestService: validationRequestService
    )

    def setup() {
        SecurityContextHolder.clearContext()
    }
    /*
        login
    */

    def "should authenticate user properly"() {
        given:
        LoginRequest loginRequest = new LoginRequest(
                username: 'user',
                password: 'password'
        )
        Farm farm = Mock(Farm)

        Role role = Mock(Role)
        role.getName() >> ERole.ROLE_FARM_OWNER

        BindingResult bindingResult = Mock(BindingResult)
        bindingResult.hasErrors() >> false

        UserDetailsImpl userDetails = Mock(UserDetailsImpl)
        userDetails.getId() >> 1
        userDetails.getUsername() >> 'user'
        userDetails.getEmail() >> 'user@gmail.com'
        userDetails.getAuthorities() >> [new SimpleGrantedAuthority('ROLE_FARM_OWNER')]
        User user = Mock(User)
        user.getId() >> 1
        user.getIsActive() >> true
        user.getFarm() >> farm
        user.getRole() >> role
        List<String> roles = ['ROLE_FARM_OWNER']

        validationRequestService.validateRequestWithException(bindingResult) >> { }
        authService.authenticateUserByLoginRequest(loginRequest) >> userDetails
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        SecurityContextHolder.getContext().setAuthentication(auth)
        userService.getLoggedUserRoles(userDetails) >> roles
        userService.getActiveUserById(userDetails) >> Optional.of(user)
        userService.getUserFarmById(Long.valueOf(userDetails.getId())) >> farm

        farmService.checkFarmDeactivation(farm, role) >> null
        activationCodeService.generateExpireCodeInfo(userDetails, farm, roles) >> null

        ResponseCookie jwtCookie = ResponseCookie.from('jwtTokenName', 'jwtTokenString')
                .path('/api')
                .httpOnly(true)
                .maxAge(Duration.ofSeconds(24 * 60 * 60))
                .build()
        jwtUtils.generateJwtCookie(userDetails) >> jwtCookie

        when:
        ResponseEntity<?> response = authController.authenticateUser(loginRequest, bindingResult)

        then:
        response.getStatusCode() == HttpStatus.OK
        response.headers.getFirst(HttpHeaders.SET_COOKIE).contains('jwtTokenName=')
        response.headers.getFirst(HttpHeaders.SET_COOKIE).contains('Path=/api')
        response.headers.getFirst(HttpHeaders.SET_COOKIE).contains('Max-Age=86400')
        response.headers.getFirst(HttpHeaders.SET_COOKIE).contains('HttpOnly')
        response.body.id == 1
        response.body.username == 'user'
        response.body.email == 'user@gmail.com'
        response.body.roles.contains('ROLE_FARM_OWNER')
    }

    def "should return unauthorized for inactive user"() {
        given:
        LoginRequest loginRequest = new LoginRequest(username: 'inactiveUser', password: 'password')

        UserDetailsImpl userDetails = Mock(UserDetailsImpl)
        userDetails.getId() >> 2
        userDetails.getUsername() >> 'inactiveUser'
        List<String> roles = ['ROLE_FARM_MANAGER']

        User user = Mock(User)
        user.getId() >> 2
        user.getIsActive() >> false
        user.getFarm() >> Mock(Farm)
        BindingResult bindingResult = Mock(BindingResult)
        bindingResult.hasErrors() >> false

        validationRequestService.validateRequestWithException(bindingResult) >> { }
        authService.authenticateUserByLoginRequest(loginRequest) >> userDetails
        userService.getLoggedUserRoles(userDetails) >> roles
        userService.getActiveUserById(userDetails) >> { throw new RuntimeException('Użytkownik jest nieaktywny!') }

        when:
        ResponseEntity<?> response = authController.authenticateUser(loginRequest, bindingResult)

        then:
        response.getStatusCode() == HttpStatus.UNAUTHORIZED
        response.body.message == 'Użytkownik jest nieaktywny!'
    }

    def "should return unauthorized for wrong credentials"() {
        given:
        LoginRequest loginRequest = new LoginRequest(username: 'wrongUser', password: 'wrongPassword')
        BindingResult bindingResult = Mock(BindingResult)
        bindingResult.hasErrors() >> false

        validationRequestService.validateRequestWithException(bindingResult) >> { }
        authService.authenticateUserByLoginRequest(loginRequest) >> { throw new UnauthorizedException('Nieprawidłowe dane logowania') }

        when:
        ResponseEntity<?> response = authController.authenticateUser(loginRequest, bindingResult)

        then:
        response.getStatusCode() == HttpStatus.UNAUTHORIZED
        response.body.message == 'Nieprawidłowe dane logowania'
    }

    def "should return FORBIDDEN if farm is inactive"() {
        given:
        LoginRequest loginRequest = new LoginRequest(username: 'user', password: 'password')
        UserDetailsImpl userDetails = Mock(UserDetailsImpl)
        userDetails.getId() >> 1
        userDetails.getUsername() >> 'userWithDeactivatedFarm'
        userDetails.getAuthorities() >> [new SimpleGrantedAuthority('ROLE_FARM_MANAGER')]

        Farm farm = Mock(Farm)
        farm.getIsActive() >> false

        Role role = Mock(Role)
        role.getName() >> ERole.ROLE_FARM_MANAGER

        User user = Mock(User)
        user.getId() >> 1
        user.getIsActive() >> true
        user.getFarm() >> farm
        user.getRole() >> role
        List<String> roles = ['ROLE_FARM_MANAGER']
        BindingResult bindingResult = Mock(BindingResult)
        bindingResult.hasErrors() >> false
        validationRequestService.validateRequestWithException(bindingResult) >> { }
        authService.authenticateUserByLoginRequest(loginRequest) >> userDetails
        userService.getLoggedUserRoles(userDetails) >> roles
        userService.getActiveUserById(userDetails) >> Optional.of(user)
        userService.getUserFarmById(Long.valueOf(userDetails.getId())) >> farm

        farmService.checkFarmDeactivation(farm, role) >> { throw new AccessDeniedException('Gospodarstwo jest nieaktywne. Kod aktywacyjny wygasł.') }

        when:
        ResponseEntity<?> response = authController.authenticateUser(loginRequest, bindingResult)

        then:
        response.getStatusCode() == HttpStatus.FORBIDDEN
        response.body.message == 'Gospodarstwo jest nieaktywne. Kod aktywacyjny wygasł.'
    }

    def "should return message if activation code is expiring soon"() {
        given:
        LoginRequest loginRequest = new LoginRequest(username: 'user', password: 'password')
        UserDetailsImpl userDetails = Mock(UserDetailsImpl)
        userDetails.getId() >> 1
        userDetails.getUsername() >> 'user'
        userDetails.getEmail() >> 'user@gmail.com'
        userDetails.getAuthorities() >> [new SimpleGrantedAuthority('ROLE_FARM_OWNER')]

        Farm farm = Mock(Farm)
        farm.getIsActive() >> true
        farm.getIdActivationCode() >> 1

        Role role = Mock(Role)
        role.getName() >> ERole.ROLE_FARM_OWNER

        User user = Mock(User)
        user.getId() >> 1
        user.getIsActive() >> true
        user.getFarm() >> farm
        user.getRole() >> role
        List<String> roles = ['ROLE_FARM_OWNER']
        BindingResult bindingResult = Mock(BindingResult)
        bindingResult.hasErrors() >> false

        ActivationCode activationCode = Mock(ActivationCode)
        activationCode.getExpireDate() >> LocalDate.now().plusDays(5)

        validationRequestService.validateRequestWithException(bindingResult) >> { }
        authService.authenticateUserByLoginRequest(loginRequest) >> userDetails
        userService.getLoggedUserRoles(userDetails) >> roles
        userService.getActiveUserById(userDetails) >> Optional.of(user)
        userService.getUserFarmById(Long.valueOf(userDetails.getId())) >> farm
        farmService.checkFarmDeactivation(farm, role) >> null

        ResponseCookie jwtCookie = ResponseCookie.from('jwtTokenName', 'jwtTokenString')
                .path('/api')
                .httpOnly(true)
                .maxAge(Duration.ofSeconds(24 * 60 * 60))
                .build()
        jwtUtils.generateJwtCookie(userDetails) >> jwtCookie

        long daysToExpiration = ChronoUnit.DAYS.between(LocalDate.now(), activationCode.getExpireDate())
        activationCodeService.generateExpireCodeInfo(farm, roles) >> 'Kod aktywacyjny wygasa za ' + daysToExpiration + ' dni.'

        when:
        ResponseEntity<?> response = authController.authenticateUser(loginRequest, bindingResult)

        then:
        response.body.expireCodeInfo == 'Kod aktywacyjny wygasa za 5 dni.'
        response.getStatusCode() == HttpStatus.OK
        response.headers.getFirst(HttpHeaders.SET_COOKIE).contains('jwtTokenName=')
        response.headers.getFirst(HttpHeaders.SET_COOKIE).contains('Path=/api')
        response.headers.getFirst(HttpHeaders.SET_COOKIE).contains('Max-Age=86400')
        response.headers.getFirst(HttpHeaders.SET_COOKIE).contains('HttpOnly')
        response.body.id == 1
        response.body.username == 'user'
        response.body.email == 'user@gmail.com'
        response.body.roles.contains('ROLE_FARM_OWNER')
    }

    /*
        logout
    */

    def "should sign out user and return expected response"() {
        given:
        ResponseCookie cookie = ResponseCookie.from('jwtTokenName', '')
                .path('/api')
                .build()

        jwtUtils.getCleanJwtCookie() >> cookie

        when:
        ResponseEntity<?> result = authController.logoutUser()

        then:
        result.getStatusCode() == HttpStatus.OK
        result.headers.getFirst(HttpHeaders.SET_COOKIE) == 'jwtTokenName=; Path=/api'
        result.body.message == 'Wylogowano'
    }

}
