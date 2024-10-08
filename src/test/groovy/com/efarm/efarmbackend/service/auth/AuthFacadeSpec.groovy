package com.efarm.efarmbackend.service.facades

import com.efarm.efarmbackend.model.farm.ActivationCode
import com.efarm.efarmbackend.model.farm.Address
import com.efarm.efarmbackend.model.farm.Farm
import com.efarm.efarmbackend.model.user.Role
import com.efarm.efarmbackend.model.user.ERole
import com.efarm.efarmbackend.model.user.User
import com.efarm.efarmbackend.payload.request.LoginRequest
import com.efarm.efarmbackend.payload.request.SignupFarmRequest
import com.efarm.efarmbackend.payload.request.SignupRequest
import com.efarm.efarmbackend.payload.request.UpdateActivationCodeRequest
import com.efarm.efarmbackend.payload.request.UpdateActivationCodeByLoggedOwnerRequest
import com.efarm.efarmbackend.payload.request.ChangePasswordRequest
import com.efarm.efarmbackend.payload.response.MessageResponse
import com.efarm.efarmbackend.payload.response.UserInfoResponse
import com.efarm.efarmbackend.repository.farm.ActivationCodeRepository
import com.efarm.efarmbackend.repository.farm.AddressRepository
import com.efarm.efarmbackend.repository.farm.FarmRepository
import com.efarm.efarmbackend.repository.user.UserRepository
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
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.validation.BindingResult
import spock.lang.Specification
import spock.lang.Subject
import java.time.LocalDate
import org.springframework.validation.FieldError

import java.time.temporal.ChronoUnit
import java.time.Duration


class AuthFacadeSpec extends Specification {

    def userRepository = Mock(UserRepository)
    def farmRepository = Mock(FarmRepository)
    def addressRepository = Mock(AddressRepository)
    def activationCodeRepository = Mock(ActivationCodeRepository)
    def authService = Mock(AuthService)
    def userService = Mock(UserService)
    def activationCodeService = Mock(ActivationCodeService)
    def farmService = Mock(FarmService)
    def validationRequestService = Mock(ValidationRequestService)
    def authenticationManager = Mock(AuthenticationManager)
    def jwtUtils = Mock(JwtUtils)

    @Subject
    AuthFacade authFacade = new AuthFacade(
            userRepository: userRepository,
            farmRepository: farmRepository,
            addressRepository: addressRepository,
            activationCodeRepository: activationCodeRepository,
            authService: authService,
            userService: userService,
            activationCodeService: activationCodeService,
            farmService: farmService,
            validationRequestService: validationRequestService,
            authenticationManager: authenticationManager,
            jwtUtils: jwtUtils
    )

    def setup() {
        SecurityContextHolder.clearContext()
    }
    /*
            authenticateUser
    */

    def "should authenticate user properly"() {
        given:
        LoginRequest loginRequest = new LoginRequest(
                username: "user",
                password: "password"
        )
        Farm farm = Mock(Farm)

        Role role = Mock(Role)
        role.getName() >> ERole.ROLE_FARM_OWNER

        UserDetailsImpl userDetails = Mock(UserDetailsImpl)
        userDetails.getId() >> 1
        userDetails.getUsername() >> "user"
        userDetails.getEmail() >> "user@gmail.com"
        userDetails.getAuthorities() >> [new SimpleGrantedAuthority("ROLE_FARM_OWNER")]
        User user = Mock(User)
        user.getId() >> 1
        user.getIsActive() >> true
        user.getFarm() >> farm
        user.getRole() >> role
        List<String> roles = ["ROLE_FARM_OWNER"]


        authService.authenticateUserByLoginRequest(loginRequest) >> userDetails
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        SecurityContextHolder.getContext().setAuthentication(auth)
        userService.getLoggedUserRoles(userDetails) >> roles
        userRepository.findById(Long.valueOf(userDetails.getId())) >> Optional.of(user)
        userService.getUserFarmById(Long.valueOf(userDetails.getId())) >> farm

        farmService.checkFarmDeactivation(farm, role) >> null
        activationCodeService.signinWithExpireCodeInfo(userDetails, farm, roles) >> null

        ResponseCookie jwtCookie = ResponseCookie.from("jwtTokenName", "jwtTokenString")
                .path("/api")
                .httpOnly(true)
                .maxAge(Duration.ofSeconds(24 * 60 * 60))
                .build()
        jwtUtils.generateJwtCookie(userDetails) >> jwtCookie

        when:
        ResponseEntity<?> response = authFacade.authenticateUser(loginRequest)

        then:
        response.getStatusCode() == HttpStatus.OK
        response.headers.getFirst(HttpHeaders.SET_COOKIE).contains("jwtTokenName=")
        response.headers.getFirst(HttpHeaders.SET_COOKIE).contains("Path=/api")
        response.headers.getFirst(HttpHeaders.SET_COOKIE).contains("Max-Age=86400")
        response.headers.getFirst(HttpHeaders.SET_COOKIE).contains("HttpOnly")
        response.body.id == 1
        response.body.username == "user"
        response.body.email == "user@gmail.com"
        response.body.roles.contains("ROLE_FARM_OWNER")
    }

    def "should return 400 for inactive user"() {
        given:
        LoginRequest loginRequest = new LoginRequest(username: "inactiveUser", password: "password")

        UserDetailsImpl userDetails = Mock(UserDetailsImpl)
        userDetails.getId() >> 2
        userDetails.getUsername() >> "inactiveUser"

        User user = Mock(User)
        user.getId() >> 2
        user.getIsActive() >> false
        user.getFarm() >> Mock(Farm)

        authService.authenticateUserByLoginRequest(loginRequest) >> userDetails
        userRepository.findById(Long.valueOf(userDetails.getId())) >> Optional.of(user)

        when:
        ResponseEntity<?> response = authFacade.authenticateUser(loginRequest)

        then:
        response.getStatusCode() == HttpStatus.BAD_REQUEST
        response.body.message == "User is inactive."
    }

    def "should return 401 for wrong credentials"() {
        given:
        LoginRequest loginRequest = new LoginRequest(username: "wrongUser", password: "wrongPassword")

        authService.authenticateUserByLoginRequest(loginRequest) >> { throw new RuntimeException("Nieprawidłowe dane logowania") }

        when:
        ResponseEntity<?> response = authFacade.authenticateUser(loginRequest)

        then:
        response.getStatusCode() == HttpStatus.UNAUTHORIZED
        response.body.message == "Nieprawidłowe dane logowania"

    }

    def "should return FORBIDDEN if farm is inactive"() {
        given:
        LoginRequest loginRequest = new LoginRequest(username: "user", password: "password")
        UserDetailsImpl userDetails = Mock(UserDetailsImpl)
        userDetails.getId() >> 1
        userDetails.getUsername() >> "userWithDeactivatedFarm"
        userDetails.getAuthorities() >> [new SimpleGrantedAuthority("ROLE_FARM_MANAGER")]

        Farm farm = Mock(Farm)
        farm.getIsActive() >> false


        Role role = Mock(Role)
        role.getName() >> ERole.ROLE_FARM_MANAGER

        User user = Mock(User)
        user.getId() >> 1
        user.getIsActive() >> true
        user.getFarm() >> farm
        user.getRole() >> role


        List<String> roles = ["ROLE_FARM_MANAGER"]

        authService.authenticateUserByLoginRequest(loginRequest) >> userDetails
        userService.getLoggedUserRoles(userDetails) >> roles
        userRepository.findById(Long.valueOf(userDetails.getId())) >> Optional.of(user)
        userService.getUserFarmById(Long.valueOf(userDetails.getId())) >> farm

        farmService.checkFarmDeactivation(farm, role) >> ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new MessageResponse("Gospodarstwo jest nieaktywne. Kod aktywacyjny wygasł."))

        when:
        ResponseEntity<?> response = authFacade.authenticateUser(loginRequest)

        then:
        response.getStatusCode() == HttpStatus.FORBIDDEN
        response.body.message == "Gospodarstwo jest nieaktywne. Kod aktywacyjny wygasł."
    }

    def "should return message if activation code is expiring soon"() {
        given:
        LoginRequest loginRequest = new LoginRequest(username: "user", password: "password")
        UserDetailsImpl userDetails = Mock(UserDetailsImpl)
        userDetails.getId() >> 1
        userDetails.getUsername() >> "user"
        userDetails.getEmail() >> "user@gmail.com"
        userDetails.getAuthorities() >> [new SimpleGrantedAuthority("ROLE_FARM_OWNER")]

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


        List<String> roles = ["ROLE_FARM_OWNER"]

        ActivationCode activationCode = Mock(ActivationCode)
        activationCode.getExpireDate() >> LocalDate.now().plusDays(5)

        authService.authenticateUserByLoginRequest(loginRequest) >> userDetails
        userService.getLoggedUserRoles(userDetails) >> roles
        userRepository.findById(Long.valueOf(userDetails.getId())) >> Optional.of(user)
        userService.getUserFarmById(Long.valueOf(userDetails.getId())) >> farm
        farmService.checkFarmDeactivation(farm, role) >> null

        ResponseCookie jwtCookie = ResponseCookie.from("jwtTokenName", "jwtTokenString")
                .path("/api")
                .httpOnly(true)
                .maxAge(Duration.ofSeconds(24 * 60 * 60))
                .build()
        jwtUtils.generateJwtCookie(userDetails) >> jwtCookie

        long daysToExpiration = ChronoUnit.DAYS.between(LocalDate.now(), activationCode.getExpireDate())
        activationCodeService.signinWithExpireCodeInfo(userDetails, farm, roles) >> ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtUtils.generateJwtCookie(userDetails).toString())
                .body(new UserInfoResponse(userDetails.getId(), userDetails.getUsername(), userDetails.getEmail(), roles, "Kod aktywacyjny wygasa za " + daysToExpiration + " dni."))


        when:
        ResponseEntity<?> response = authFacade.authenticateUser(loginRequest)

        then:
        response.body.expireCodeInfo == "Kod aktywacyjny wygasa za 5 dni."
        response.getStatusCode() == HttpStatus.OK
        response.headers.getFirst(HttpHeaders.SET_COOKIE).contains("jwtTokenName=")
        response.headers.getFirst(HttpHeaders.SET_COOKIE).contains("Path=/api")
        response.headers.getFirst(HttpHeaders.SET_COOKIE).contains("Max-Age=86400")
        response.headers.getFirst(HttpHeaders.SET_COOKIE).contains("HttpOnly")
        response.body.id == 1
        response.body.username == "user"
        response.body.email == "user@gmail.com"
        response.body.roles.contains("ROLE_FARM_OWNER")
    }

    /*
            RegisterUser
    */

    def "should register user successfully"() {
        given:
        SignupRequest signUpRequest = new SignupRequest(
                firstName: "John",
                lastName: "Doe",
                username: "user",
                email: "user@gmail.com",
                password: "password",
                phoneNumber: "",
                role: "ROLE_FARM_MANAGER"
        )
        BindingResult bindingResult = Mock(BindingResult)
        User user = Mock(User)
        Farm farm = Mock(Farm)

        bindingResult.hasErrors() >> false
        validationRequestService.validateRequest(bindingResult) >> null
        userRepository.existsByUsername(signUpRequest.getUsername()) >> false
        userService.createFarmUser(signUpRequest) >> user
        userService.getLoggedUserFarm() >> farm

        when:
        ResponseEntity<?> response = authFacade.registerUser(signUpRequest, bindingResult)

        then:
        1 * userRepository.save(user)
        response.getStatusCode() == HttpStatus.OK
        response.body.message == "User registered successfully!"
    }

    def "should return error if binding result has errors"() {
        given:
        SignupRequest signUpRequest = new SignupRequest(
                username: "username",
                lastName: "Doe",
                email: "newuser@m",
                password: "password",
                phoneNumber: "123456789",
                role: "ROLE_FARM_MANAGER"
        )
        BindingResult bindingResult = Mock(BindingResult)
        bindingResult.hasErrors() >> true
        bindingResult.getFieldErrors() >> [
                new FieldError("signupRequest", "firstName", "first name is required"),
                new FieldError("signupRequest", "email", "Email is invalid")
        ]
        ResponseEntity<?> validationErrorResponse = ResponseEntity.badRequest().body(new MessageResponse("Validation error"))
        validationRequestService.validateRequest(bindingResult) >> validationErrorResponse

        when:
        ResponseEntity<?> response = authFacade.registerUser(signUpRequest, bindingResult)

        then:
        response.getStatusCode() == HttpStatus.BAD_REQUEST
        response.body.message == "Validation error"
    }

    def "should return error if username already exists"() {
        given:
        SignupRequest signUpRequest = new SignupRequest(
                firstName: "John",
                lastName: "Doe",
                username: "existingUser",
                email: "newuser@example.com",
                password: "password",
                phoneNumber: "123456789",
                role: "ROLE_FARM_MANAGER"
        )
        BindingResult bindingResult = Mock(BindingResult)
        bindingResult.hasErrors() >> false
        validationRequestService.validateRequest(bindingResult) >> null
        userRepository.existsByUsername(signUpRequest.getUsername()) >> true

        when:
        ResponseEntity<?> response = authFacade.registerUser(signUpRequest, bindingResult)

        then:
        response.getStatusCode() == HttpStatus.BAD_REQUEST
        response.body.message == "Error: Username is already taken!"
    }

    def "should return error if farm retrieval fails"() {
        given:
        SignupRequest signUpRequest = new SignupRequest(
                firstName: "John",
                lastName: "Doe",
                username: "newUser",
                email: "newuser@example.com",
                password: "password",
                phoneNumber: "123456789",
                role: "ROLE_FARM_MANAGER"
        )
        BindingResult bindingResult = Mock(BindingResult)
        User user = Mock(User)

        bindingResult.hasErrors() >> false
        validationRequestService.validateRequest(bindingResult) >> null
        userRepository.existsByUsername(signUpRequest.getUsername()) >> false
        userService.createFarmUser(signUpRequest) >> user
        userService.getLoggedUserFarm() >> { throw new RuntimeException("Farm not found") }

        when:
        ResponseEntity<?> response = authFacade.registerUser(signUpRequest, bindingResult)

        then:
        response.getStatusCode() == HttpStatus.BAD_REQUEST
        response.body.message == "Farm not found"
    }
    /*
        registerFarmAndFarmOwner
    */

    def "should handle signup farm and farm owner"() {
        given:
        SignupFarmRequest signUpFarmRequest = new SignupFarmRequest(
                firstName: "John",
                lastName: "Doe",
                username: "newUser",
                email: "newuser@example.com",
                password: "password",
                phoneNumber: "123456789",
                farmName: "NewFarm",
                activationCode: "activation-code"
        )
        BindingResult bindingResult = Mock(BindingResult)
        User user = Mock(User)
        ActivationCode activationCode = Mock(ActivationCode)
        activationCode.getId() >> 1
        Farm farm = Mock(Farm)
        Address address = Mock(Address)
        address.getId() >> 1

        bindingResult.hasErrors() >> false
        validationRequestService.validateRequest(bindingResult) >> null
        userRepository.existsByUsername(signUpFarmRequest.getUsername()) >> false
        farmRepository.existsByFarmName(signUpFarmRequest.getFarmName()) >> false
        userService.createFarmOwner(signUpFarmRequest) >> user
        activationCodeRepository.findByCode(signUpFarmRequest.getActivationCode()) >> Optional.of(activationCode)
        activationCodeService.validateActivationCode(signUpFarmRequest.getActivationCode()) >> ResponseEntity.ok().build()
        farmService.createFarm(signUpFarmRequest.getFarmName(), address.getId(), activationCode.getId()) >> farm
        activationCodeService.markActivationCodeAsUsed(signUpFarmRequest.getActivationCode()) >> {}

        when:
        ResponseEntity<?> response = authFacade.registerFarmAndFarmOwner(signUpFarmRequest, bindingResult)

        then:
        1 * addressRepository.save(_)
        1 * userRepository.save(user)
        response.getStatusCode() == HttpStatus.OK
        response.body.message == "Farm registered successfully!"
    }

    def "should return bad request when bindingResult has errors"() {
        given:
        SignupFarmRequest signUpFarmRequest = new SignupFarmRequest(
                firstName: "John",
                lastName: "Doe",
                email: "newuser@example.com",
                password: "password",
                phoneNumber: "123456789",
                farmName: "NewFarm",
                activationCode: "activation-code"
        )
        BindingResult bindingResult = Mock(BindingResult)

        bindingResult.hasErrors() >> true
        bindingResult.getFieldErrors() >> [new FieldError("signUpFarmRequest", "username", "Username is required")]
        validationRequestService.validateRequest(bindingResult) >> ResponseEntity.badRequest().body(new MessageResponse("Validation error"))

        when:
        ResponseEntity<?> response = authFacade.registerFarmAndFarmOwner(signUpFarmRequest, bindingResult)

        then:
        response.getStatusCode() == HttpStatus.BAD_REQUEST
        response.body.message == "Validation error"
    }

    def "should return bad request when username is already taken"() {
        given:
        SignupFarmRequest signUpFarmRequest = new SignupFarmRequest(
                firstName: "John",
                lastName: "Doe",
                username: "existingUser",
                email: "newuser@example.com",
                password: "password",
                phoneNumber: "123456789",
                farmName: "NewFarm",
                activationCode: "activation-code"
        )
        BindingResult bindingResult = Mock(BindingResult)

        bindingResult.hasErrors() >> false
        validationRequestService.validateRequest(bindingResult) >> null
        userRepository.existsByUsername(signUpFarmRequest.getUsername()) >> true

        when:
        ResponseEntity<?> response = authFacade.registerFarmAndFarmOwner(signUpFarmRequest, bindingResult)

        then:
        response.getStatusCode() == HttpStatus.BAD_REQUEST
        response.body.message == "Error: Username is already taken!"
    }

    def "should return bad request when farm name is already taken"() {
        given:
        SignupFarmRequest signUpFarmRequest = new SignupFarmRequest(
                firstName: "John",
                lastName: "Doe",
                username: "newUser",
                email: "newuser@example.com",
                password: "password",
                phoneNumber: "123456789",
                farmName: "ExistingFarm",
                activationCode: "activation-code"
        )
        BindingResult bindingResult = Mock(BindingResult)

        bindingResult.hasErrors() >> false
        validationRequestService.validateRequest(bindingResult) >> null
        userRepository.existsByUsername(signUpFarmRequest.getUsername()) >> false
        farmRepository.existsByFarmName(signUpFarmRequest.getFarmName()) >> true

        when:
        ResponseEntity<?> response = authFacade.registerFarmAndFarmOwner(signUpFarmRequest, bindingResult)

        then:
        response.getStatusCode() == HttpStatus.BAD_REQUEST
        response.body.message == "Error: Farm Name is already taken!"
    }

    def "should return bad request when activation code does not exist"() {
        given:
        SignupFarmRequest signUpFarmRequest = new SignupFarmRequest(
                firstName: "John",
                lastName: "Doe",
                username: "newUser",
                email: "newuser@example.com",
                password: "password",
                phoneNumber: "123456789",
                farmName: "NewFarm",
                activationCode: "invalid-code"
        )
        def bindingResult = Mock(BindingResult)

        bindingResult.hasErrors() >> false
        validationRequestService.validateRequest(bindingResult) >> null
        userRepository.existsByUsername(signUpFarmRequest.getUsername()) >> false
        farmRepository.existsByFarmName(signUpFarmRequest.getFarmName()) >> false
        activationCodeRepository.findByCode(signUpFarmRequest.getActivationCode()) >> Optional.empty()

        activationCodeService.validateActivationCode(signUpFarmRequest.getActivationCode()) >> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse("Activation code does not exist."))

        when:
        ResponseEntity<?> response = authFacade.registerFarmAndFarmOwner(signUpFarmRequest, bindingResult)

        then:
        response.getStatusCode() == HttpStatus.BAD_REQUEST
        response.body.message == "Activation code does not exist."
    }

    def "should return bad request when activation code is used"() {
        given:
        SignupFarmRequest signUpFarmRequest = new SignupFarmRequest(
                firstName: "John",
                lastName: "Doe",
                username: "newUser",
                email: "newuser@example.com",
                password: "password",
                phoneNumber: "123456789",
                farmName: "NewFarm",
                activationCode: "invalid-code"
        )
        BindingResult bindingResult = Mock(BindingResult)
        ActivationCode activationCode = Mock(ActivationCode)
        activationCode.getIsUsed() >> true

        bindingResult.hasErrors() >> false
        validationRequestService.validateRequest(bindingResult) >> null
        userRepository.existsByUsername(signUpFarmRequest.getUsername()) >> false
        farmRepository.existsByFarmName(signUpFarmRequest.getFarmName()) >> false
        activationCodeRepository.findByCode(signUpFarmRequest.getActivationCode()) >> Optional.of(activationCode)

        activationCodeService.validateActivationCode(signUpFarmRequest.getActivationCode()) >> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse("Activation code has already been used."))

        when:
        ResponseEntity<?> response = authFacade.registerFarmAndFarmOwner(signUpFarmRequest, bindingResult)

        then:
        response.getStatusCode() == HttpStatus.BAD_REQUEST
        response.body.message == "Activation code has already been used."
    }

    def "should return bad request when marking activation code as used fails"() {
        given:
        SignupFarmRequest signUpFarmRequest = new SignupFarmRequest(
                firstName: "John",
                lastName: "Doe",
                username: "newUser",
                email: "newuser@example.com",
                password: "password",
                phoneNumber: "123456789",
                farmName: "NewFarm",
                activationCode: "activation-code"
        )
        BindingResult bindingResult = Mock(BindingResult)
        ActivationCode activationCode = Mock(ActivationCode)
        activationCode.getId() >> 1
        Farm farm = Mock(Farm)
        Address address = Mock(Address)
        address.getId() >> 1
        User user = Mock(User)

        bindingResult.hasErrors() >> false
        validationRequestService.validateRequest(bindingResult) >> null
        userRepository.existsByUsername(signUpFarmRequest.getUsername()) >> false
        farmRepository.existsByFarmName(signUpFarmRequest.getFarmName()) >> false
        userService.createFarmOwner(signUpFarmRequest) >> user
        activationCodeRepository.findByCode(signUpFarmRequest.getActivationCode()) >> Optional.of(activationCode)
        activationCodeService.validateActivationCode(signUpFarmRequest.getActivationCode()) >> ResponseEntity.ok().build()
        farmService.createFarm(signUpFarmRequest.getFarmName(), address.getId(), activationCode.getId()) >> farm


        activationCodeService.markActivationCodeAsUsed(signUpFarmRequest.getActivationCode()) >> { throw new RuntimeException("Activation code not found") }

        when:
        ResponseEntity<?> response = authFacade.registerFarmAndFarmOwner(signUpFarmRequest, bindingResult)

        then:
        response.getStatusCode() == HttpStatus.BAD_REQUEST
        response.body.message == "Activation code not found"
    }
    /*
        updateActivationCode
    */

    def "should update activation code by owner"() {
        given:
        UpdateActivationCodeRequest updateActivationCodeRequest = new UpdateActivationCodeRequest(
                username: "user",
                password: "password",
                newActivationCode: "newActivationCode"
        )
        List<String> roles = ["ROLE_FARM_OWNER"]
        UserDetailsImpl userDetails = Mock(UserDetailsImpl)
        userDetails.getId() >> 1
        userDetails.getUsername() >> updateActivationCodeRequest.getUsername()
        Farm farm = Mock(Farm)
        farm.getId() >> 1


        authService.authenticateUserByUpdateCodeRequest(updateActivationCodeRequest) >> userDetails
        userService.getLoggedUserRoles(userDetails) >> roles
        userService.getUserFarmById(Long.valueOf(userDetails.getId())) >> farm
        activationCodeService.updateActivationCodeForFarm(updateActivationCodeRequest.getNewActivationCode(), farm.getId(), userDetails.getUsername()) >> ResponseEntity
                .status(HttpStatus.OK)
                .location(URI.create("/"))
                .body(new MessageResponse("Activation code updated successfully for the farm."))

        when:
        ResponseEntity<?> response = authFacade.updateActivationCode(updateActivationCodeRequest)

        then:
        response.getStatusCode() == HttpStatus.OK
        response.getBody().message.contains("Activation code updated successfully for the farm.")
        response.getHeaders().getLocation() == URI.create("/")
    }

    def "should return UNAUTHORIZED if user is not a farm owner"() {
        given:
        UpdateActivationCodeRequest updateActivationCodeRequest = new UpdateActivationCodeRequest(
                username: "user",
                password: "password",
                newActivationCode: "newActivationCode"
        )
        List<String> roles = ["ROLE_FARM_MANAGER"]
        UserDetailsImpl userDetails = Mock(UserDetailsImpl)
        userDetails.getId() >> 1

        authService.authenticateUserByUpdateCodeRequest(updateActivationCodeRequest) >> userDetails
        userService.getLoggedUserRoles(userDetails) >> roles

        when:
        ResponseEntity<?> response = authFacade.updateActivationCode(updateActivationCodeRequest)

        then:
        response.getStatusCode() == HttpStatus.UNAUTHORIZED
        response.getBody().message == "Brak uprawnień"
    }

    def "should return BAD_REQUEST if activation code update fails"() {
        given:
        UpdateActivationCodeRequest updateActivationCodeRequest = new UpdateActivationCodeRequest(
                username: "user",
                password: "password",
                newActivationCode: "invalidActivationCode"
        )
        List<String> roles = ["ROLE_FARM_OWNER"]
        UserDetailsImpl userDetails = Mock(UserDetailsImpl)
        userDetails.getId() >> 1
        userDetails.getUsername() >> updateActivationCodeRequest.getUsername()
        Farm farm = Mock(Farm)
        farm.getId() >> 1

        authService.authenticateUserByUpdateCodeRequest(updateActivationCodeRequest) >> userDetails
        userService.getLoggedUserRoles(userDetails) >> roles
        userService.getUserFarmById(Long.valueOf(userDetails.getId())) >> farm
        activationCodeService.updateActivationCodeForFarm(updateActivationCodeRequest.getNewActivationCode(), farm.getId(), userDetails.getUsername()) >> ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse("Activation code does not exist."))

        when:
        ResponseEntity<?> response = authFacade.updateActivationCode(updateActivationCodeRequest)

        then:
        response.getStatusCode() == HttpStatus.BAD_REQUEST
        response.getBody().message == "Activation code does not exist."
    }
    /*
        updateActivationCodeByLoggedOwner
    */

    def "should update activation code when password is valid and no validation errors"() {
        given:
        UpdateActivationCodeByLoggedOwnerRequest request = Mock(UpdateActivationCodeByLoggedOwnerRequest) {
            getPassword() >> "validPassword"
            getNewActivationCode() >> "newActivationCode123"
        }
        BindingResult bindingResult = Mock(BindingResult) {
            hasErrors() >> false
        }
        Integer farmId = 1
        String username = "loggedOwner"
        Farm farm = Mock(Farm) { getId() >> farmId }

        validationRequestService.validateRequest(bindingResult) >> null
        userService.isPasswordValidForLoggedUser(request.getPassword()) >> true
        userService.getLoggedUserFarm() >> farm
        userService.getLoggedUser() >> Mock(User) { getUsername() >> username }

        activationCodeService.updateActivationCodeForFarm(request.getNewActivationCode(), farmId, username) >> ResponseEntity.ok(new MessageResponse("Activation code updated successfully for the farm."))

        when:
        ResponseEntity<?> response = authFacade.updateActivationCodeByLoggedOwner(request, bindingResult)

        then:
        response.getStatusCode() == HttpStatus.OK
        response.body.message == "Activation code updated successfully for the farm."
    }


    def "should return unauthorized when password is invalid"() {
        given:
        UpdateActivationCodeByLoggedOwnerRequest request = Mock(UpdateActivationCodeByLoggedOwnerRequest) {
            getPassword() >> "invalidPassword"
            getNewActivationCode() >> "newActivationCode123"
        }
        BindingResult bindingResult = Mock(BindingResult) {
            hasErrors() >> false
        }

        validationRequestService.validateRequest(bindingResult) >> null
        userService.isPasswordValidForLoggedUser(request.getPassword()) >> false

        when:
        ResponseEntity<?> response = authFacade.updateActivationCodeByLoggedOwner(request, bindingResult)

        then:
        response.getStatusCode() == HttpStatus.UNAUTHORIZED
        response.body.message == "Nieprawidłowe hasło"
    }

    def "should return bad request when there are validation errors"() {
        given:
        UpdateActivationCodeByLoggedOwnerRequest request = Mock(UpdateActivationCodeByLoggedOwnerRequest)
        BindingResult bindingResult = Mock(BindingResult) {
            hasErrors() >> true
        }
        ResponseEntity<?> validationErrorResponse = ResponseEntity.badRequest().body(new MessageResponse("Validation error"))

        validationRequestService.validateRequest(bindingResult) >> validationErrorResponse

        when:
        ResponseEntity<?> response = authFacade.updateActivationCodeByLoggedOwner(request, bindingResult)

        then:
        response.getStatusCode() == HttpStatus.BAD_REQUEST
        response.body.message == "Validation error"
    }

    /*
        changePassword
    */

    def "should successfully change password when valid"() {
        given:
        String currentPassword = "password123"
        String newPassword = "newPassword123"
        BindingResult bindingResult = Mock(BindingResult)
        ChangePasswordRequest request = Mock(ChangePasswordRequest) {
            getCurrentPassword() >> currentPassword
            getNewPassword() >> newPassword
        }

        validationRequestService.validateRequest(bindingResult) >> null
        userService.isPasswordValidForLoggedUser(currentPassword) >> true

        when:
        ResponseEntity<?> response = authFacade.changePassword(request, bindingResult)

        then:
        response.getStatusCode() == HttpStatus.OK
        response.body.message == "Hasło zostało pomyślnie zmienione"
    }

    def "should return unauthorized if current password is invalid"() {
        given:
        String currentPassword = "wrongPassword"
        String newPassword = "newPassword123"
        BindingResult bindingResult = Mock(BindingResult)
        ChangePasswordRequest request = Mock(ChangePasswordRequest) {
            getCurrentPassword() >> currentPassword
            getNewPassword() >> newPassword
        }
        validationRequestService.validateRequest(bindingResult) >> null
        userService.isPasswordValidForLoggedUser(currentPassword) >> false

        when:
        ResponseEntity<?> response = authFacade.changePassword(request, bindingResult)

        then:
        response.getStatusCode() == HttpStatus.UNAUTHORIZED
        response.body.message == "Podano nieprawidłowe aktualne hasło"
    }

    def "should return validation error if validation fails"() {
        given:
        String currentPassword = "password123"
        String newPassword = "newPassword123"
        BindingResult bindingResult = Mock(BindingResult)
        ChangePasswordRequest request = Mock(ChangePasswordRequest) {
            getCurrentPassword() >> currentPassword
            getNewPassword() >> newPassword
        }
        ResponseEntity<?> validationError = ResponseEntity.badRequest().body(new MessageResponse("Validation error"))
        validationRequestService.validateRequest(bindingResult) >> validationError

        when:
        ResponseEntity<?> response = authFacade.changePassword(request, bindingResult)

        then:
        response.getStatusCode() == HttpStatus.BAD_REQUEST
        response.body.message == "Validation error"
    }
}