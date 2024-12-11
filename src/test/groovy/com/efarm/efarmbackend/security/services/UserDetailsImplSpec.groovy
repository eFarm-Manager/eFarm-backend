package com.efarm.efarmbackend.security.services

import com.efarm.efarmbackend.model.user.ERole
import com.efarm.efarmbackend.model.user.Role
import com.efarm.efarmbackend.model.user.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import spock.lang.Specification

class UserDetailsImplSpec extends Specification {

    //checks if user details are correctly initialized
    def "should properly initialize UserDetailsImpl object via constructor"() {
        given:
        Integer id = 1
        String username = 'user1'
        String email = 'user1@gmail.com'
        String password = 'pass123!'
        Collection<? extends GrantedAuthority> authorities = [new SimpleGrantedAuthority('ROLE_FARM_MANAGER')]

        when:
        UserDetailsImpl userDetails = new UserDetailsImpl(id, username, email, password, authorities)

        then:
        userDetails.id == id
        userDetails.username == username
        userDetails.email == email
        userDetails.password == password
        userDetails.authorities == authorities
    }

    //checks the correctness build function
    def "should correctly build UserDetailsImpl from User object"() {
        given:
        User user = Mock(User)
        Role role = Mock(Role)
        String roleName = 'ROLE_FARM_MANAGER'

        user.getId() >> 1
        user.getUsername() >> 'user1'
        user.getEmail() >> 'user1@gmail.com'
        user.getPassword() >> 'pass123!'
        role.getName() >> ERole.valueOf(roleName)
        user.getRole() >> role

        when:
        UserDetailsImpl userDetails = UserDetailsImpl.build(user)

        then:
        userDetails.id == 1
        userDetails.username == 'user1'
        userDetails.email == 'user1@gmail.com'
        userDetails.password == 'pass123!'
        userDetails.authorities.size() == 1
        userDetails.authorities[0].authority == roleName
    }

    //checks if same user is equal to itself
    def "should return true when UserDetailsImpl compared to itself - equals"() {
        given:
        UserDetailsImpl user1 = new UserDetailsImpl(1, 'user1', 'user1@gmail.com',
                'pass123!', [new SimpleGrantedAuthority('ROLE_FARM_MANAGER')])

        expect:
        user1.equals(user1)
    }

    //checks if different users are equal
    def "should return false when UserDetailsImpl compared to another UserDetailsImpl with different id - equals"() {
        given:
        UserDetailsImpl user1 = new UserDetailsImpl(1, 'user1', 'user1@gmail.com',
                'pass123!', [new SimpleGrantedAuthority('ROLE_FARM_MANAGER')])
        UserDetailsImpl user2 = new UserDetailsImpl(2, 'user2', 'user2@gmail.com',
                'pass123!', [new SimpleGrantedAuthority('ROLE_FARM_MANAGER')])

        expect:
        !user1.equals(user2)
    }

    //checks if existing user is equal to null
    def "should return false when UserDetailsImpl compared to null - equals"() {
        given:
        UserDetailsImpl user1 = new UserDetailsImpl(1, 'user1', 'user1@gmail.com',
                'pass123!', [new SimpleGrantedAuthority('ROLE_FARM_MANAGER')])

        expect:
        !user1.equals(null)
    }

    //checks if Object with field id is equal to user details with same id
    def "should return false when UserDetailsImpl compared to object - equals"() {
        given:
        UserDetailsImpl user1 = new UserDetailsImpl(1, 'user1', 'user1@gmail.com',
                'pass123!', [new SimpleGrantedAuthority('ROLE_FARM_MANAGER')])
        Object obj = new Expando()
        obj.id = 1

        expect:
        !user1.equals(obj)
    }

}

