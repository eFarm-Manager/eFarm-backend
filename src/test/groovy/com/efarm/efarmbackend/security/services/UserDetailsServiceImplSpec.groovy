package com.efarm.efarmbackend.security.services

import com.efarm.efarmbackend.repository.user.UserRepository
import com.efarm.efarmbackend.model.user.User
import com.efarm.efarmbackend.model.user.Role
import com.efarm.efarmbackend.model.user.ERole
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import spock.lang.Specification

import spock.lang.Subject

class UserDetailsServiceImplSpec extends Specification {

    UserRepository userRepository = Mock(UserRepository)

    UserDetailsServiceImpl userDetailsServiceImpl = new UserDetailsServiceImpl()

    def setup() {
        userDetailsServiceImpl.userRepository = userRepository
    }

    def "should properly find user by username service - loadUserByUsername"() {
        given:
        String usernameTest = 'userTest'
        User mockUser = Mock(User)
        Role role = Mock(Role)
        String roleName = "ROLE_FARM_MANAGER"
        mockUser.getUsername() >> usernameTest
        role.getName() >> ERole.valueOf(roleName) 
        mockUser.getRole() >> role

        userRepository.findByUsername(usernameTest) >> Optional.of(mockUser)

        when:
        UserDetails actual = userDetailsServiceImpl.loadUserByUsername(usernameTest)

        then:
        actual != null
        actual.username == usernameTest
    }

    def "should throw UsernameNotFoundException - loadUserByUsername"() {
        given:
        String usernameTest = 'user1'
        userRepository.findByUsername(usernameTest) >> Optional.empty()

        when:
        UserDetails actual = userDetailsServiceImpl.loadUserByUsername(usernameTest)

        then:
        thrown(UsernameNotFoundException)
    }

}
