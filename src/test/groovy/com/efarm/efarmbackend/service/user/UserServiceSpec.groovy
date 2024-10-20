package com.efarm.efarmbackend.service

import com.efarm.efarmbackend.model.farm.Farm
import com.efarm.efarmbackend.model.user.ERole
import com.efarm.efarmbackend.model.user.Role
import com.efarm.efarmbackend.model.user.User
import com.efarm.efarmbackend.payload.request.auth.SignupFarmRequest
import com.efarm.efarmbackend.payload.request.auth.SignupRequest
import com.efarm.efarmbackend.repository.user.RoleRepository
import com.efarm.efarmbackend.repository.user.UserRepository
import com.efarm.efarmbackend.security.services.UserDetailsImpl
import com.efarm.efarmbackend.service.user.UserService
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import spock.lang.Specification
import spock.lang.Subject

class UserServiceSpec extends Specification {

    def userRepository = Mock(UserRepository)
    def roleRepository = Mock(RoleRepository)
    def encoder = Mock(PasswordEncoder)

    Role class_role_manager
    Role class_role_operator
    Role class_role_owner

    @Subject
    UserService userService = new UserService(
            userRepository: userRepository,
            roleRepository: roleRepository,
            encoder: encoder
    )

    def setup() {
        SecurityContextHolder.clearContext()
        //Mock Role
        String roleNameManager = 'ROLE_FARM_MANAGER'
        String roleOperator = 'ROLE_FARM_EQUIPMENT_OPERATOR'
        String roleOwner = 'ROLE_FARM_OWNER'
        class_role_manager = Mock(Role) {
            getId() >> 2
            getName() >> ERole.valueOf(roleNameManager)
        }
        class_role_operator = Mock(Role) {
            getId() >> 1
            getName() >> ERole.valueOf(roleOperator)
        }
        class_role_owner = Mock(Role) {
            getId() >> 3
            getName() >> ERole.valueOf(roleOwner)
        }
    }

    def "should handle creation of farm owner"() {
        given:
        def signUpFarmRequest = new SignupFarmRequest(
                firstName: 'John',
                lastName: 'Doe',
                username: 'user',
                email: 'user@example.com',
                password: 'password',
                phoneNumber: '',
                farmName: 'FarmName',
                activationCode: 'activation-code'
        )
        roleRepository.findByName(ERole.ROLE_FARM_OWNER) >> Optional.of(class_role_owner)
        roleRepository.findByName(ERole.ROLE_FARM_MANAGER) >> Optional.of(class_role_manager)
        roleRepository.findByName(ERole.ROLE_FARM_EQUIPMENT_OPERATOR) >> Optional.of(class_role_operator)

        encoder.encode(signUpFarmRequest.getPassword()) >> 'encodedPassword'

        when:
        User newFarmOwner = userService.createFarmOwner(signUpFarmRequest)

        then:
        newFarmOwner.getUsername() == 'user'
        newFarmOwner.getRole().getName() == ERole.ROLE_FARM_OWNER
        newFarmOwner.getPassword() == 'encodedPassword'
    }

    def "should handle create farm user"() {
        given:
        SignupRequest signUpRequest = new SignupRequest(
                firstName: 'John',
                lastName: 'Doe',
                username: 'newUser',
                email: 'newuser@example.com',
                password: 'password',
                phoneNumber: '',
                role: 'ROLE_FARM_OWNER'
        )
        roleRepository.findByName(ERole.ROLE_FARM_OWNER) >> Optional.of(class_role_owner)
        roleRepository.findByName(ERole.ROLE_FARM_MANAGER) >> Optional.of(class_role_manager)
        roleRepository.findByName(ERole.ROLE_FARM_EQUIPMENT_OPERATOR) >> Optional.of(class_role_operator)

        encoder.encode(signUpRequest.getPassword()) >> 'encodedPassword'

        when:
        User newUser = userService.createFarmUser(signUpRequest)

        then:
        newUser.getUsername() == 'newUser'
        newUser.getRole().getName() == ERole.ROLE_FARM_OWNER
        newUser.getPassword() == 'encodedPassword'
    }

    def "should handle returning current logged user"() {
        given:
        User currentUser = Mock(User)
        currentUser.getUsername() >> 'currentUser'
        currentUser.getId() >> 1
        currentUser.getEmail() >> 'test@gmail.com'
        currentUser.getPassword() >> 'fwafwafa312z'
        currentUser.getRole() >> class_role_manager
        UserDetailsImpl currentUserDetails = UserDetailsImpl.build(currentUser)

        Authentication authentication = Mock(Authentication) {
            getPrincipal() >> currentUserDetails
        }
        SecurityContextHolder.getContext().setAuthentication(authentication)

        userRepository.findById(Long.valueOf(currentUserDetails.getId())) >> Optional.of(currentUser)

        when:
        User currentUserReturned = userService.getLoggedUser()

        then:
        currentUserReturned.getUsername() == currentUser.getUsername()
    }

    def "should handle returning current users farm"() {
        given:
        Farm currentFarm = Mock(Farm)
        currentFarm.getId() >> 1
        currentFarm.getFarmName() >> 'uniqueFarmName'

        User currentUser = Mock(User)
        currentUser.getUsername() >> 'currentUser'
        currentUser.getId() >> 1
        currentUser.getFarm() >> currentFarm
        currentUser.getEmail() >> 'test@gmail.com'
        currentUser.getPassword() >> 'fwafwafa312z'
        currentUser.getRole() >> class_role_manager
        UserDetailsImpl currentUserDetails = UserDetailsImpl.build(currentUser)

        Authentication authentication = Mock(Authentication) {
            getPrincipal() >> currentUserDetails
        }
        SecurityContextHolder.getContext().setAuthentication(authentication)

        userRepository.findById(Long.valueOf(currentUserDetails.getId())) >> Optional.of(currentUser)

        when:
        Farm currentFarmReturned = userService.getLoggedUserFarm()

        then:
        currentFarmReturned.getFarmName() == currentFarm.getFarmName()
    }

    def "should handle no current user details"() {
        given:
        Authentication authentication = Mock(Authentication) {
            getPrincipal() >> null
        }

        SecurityContextHolder.getContext().setAuthentication(authentication)

        when:
        userService.getLoggedUserFarm()

        then:
        thrown(RuntimeException)
    }

    def "should get user farm by id"() {
        given:
        Farm farm = Mock(Farm)
        User user = Mock(User)
        user.getId() >> 1
        user.getFarm() >> farm
        userRepository.findById(Long.valueOf(user.getId())) >> Optional.of(user)

        when:
        Farm userFarmById = userService.getUserFarmById(Long.valueOf(user.getId()))

        then:
        userFarmById == farm
    }

    def "should get logged user roles"() {
        given:
        GrantedAuthority authority = Mock(GrantedAuthority)
        authority.getAuthority() >> 'ROLE_FARM_MANAGER'
        UserDetailsImpl userDetails = Mock(UserDetailsImpl)
        userDetails.getAuthorities() >> [authority]

        when:
        List<String> roles = userService.getLoggedUserRoles(userDetails)

        then:
        roles == ['ROLE_FARM_MANAGER']
    }

    def "should return true when password is valid"() {
        given:
        String providedPassword = 'password123'
        String encodedPassword = 'encodedPassword123'
        User loggedUser = Mock(User) {
            getPassword() >> encodedPassword
        }
        Authentication authentication = Mock(Authentication) {
            getPrincipal() >> Mock(UserDetailsImpl) {
                getId() >> 1
            }
        }
        SecurityContextHolder.getContext().setAuthentication(authentication)
        userRepository.findById(1) >> Optional.of(loggedUser)
        encoder.matches(providedPassword, encodedPassword) >> true

        when:
        Boolean result = userService.isPasswordValidForLoggedUser(providedPassword)

        then:
        result == true
    }

    def "should return false when password is invalid"() {
        given:
        String providedPassword = 'wrongPassword'
        String encodedPassword = 'encodedPassword123'
        User loggedUser = Mock(User) {
            getPassword() >> encodedPassword
        }
        Authentication authentication = Mock(Authentication) {
            getPrincipal() >> Mock(UserDetailsImpl) {
                getId() >> 1
            }
        }
        SecurityContextHolder.getContext().setAuthentication(authentication)
        userRepository.findById(1) >> Optional.of(loggedUser)
        encoder.matches(providedPassword, encodedPassword) >> false

        when:
        Boolean result = userService.isPasswordValidForLoggedUser(providedPassword)

        then:
        result == false
    }

    def "should update password successfully"() {
        given:
        String newPassword = 'newPassword123'
        String encodedPassword = 'encodedNewPassword123'
        User loggedUser = Mock(User)
        Authentication authentication = Mock(Authentication) {
            getPrincipal() >> Mock(UserDetailsImpl) {
                getId() >> 1
            }
        }
        SecurityContextHolder.getContext().setAuthentication(authentication)
        userRepository.findById(1) >> Optional.of(loggedUser)
        encoder.encode(newPassword) >> encodedPassword

        when:
        userService.updatePasswordForLoggedUser(newPassword)

        then:
        1 * loggedUser.setPassword(encodedPassword)
        1 * userRepository.save(loggedUser)
    }

    def "should throw error if authentication is null"() {
        given:
        SecurityContextHolder.getContext().setAuthentication(null)

        when:
        userService.updatePasswordForLoggedUser('newPassword123')

        then:
        thrown(RuntimeException)
        0 * userRepository.save(_)
    }

    def "should correctly return farm owner"() {
        given:
        String role = 'ROLE_FARM_OWNER'
        roleRepository.findByName(ERole.ROLE_FARM_OWNER) >> Optional.of(class_role_owner)

        when:
        Role assignRole = userService.assignUserRole(role)

        then:
        assignRole.getName() == ERole.ROLE_FARM_OWNER
    }

    def "should correctly return farm manager"() {
        given:
        String role = 'ROLE_FARM_MANAGER'
        roleRepository.findByName(ERole.ROLE_FARM_MANAGER) >> Optional.of(class_role_manager)

        when:
        Role assignRole = userService.assignUserRole(role)

        then:
        assignRole.getName() == ERole.ROLE_FARM_MANAGER
    }

    def "should correctly return farm operator when string not owner or manager"() {
        given:
        roleRepository.findByName(ERole.ROLE_FARM_EQUIPMENT_OPERATOR) >> Optional.of(class_role_operator)

        when:
        Role assignRole = userService.assignUserRole('')

        then:
        assignRole.getName() == ERole.ROLE_FARM_EQUIPMENT_OPERATOR
    }

    def "should get active user by id"() {
        given:
        UserDetailsImpl userDetails = Mock(UserDetailsImpl) {
            getId() >> 1
        }
        User user = Mock(User) {
            getId() >> 1
            getIsActive() >> true
        }
        userRepository.findById(Long.valueOf(userDetails.getId())) >> Optional.of(user)

        when:
        Optional<User> activeUser = userService.getActiveUserById(userDetails)

        then:
        activeUser.get() == user
    }

    def "should throw runtime exception when user is not active"() {
        given:
        UserDetailsImpl userDetails = Mock(UserDetailsImpl) {
            getId() >> 1
        }
        User user = Mock(User) {
            getId() >> 1
            getIsActive() >> false
        }
        userRepository.findById(Long.valueOf(userDetails.getId())) >> Optional.of(user)

        when:
        userService.getActiveUserById(userDetails)

        then:
        RuntimeException ex = thrown(RuntimeException)
        ex.message == 'Użytkownik jest nieaktywny!'
    }

    def "should return all owners for the specified farm"() {
        given:
        Integer farmId = 1
        User owner1 = new User(email: 'owner1@example.com')
        User owner2 = new User(email: 'owner2@example.com')
        userRepository.findOwnersForFarm(farmId) >> [owner1, owner2]

        when:
        List<User> owners = userService.getAllOwnersForFarm(farmId)

        then:
        owners.size() == 2
        owners[0].email == 'owner1@example.com'
        owners[1].email == 'owner2@example.com'
    }

}
