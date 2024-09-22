package com.efarm.efarmbackend.service.facades

import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.model.user.Role
import com.efarm.efarmbackend.model.user.ERole
import com.efarm.efarmbackend.model.user.UserDTO;
import com.efarm.efarmbackend.service.FarmService;
import com.efarm.efarmbackend.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.http.ResponseEntity
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll
import java.net.URI;

import java.time.LocalDate

class FarmFacadeSpec extends Specification {

    def userService = Mock(UserService)
    def farmService = Mock(FarmService)

    @Subject
    FarmFacade farmFacade = new FarmFacade(
            userService: userService,
            farmService: farmService,
    )

    def setup() {
        SecurityContextHolder.clearContext()
    }

    def "should return farm users by farm id"() {
        given:
        Farm farm1 = Mock(Farm)
        farm1.getId() >> 1
        Farm farm2 = Mock(Farm)
        farm2.getId() >> 2

        User user1 = Mock(User)
        user1.getUsername() >> "user1"
        user1.getEmail() >> "user1@example.com"
        user1.getFirstName() >> "John"
        user1.getLastName() >> "Doe"
        user1.getPhoneNumber() >> "123456789"
        user1.getIsActive() >> true
        user1.getRole() >> Mock(Role) {
            toString() >> "ROLE_FARM_OWNER"
        }
        user1.getFarm() >> farm1
        User user2 = Mock(User)
        user2.getUsername() >> "user2"
        user2.getEmail() >> "user2@example.com"
        user2.getFirstName() >> "Jane"
        user2.getLastName() >> "Smith"
        user2.getPhoneNumber() >> ""
        user2.getIsActive() >> false
        user2.getRole() >> Mock(Role) {
            toString() >> "ROLE_FARM_EQUIPMENT_OPERATOR"
        }    
        user2.getFarm() >> farm1
        User user3 = Mock(User)
        user3.getFarm() >> farm2

        userService.getLoggedUserFarm() >> farm1
        farmService.getUsersByFarmId(farm1.getId()) >> [user1,user2]

        when:
        ResponseEntity<List<UserDTO>> response = farmFacade.getFarmUsersByFarmId()

        then:
        response.statusCodeValue == 200
        response.body.size() == 2
        response.body[0].username == "user1"
        response.body[1].username == "user2"
    }
    
}