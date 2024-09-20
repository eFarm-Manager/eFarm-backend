package com.efarm.efarmbackend.controller

import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.user.Role;
import com.efarm.efarmbackend.model.user.ERole;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.model.user.UserDTO;
import com.efarm.efarmbackend.service.FarmService;
import com.efarm.efarmbackend.service.UserService;
import com.efarm.efarmbackend.payload.response.UserInfoResponse
import com.efarm.efarmbackend.payload.response.MessageResponse
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf


class FarmControllerSpec extends Specification {

    def farmService = Mock(FarmService)
    def userService = Mock(UserService)

    @Subject
    FarmController farmController = new FarmController(
        farmService: farmService,
        userService: userService
    )
    def setup() {
        SecurityContextHolder.clearContext()
    } 

    @Unroll
    def "should get users by farm id"() {
        given:
        Farm farm = Mock(Farm)
        farm.getFarmName() >> "farm1"
        farm.getId() >> 1

        Role role1 = Mock(Role)
        Role role3 = Mock(Role)
        role1.toString() >> "Operator"
        role3.toString() >> "Owner"

        User user1 = Mock(User)
        User user2 = Mock(User)
        user1.getUsername() >> "user1"
        user1.getRole() >> role1
        user1.getEmail() >> "user1@gmail.com"
        user1.getFirstName() >> "firstName1"
        user1.getLastName() >> "lastName1"
        user1.getPhoneNumber() >> "123456789"
        user1.getIsActive() >> true
        user1.getFarm() >> farm

        user2.getUsername() >> "user2"
        user2.getRole() >> role3
        user2.getEmail() >> "user2@gmail.com"
        user2.getFirstName() >> "firstName2"
        user2.getLastName() >> "lastName2"
        user2.getPhoneNumber() >> null
        user2.getIsActive() >> true
        user2.getFarm() >> farm
 
        userService.getLoggedUserFarm() >> farm
        farmService.getUsersByFarmId(farm.getId()) >> [user1,user2]

        when:
        ResponseEntity<List<UserDTO>> response = farmController.getFarmUsersByFarmId()

        then:
        response.getBody().size() == 2
        response.getBody()[0].getUsername() == "user1"
        response.getBody()[0].getRole() == "Operator"
        response.getBody()[0].getPhone() == "123456789"
        response.getBody()[1].getUsername() == "user2"
        response.getBody()[1].getRole() == "Owner"
        response.getBody()[1].getPhone() == null     
    }
}