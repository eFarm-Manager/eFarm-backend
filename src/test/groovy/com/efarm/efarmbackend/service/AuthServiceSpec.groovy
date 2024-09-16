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

import jakarta.transaction.Transactional
import java.time.LocalDate
import java.util.HashSet
import java.util.Optional
import java.util.Set

class AuthServiceSpec extends Specification {

    def userRepository = Mock(UserRepository)
    def roleRepository = Mock(RoleRepository)
    def farmRepository = Mock(FarmRepository)
    def addressRepository = Mock(AddressRepository)
    def activationCodeRepository = Mock(ActivationCodeRepository)
    def encoder = Mock(PasswordEncoder)

    @Subject
    AuthService authService = new AuthService(
            userRepository: userRepository,
            roleRepository: roleRepository,
            farmRepository: farmRepository,
            addressRepository: addressRepository,
            activationCodeRepository: activationCodeRepository,
            encoder: encoder
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
        Role class_role_manager = Mock(Role) {
            getId() >> 1
            getName() >> ERole.valueOf(roleNameManager)
        }
        Role class_role_operator = Mock(Role) {
            getId() >> 2
            getName() >> ERole.valueOf(roleOperator)
        }
        roleRepository.findByName(ERole.ROLE_FARM_MANAGER) >> Optional.of(class_role_manager)
        roleRepository.findByName(ERole.ROLE_FARM_EQUIPMENT_OPERATOR) >> Optional.of(class_role_operator)

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
        //Mock currentUser (manager)
        User currentUser = Mock(User)    
        currentUser.getUsername() >> "currentUser"
        currentUser.getId() >> 1
        currentUser.getFarm() >> farm
        currentUser.getEmail() >> "test@gmail.com"
        currentUser.getPassword() >> "fwafwafa312z"
        currentUser.getRole() >> class_role_manager
        UserDetailsImpl currentUserDetails = UserDetailsImpl.build(currentUser)
        

        Authentication authentication = Mock(Authentication) {
            getPrincipal() >> currentUserDetails
        }
        SecurityContextHolder.getContext().setAuthentication(authentication)

        //testing for username exists pretty much
        userRepository.existsByUsername(signUpRequest.username) >> usernameExists
        userRepository.findById(currentUserDetails.id.toLong()) >> Optional.of(currentUser)
        userRepository.save(_ as User) >> { User user -> user }

        when:
        ResponseEntity result = authService.registerUser(signUpRequest)

        then:
        result.statusCode == expectedResponse.statusCode
        result.body.message == expectedResponse.body.message

        where:
        roleName                         | usernameExists  | farm            | expectedResponse
        "ROLE_FARM_MANAGER"              | false           | new Farm()      | ResponseEntity.ok(new MessageResponse("User registered successfully!"))
        "ROLE_FARM_EQUIPMENT_OPERATOR"   | false           | new Farm()      | ResponseEntity.ok(new MessageResponse("User registered successfully!"))
        "ROLE_FARM_EQUIPMENT_OPERATOR"   | true            | new Farm()      | ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"))
        "ROLE_FARM_EQUIPMENT_OPERATOR"   | false           | null            | ResponseEntity.badRequest().body(new MessageResponse("Error: Current user does not have a farm associated!"))
        "ROLE_FARM_MANAGER"              | false           | null            | ResponseEntity.badRequest().body(new MessageResponse("Error: Current user does not have a farm associated!"))
    }

    @Unroll
    def "should throw RuntimeException if current user isnt found by id - SignupRequest" () {
        given:
        //Mock roles
        String roleNameManager = 'ROLE_FARM_MANAGER'
        String roleOperator = 'ROLE_FARM_EQUIPMENT_OPERATOR'
        Role class_role_manager = Mock(Role) {
            getId() >> 1
            getName() >> ERole.valueOf(roleNameManager)
        }
        Role class_role_operator = Mock(Role) {
            getId() >> 2
            getName() >> ERole.valueOf(roleOperator)
        }
        roleRepository.findByName(ERole.ROLE_FARM_MANAGER) >> Optional.of(class_role_manager)
        roleRepository.findByName(ERole.ROLE_FARM_EQUIPMENT_OPERATOR) >> Optional.of(class_role_operator)
        
        //Make SignupRequest
        SignupRequest signUpRequest = new SignupRequest(
                firstName: "John",
                lastName: "Doe",
                username: "newUser",
                email: "newuser@example.com",
                password: "password",
                phoneNumber: "123456789",
                role: roleOperator
        )   
        UserDetailsImpl currentUserDetails = Mock(UserDetailsImpl) {
            getId() >> 1
        }
        Authentication authentication = Mock(Authentication) {
            getPrincipal() >> currentUserDetails
        }
        SecurityContextHolder.getContext().setAuthentication(authentication)

        userRepository.findById(Long.valueOf(currentUserDetails.getId())) >> Optional.empty()
        userRepository.existsByUsername(signUpRequest.username) >> false

        when:
        ResponseEntity result = authService.registerUser(signUpRequest)

        then:
        thrown(RuntimeException)

    }


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
        userRepository.existsByUsername(signUpFarmRequest.username) >> usernameExists
        farmRepository.existsByFarmName(signUpFarmRequest.farmName) >> farmExists


        activationCodeRepository.findByCode(signUpFarmRequest.activationCode) >> Optional.of(new ActivationCode(
            code: "activation-code",
            expireDate: LocalDate.now().plusDays(1),
            isUsed: false))
        
        roleRepository.findByName(_ as ERole) >> { ERole role -> Optional.of(new Role(name: role)) }


        when:
        ResponseEntity result = authService.registerFarmAndFarmOwner(signUpFarmRequest)

        then:
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
    
        userRepository.existsByUsername(signUpFarmRequest.username) >> false
        farmRepository.existsByFarmName(signUpFarmRequest.farmName) >> false
        roleRepository.findByName(ERole.ROLE_FARM_MANAGER) >> Optional.of(new Role(id: 1, name: ERole.ROLE_FARM_MANAGER))

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

        addressRepository.save(_ as Address) >> new Address(id: 1)
        farmRepository.save(_ as Farm) >> new Farm(id: 1, farmName: signUpFarmRequest.farmName)

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
