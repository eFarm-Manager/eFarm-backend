package com.efarm.efarmbackend.service.facades

import com.efarm.efarmbackend.model.farm.ActivationCode
import com.efarm.efarmbackend.model.farm.Address
import com.efarm.efarmbackend.model.farm.Farm
import com.efarm.efarmbackend.model.user.User
import com.efarm.efarmbackend.payload.request.auth.SignupFarmRequest
import com.efarm.efarmbackend.payload.request.auth.SignupUserRequest
import com.efarm.efarmbackend.payload.request.auth.UpdateActivationCodeRequest
import com.efarm.efarmbackend.payload.request.farm.UpdateActivationCodeByLoggedOwnerRequest
import com.efarm.efarmbackend.payload.request.user.ChangePasswordRequest
import com.efarm.efarmbackend.payload.response.MessageResponse
import com.efarm.efarmbackend.repository.farm.ActivationCodeRepository
import com.efarm.efarmbackend.repository.farm.AddressRepository
import com.efarm.efarmbackend.repository.farm.FarmRepository
import com.efarm.efarmbackend.repository.user.UserRepository
import com.efarm.efarmbackend.security.services.UserDetailsImpl
import com.efarm.efarmbackend.service.auth.AuthFacade
import com.efarm.efarmbackend.service.farm.ActivationCodeService
import com.efarm.efarmbackend.service.auth.AuthService
import com.efarm.efarmbackend.service.farm.FarmService
import com.efarm.efarmbackend.service.user.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.context.SecurityContextHolder
import spock.lang.Specification
import spock.lang.Subject
import java.nio.file.AccessDeniedException
import com.efarm.efarmbackend.exception.UnauthorizedException

class AuthFacadeSpec extends Specification {

    def userRepository = Mock(UserRepository)
    def farmRepository = Mock(FarmRepository)
    def addressRepository = Mock(AddressRepository)
    def activationCodeRepository = Mock(ActivationCodeRepository)
    def authService = Mock(AuthService)
    def userService = Mock(UserService)
    def activationCodeService = Mock(ActivationCodeService)
    def farmService = Mock(FarmService)
    def authenticationManager = Mock(AuthenticationManager)

    @Subject
    AuthFacade authFacade = new AuthFacade(
            userRepository,
            farmRepository,
            addressRepository,
            activationCodeRepository,
            authService,
            userService,
            activationCodeService,
            farmService,
            authenticationManager
    )

    def setup() {
        SecurityContextHolder.clearContext()
    }

    /*
            RegisterUser
    */

    def "should register user successfully"() {
        given:
        SignupUserRequest signUpRequest = new SignupUserRequest(
                firstName: 'John',
                lastName: 'Doe',
                username: 'user',
                email: 'user@gmail.com',
                password: 'password',
                phoneNumber: '',
                role: 'ROLE_FARM_MANAGER'
        )

        User user = Mock(User)
        Farm farm = Mock(Farm)

        userRepository.existsByUsername(signUpRequest.getUsername()) >> false
        userService.createFarmUser(signUpRequest) >> user
        userService.getLoggedUserFarm() >> farm

        when:
        authFacade.registerUser(signUpRequest)

        then:
        1 * userRepository.save(_)
    }

    def "should return error if username already exists"() {
        given:
        SignupUserRequest signUpRequest = new SignupUserRequest(
                firstName: 'John',
                lastName: 'Doe',
                username: 'existingUser',
                email: 'newuser@example.com',
                password: 'password',
                phoneNumber: '123456789',
                role: 'ROLE_FARM_MANAGER'
        )
        userRepository.existsByUsername(signUpRequest.getUsername()) >> true

        when:
        authFacade.registerUser(signUpRequest)

        then:
        RuntimeException exception = thrown()
        exception.message == 'Podana nazwa użytkownika jest już zajęta'
    }

    def "should return error if farm retrieval fails"() {
        given:
        SignupUserRequest signUpRequest = new SignupUserRequest(
                firstName: 'John',
                lastName: 'Doe',
                username: 'newUser',
                email: 'newuser@example.com',
                password: 'password',
                phoneNumber: '123456789',
                role: 'ROLE_FARM_MANAGER'
        )
        User user = Mock(User)

        userRepository.existsByUsername(signUpRequest.getUsername()) >> false
        userService.createFarmUser(signUpRequest) >> user
        userService.getLoggedUserFarm() >> { throw new RuntimeException('Farm not found') }

        when:
        authFacade.registerUser(signUpRequest)

        then:
        RuntimeException exception = thrown()
        exception.message == 'Farm not found'
    }
    /*
        registerFarmAndFarmOwner
    */

    def "should handle signup farm and farm owner"() {
        given:
        SignupFarmRequest signUpFarmRequest = new SignupFarmRequest(
                firstName: 'John',
                lastName: 'Doe',
                username: 'newUser',
                email: 'newuser@example.com',
                password: 'password',
                phoneNumber: '123456789',
                farmName: 'NewFarm',
                activationCode: 'activation-code'
        )
        User user = Mock(User)
        ActivationCode activationCode = Mock(ActivationCode)
        activationCode.getId() >> 1
        Farm farm = Mock(Farm)
        Address address = Mock(Address)
        address.getId() >> 1

        userRepository.existsByUsername(signUpFarmRequest.getUsername()) >> false
        farmRepository.existsByFarmName(signUpFarmRequest.getFarmName()) >> false
        userService.createFarmOwner(signUpFarmRequest) >> user
        activationCodeRepository.findByCode(signUpFarmRequest.getActivationCode()) >> Optional.of(activationCode)
        activationCodeService.validateActivationCode(signUpFarmRequest.getActivationCode()) >> { }
        farmService.createFarm(signUpFarmRequest.getFarmName(), address.getId(), activationCode.getId()) >> farm
        activationCodeService.markActivationCodeAsUsed(signUpFarmRequest.getActivationCode()) >> { }

        when:
        authFacade.registerFarmAndFarmOwner(signUpFarmRequest)

        then:
        1 * addressRepository.save(_)
        1 * userRepository.save(_)
    }

    def "should return bad request when username is already taken"() {
        given:
        SignupFarmRequest signUpFarmRequest = new SignupFarmRequest(
                firstName: 'John',
                lastName: 'Doe',
                username: 'existingUser',
                email: 'newuser@example.com',
                password: 'password',
                phoneNumber: '123456789',
                farmName: 'NewFarm',
                activationCode: 'activation-code'
        )
        userRepository.existsByUsername(signUpFarmRequest.getUsername()) >> true

        when:
        authFacade.registerFarmAndFarmOwner(signUpFarmRequest)

        then:
        RuntimeException exception = thrown()
        exception.message == 'Wybrana nazwa użytkownika jest już zajęta'
    }

    def "should return bad request when farm name is already taken"() {
        given:
        SignupFarmRequest signUpFarmRequest = new SignupFarmRequest(
                firstName: 'John',
                lastName: 'Doe',
                username: 'newUser',
                email: 'newuser@example.com',
                password: 'password',
                phoneNumber: '123456789',
                farmName: 'ExistingFarm',
                activationCode: 'activation-code'
        )
        userRepository.existsByUsername(signUpFarmRequest.getUsername()) >> false
        farmRepository.existsByFarmName(signUpFarmRequest.getFarmName()) >> true

        when:
        authFacade.registerFarmAndFarmOwner(signUpFarmRequest)

        then:
        RuntimeException exception = thrown()
        exception.message == 'Wybrana nazwa farmy jest już zajęta'
    }

    def "should return bad request when activation code does not exist"() {
        given:
        SignupFarmRequest signUpFarmRequest = new SignupFarmRequest(
                firstName: 'John',
                lastName: 'Doe',
                username: 'newUser',
                email: 'newuser@example.com',
                password: 'password',
                phoneNumber: '123456789',
                farmName: 'NewFarm',
                activationCode: 'invalid-code'
        )
        userRepository.existsByUsername(signUpFarmRequest.getUsername()) >> false
        farmRepository.existsByFarmName(signUpFarmRequest.getFarmName()) >> false
        activationCodeRepository.findByCode(signUpFarmRequest.getActivationCode()) >> Optional.empty()

        activationCodeService.validateActivationCode(signUpFarmRequest.getActivationCode()) >> { throw new RuntimeException('Podany kod aktywacyjny nie istnieje!') }

        when:
        authFacade.registerFarmAndFarmOwner(signUpFarmRequest)

        then:
        RuntimeException exception = thrown()
        exception.message == 'Podany kod aktywacyjny nie istnieje!'
    }

    def "should return bad request when activation code is used"() {
        given:
        SignupFarmRequest signUpFarmRequest = new SignupFarmRequest(
                firstName: 'John',
                lastName: 'Doe',
                username: 'newUser',
                email: 'newuser@example.com',
                password: 'password',
                phoneNumber: '123456789',
                farmName: 'NewFarm',
                activationCode: 'invalid-code'
        )
        ActivationCode activationCode = Mock(ActivationCode)
        activationCode.getIsUsed() >> true

        userRepository.existsByUsername(signUpFarmRequest.getUsername()) >> false
        farmRepository.existsByFarmName(signUpFarmRequest.getFarmName()) >> false
        activationCodeRepository.findByCode(signUpFarmRequest.getActivationCode()) >> Optional.of(activationCode)
        activationCodeService.validateActivationCode(signUpFarmRequest.getActivationCode()) >> { throw new RuntimeException('Podany kod aktywacyjny został już wykorzystany!') }

        when:
        authFacade.registerFarmAndFarmOwner(signUpFarmRequest)

        then:
        RuntimeException exception = thrown()
        exception.message == 'Podany kod aktywacyjny został już wykorzystany!'
    }

    def "should return bad request when marking activation code as used fails"() {
        given:
        SignupFarmRequest signUpFarmRequest = new SignupFarmRequest(
                firstName: 'John',
                lastName: 'Doe',
                username: 'newUser',
                email: 'newuser@example.com',
                password: 'password',
                phoneNumber: '123456789',
                farmName: 'NewFarm',
                activationCode: 'activation-code'
        )
        ActivationCode activationCode = Mock(ActivationCode)
        activationCode.getId() >> 1
        Farm farm = Mock(Farm)
        Address address = Mock(Address)
        address.getId() >> 1
        User user = Mock(User)

        userRepository.existsByUsername(signUpFarmRequest.getUsername()) >> false
        farmRepository.existsByFarmName(signUpFarmRequest.getFarmName()) >> false
        userService.createFarmOwner(signUpFarmRequest) >> user
        activationCodeRepository.findByCode(signUpFarmRequest.getActivationCode()) >> Optional.of(activationCode)
        activationCodeService.validateActivationCode(signUpFarmRequest.getActivationCode()) >> { }
        farmService.createFarm(signUpFarmRequest.getFarmName(), address.getId(), activationCode.getId()) >> farm

        activationCodeService.markActivationCodeAsUsed(signUpFarmRequest.getActivationCode()) >> { throw new RuntimeException('Activation code not found') }

        when:
        authFacade.registerFarmAndFarmOwner(signUpFarmRequest)

        then:
        RuntimeException exception = thrown()
        exception.message == 'Activation code not found'
    }
    /*
        updateActivationCode
    */

    def "should update activation code by owner"() {
        given:
        UpdateActivationCodeRequest updateActivationCodeRequest = new UpdateActivationCodeRequest(
                username: 'user',
                password: 'password',
                newActivationCode: 'newActivationCode'
        )
        List<String> roles = ['ROLE_FARM_OWNER']
        UserDetailsImpl userDetails = Mock(UserDetailsImpl)
        userDetails.getId() >> 1
        userDetails.getUsername() >> updateActivationCodeRequest.getUsername()
        Farm farm = Mock(Farm)
        farm.getId() >> 1

        authService.authenticateUserByUpdateCodeRequest(updateActivationCodeRequest) >> userDetails
        userService.getLoggedUserRoles(userDetails) >> roles
        userService.getUserFarmById(Long.valueOf(userDetails.getId())) >> farm

        when:
        authFacade.updateActivationCode(updateActivationCodeRequest)

        then:
        1 * activationCodeService.updateActivationCodeForFarm(updateActivationCodeRequest.getNewActivationCode(), farm.getId(), userDetails.getUsername())
    }

    def "should return UNAUTHORIZED if user is not a farm owner"() {
        given:
        UpdateActivationCodeRequest updateActivationCodeRequest = new UpdateActivationCodeRequest(
                username: 'user',
                password: 'password',
                newActivationCode: 'newActivationCode'
        )
        List<String> roles = ['ROLE_FARM_MANAGER']
        UserDetailsImpl userDetails = Mock(UserDetailsImpl)
        userDetails.getId() >> 1

        authService.authenticateUserByUpdateCodeRequest(updateActivationCodeRequest) >> userDetails
        userService.getLoggedUserRoles(userDetails) >> roles

        when:
        authFacade.updateActivationCode(updateActivationCodeRequest)

        then:
        AccessDeniedException exception = thrown()
        exception.message == 'Brak uprawnień'
    }

    def "should return BAD_REQUEST if activation code update fails"() {
        given:
        UpdateActivationCodeRequest updateActivationCodeRequest = new UpdateActivationCodeRequest(
                username: 'user',
                password: 'password',
                newActivationCode: 'invalidActivationCode'
        )
        List<String> roles = ['ROLE_FARM_OWNER']
        UserDetailsImpl userDetails = Mock(UserDetailsImpl)
        userDetails.getId() >> 1
        userDetails.getUsername() >> updateActivationCodeRequest.getUsername()
        Farm farm = Mock(Farm)
        farm.getId() >> 1

        authService.authenticateUserByUpdateCodeRequest(updateActivationCodeRequest) >> userDetails
        userService.getLoggedUserRoles(userDetails) >> roles
        userService.getUserFarmById(Long.valueOf(userDetails.getId())) >> farm
        activationCodeService.updateActivationCodeForFarm(updateActivationCodeRequest.getNewActivationCode(), farm.getId(), userDetails.getUsername()) >> {
            throw new RuntimeException('Podany kod aktywacyjny nie istnieje!')
        }

        when:
        authFacade.updateActivationCode(updateActivationCodeRequest)

        then:
        RuntimeException exception = thrown()
        exception.message ==  'Podany kod aktywacyjny nie istnieje!'
    }
    /*
        updateActivationCodeByLoggedOwner
    */

    def "should update activation code when password is valid and no validation errors"() {
        given:
        UpdateActivationCodeByLoggedOwnerRequest request = Mock(UpdateActivationCodeByLoggedOwnerRequest) {
            getPassword() >> 'validPassword'
            getNewActivationCode() >> 'newActivationCode123'
        }
        Integer farmId = 1
        String username = 'loggedOwner'
        Farm farm = Mock(Farm) { getId() >> farmId }

        userService.isPasswordValidForLoggedUser(request.getPassword()) >> true
        userService.getLoggedUserFarm() >> farm
        userService.getLoggedUser() >> Mock(User) { getUsername() >> username }

        when:
        authFacade.updateActivationCodeByLoggedOwner(request)

        then:
        1 * activationCodeService.updateActivationCodeForFarm(request.getNewActivationCode(), farmId, username)
    }

    def "should return unauthorized when password is invalid"() {
        given:
        UpdateActivationCodeByLoggedOwnerRequest request = Mock(UpdateActivationCodeByLoggedOwnerRequest) {
            getPassword() >> 'invalidPassword'
            getNewActivationCode() >> 'newActivationCode123'
        }

        userService.isPasswordValidForLoggedUser(request.getPassword()) >> false

        when:
        authFacade.updateActivationCodeByLoggedOwner(request)

        then:
        UnauthorizedException exception = thrown()
        exception.message == 'Nieprawidłowe hasło'
    }

    /*
        changePassword
    */

    def "should successfully change password when valid"() {
        given:
        String currentPassword = 'password123'
        String newPassword = 'newPassword123'
        ChangePasswordRequest request = Mock(ChangePasswordRequest) {
            getCurrentPassword() >> currentPassword
            getNewPassword() >> newPassword
        }

        userService.isPasswordValidForLoggedUser(currentPassword) >> true

        when:
        authFacade.changePassword(request)

        then:
        1 * userService.updatePasswordForLoggedUser(request.getNewPassword())
    }

    def "should return unauthorized if current password is invalid"() {
        given:
        String currentPassword = 'wrongPassword'
        String newPassword = 'newPassword123'
        ChangePasswordRequest request = Mock(ChangePasswordRequest) {
            getCurrentPassword() >> currentPassword
            getNewPassword() >> newPassword
        }
        userService.isPasswordValidForLoggedUser(currentPassword) >> false

        when:
        authFacade.changePassword(request)

        then:
        UnauthorizedException exception = thrown()
        exception.message == 'Podano nieprawidłowe aktualne hasło'
    }

    def "should return runtime exceptioon when new password is same as old password"() {
        given:
        String currentPassword = 'password123'
        String newPassword = 'password123'
        ChangePasswordRequest request = Mock(ChangePasswordRequest) {
            getCurrentPassword() >> currentPassword
            getNewPassword() >> newPassword
        }

        userService.isPasswordValidForLoggedUser(currentPassword) >> true

        when:
        authFacade.changePassword(request)

        then:
        RuntimeException exception = thrown()
        exception.message == 'Nowe hasło nie może być takie samo jak poprzednie'
    }

}
