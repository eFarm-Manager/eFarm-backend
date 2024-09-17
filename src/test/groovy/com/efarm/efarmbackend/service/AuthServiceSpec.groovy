package com.efarm.efarmbackend.service

import com.efarm.efarmbackend.model.farm.ActivationCode
import com.efarm.efarmbackend.model.farm.Address
import com.efarm.efarmbackend.model.farm.Farm
import com.efarm.efarmbackend.model.user.ERole
import com.efarm.efarmbackend.model.user.Role
import com.efarm.efarmbackend.model.user.User
import com.efarm.efarmbackend.payload.request.SignupFarmRequest
import com.efarm.efarmbackend.payload.request.SignupRequest
import com.efarm.efarmbackend.payload.response.MessageResponse
import com.efarm.efarmbackend.repository.farm.ActivationCodeRepository
import com.efarm.efarmbackend.repository.farm.AddressRepository
import com.efarm.efarmbackend.repository.farm.FarmRepository
import com.efarm.efarmbackend.repository.user.RoleRepository
import com.efarm.efarmbackend.repository.user.UserRepository
import com.efarm.efarmbackend.security.services.UserDetailsImpl
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll
import org.springframework.http.HttpStatus

import jakarta.transaction.Transactional
import java.time.LocalDate
import java.util.HashSet
import java.util.Optional
import java.util.Set

class AuthServiceSpec extends Specification {

    def userRepository = Mock(UserRepository)
    def farmRepository = Mock(FarmRepository)
    def addressRepository = Mock(AddressRepository)
    def activationCodeRepository = Mock(ActivationCodeRepository)
    def userService = Mock(UserService)
    def activationCodeService = Mock(ActivationCodeService)
    def farmService = Mock(FarmService)
    def encoder = Mock(PasswordEncoder)
    @Subject
    AuthService authService = new AuthService(
            userRepository: userRepository,
            farmRepository: farmRepository,
            addressRepository: addressRepository,
            activationCodeRepository: activationCodeRepository,
            userService: userService,
            activationCodeService: activationCodeService,
            farmService: farmService

    )

    def setup() {
        SecurityContextHolder.clearContext()
    }

    //Signup request (manager makes account for new user)
    @Unroll
    def "should handle registration for user with different scenarios - SignupRequest"() {
        given:
        //Mock Role
        String roleNameManager = 'ROLE_FARM_MANAGER'
        String roleOperator = 'ROLE_FARM_EQUIPMENT_OPERATOR'
        String roleOwner = 'ROLE_FARM_OWNER'
        Role class_role_manager = Mock(Role) {
            getId() >> 2
            getName() >> ERole.valueOf(roleNameManager)
        }
        Role class_role_operator = Mock(Role) {
            getId() >> 1
            getName() >> ERole.valueOf(roleOperator)
        }
        Role class_role_owner = Mock(Role) {
            getId() >> 3
            getName() >> ERole.valueOf(roleOwner)
        }
        //make SignupRequest
        SignupRequest signUpRequest = new SignupRequest(
                firstName: "John",
                lastName: "Doe",
                username: "newUser",
                email: "newuser@example.com",
                password: "password",
                phoneNumber: "123456789",
                role: roleName
        )        
        User mockUser = Mock(User)
        //Mock currentUser (manager)
        Farm currentFarm = Mock(Farm)
        currentFarm.getId() >> 1
        currentFarm.getFarmName() >> "uniqueFarmName"

        User currentUser = Mock(User)    
        currentUser.getUsername() >> "currentUser"
        currentUser.getId() >> 1
        currentUser.getFarm() >> currentFarm
        currentUser.getEmail() >> "test@gmail.com"
        currentUser.getPassword() >> "fwafwafa312z"
        currentUser.getRole() >> class_role_manager
        UserDetailsImpl currentUserDetails = UserDetailsImpl.build(currentUser)

        Authentication authentication = Mock(Authentication) {
            getPrincipal() >> currentUserDetails
        }
        SecurityContextHolder.getContext().setAuthentication(authentication)

        userRepository.existsByUsername(signUpRequest.username) >> usernameExists
        encoder.encode(signUpRequest.getPassword()) >> "encodedPassword"
        userService.getLoggedUserFarm() >> currentFarm
        userRepository.findById(currentUserDetails.id.toLong()) >> Optional.of(currentUser)
        userService.createFarmUser(signUpRequest) >> mockUser
        
        userRepository.save(_ as User) >> { User user -> user }

        when:
        ResponseEntity result = authService.registerUser(signUpRequest)

        then:
        result.statusCode == expectedResponse.statusCode
        result.body.message == expectedResponse.body.message

        where:
        roleName                         | usernameExists  | expectedResponse
        "ROLE_FARM_MANAGER"              | false           | ResponseEntity.ok(new MessageResponse("User registered successfully!"))
        "ROLE_FARM_EQUIPMENT_OPERATOR"   | false           | ResponseEntity.ok(new MessageResponse("User registered successfully!"))
        "ROLE_FARM_OWNER"                | false           | ResponseEntity.ok(new MessageResponse("User registered successfully!"))  
        "ROLE_FARM_EQUIPMENT_OPERATOR"   | true            | ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"))
    }

        @Unroll
    def "should handle registration for user without current account"() {
        given:
        //Mock Role
        String roleNameManager = 'ROLE_FARM_MANAGER'
        String roleOperator = 'ROLE_FARM_EQUIPMENT_OPERATOR'
        String roleOwner = 'ROLE_FARM_OWNER'
        Role class_role_manager = Mock(Role) {
            getId() >> 2
            getName() >> ERole.valueOf(roleNameManager)
        }
        Role class_role_operator = Mock(Role) {
            getId() >> 1
            getName() >> ERole.valueOf(roleOperator)
        }
        Role class_role_owner = Mock(Role) {
            getId() >> 3
            getName() >> ERole.valueOf(roleOwner)
        }
        //make SignupRequest
        SignupRequest signUpRequest = new SignupRequest(
                firstName: "John",
                lastName: "Doe",
                username: "newUser",
                email: "newuser@example.com",
                password: "password",
                phoneNumber: "123456789",
                role: "ROLE_FARM_MANAGER"
        )        
        User mockUser = Mock(User)
        Authentication authentication = Mock(Authentication) {
            getPrincipal() >> null
        }
        SecurityContextHolder.getContext().setAuthentication(authentication)

        userRepository.existsByUsername(signUpRequest.username) >> false
        encoder.encode(signUpRequest.getPassword()) >> "encodedPassword"
        userService.createFarmUser(signUpRequest) >> mockUser
        userService.getLoggedUserFarm() >> { throw new RuntimeException() }
        
        when:
        ResponseEntity<?> response = authService.registerUser(signUpRequest)

        then:
        response.statusCodeValue == 400
        response.body.message == null
    }

    //FARM REQUEST

    @Unroll
    def "should handle registration for farm and manager with different scenarios - SignupFarmRequest"() {
        given:
        def signUpFarmRequest = new SignupFarmRequest(
                firstName: "John",
                lastName: "Doe",
                username: "user",
                email: "user@example.com",
                password: "password",
                phoneNumber: "123456789",
                farmName: "FarmName",
                activationCode: "activation-code"
        )
        User user = Mock(User)
        userRepository.existsByUsername(signUpFarmRequest.username) >> usernameExists
        farmRepository.existsByFarmName(signUpFarmRequest.farmName) >> farmExists

        userService.createFarmOwner(signUpFarmRequest) >> user
        activationCodeRepository.findByCode(signUpFarmRequest.activationCode) >> Optional.of(new ActivationCode(
            code: "activation-code",
            expireDate: LocalDate.now().plusDays(1),
            isUsed: false))
        activationCodeService.checkActivationCode(signUpFarmRequest.activationCode) >> ResponseEntity.ok().build()
        

        when:
        ResponseEntity result = authService.registerFarmAndFarmOwner(signUpFarmRequest)

        then:
        if(!(usernameExists || farmExists)){
        1 * activationCodeService.markActivationCodeAsUsed(signUpFarmRequest.getActivationCode())
        }
        else {
            0 * activationCodeService.markActivationCodeAsUsed(signUpFarmRequest.getActivationCode())
        }

        result.statusCode == expectedResponse.statusCode
        result.body.message == expectedResponse.body.message

        where:
        usernameExists  | farmExists | expectedResponse
        true            | false      | ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"))
        false           | true       | ResponseEntity.badRequest().body(new MessageResponse("Error: Farm Name is already taken!"))
        false           | false      | ResponseEntity.ok(new MessageResponse("Farm registered successfully!"))
    }

    @Unroll
    def "should handle activation code validation scenarios during farm registration - #scenario"() {
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
        User user = Mock(User)
    
        userRepository.existsByUsername(signUpFarmRequest.username) >> false
        farmRepository.existsByFarmName(signUpFarmRequest.farmName) >> false

        if (activationCodeExists) {
            activationCodeRepository.findByCode(signUpFarmRequest.activationCode) >> Optional.of(new ActivationCode(
                    id: 1,
                    code: "activation-code",
                    expireDate: activationCodeExpiryDate,
                    isUsed: activationCodeUsed
            ))
        } else {
            activationCodeRepository.findByCode(signUpFarmRequest.activationCode) >> Optional.empty()
        }
        userService.createFarmOwner(signUpFarmRequest) >> user

        activationCodeService.checkActivationCode(signUpFarmRequest.activationCode) >> expectedResponse

        when:
        ResponseEntity result = authService.registerFarmAndFarmOwner(signUpFarmRequest)

        then:
        result.statusCode == expectedResponse.statusCode
        result.body.message == expectedResponse.body.message

        where:
        scenario                                | activationCodeExists | activationCodeExpiryDate           | activationCodeUsed | expectedResponse
        "Activation code does not exist"        | false                | null                               | false              | ResponseEntity.badRequest().body(new MessageResponse("Activation code does not exist."))
        "Activation code has expired"           | true                 | LocalDate.now().minusDays(1)       | false              | ResponseEntity.badRequest().body(new MessageResponse("Activation code has expired."))
        "Activation code has already been used" | true                 | LocalDate.now().plusDays(1)        | true               | ResponseEntity.badRequest().body(new MessageResponse("Activation code has already been used."))
        "Successful registration"               | true                 | LocalDate.now().plusDays(1)        | false              | ResponseEntity.ok(new MessageResponse("Farm registered successfully!"))
    }    


}