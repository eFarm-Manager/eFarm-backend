package com.efarm.efarmbackend.repository.user

import com.efarm.efarmbackend.model.user.Role
import com.efarm.efarmbackend.model.user.User
import com.efarm.efarmbackend.model.user.ERole
import spock.lang.Specification

class UserRepositorySpec extends Specification{

    // checks if user is correctly found based on username
    def "should correctly return user - findByUsername"() {
        given:
        UserRepository userRepository = Mock(UserRepository)
        User user = Mock(User)

        String username="user1"
        user.getUsername() >> username

        userRepository.findByUsername(username) >> Optional.of(user)

        when:
        Optional<User> foundUser = userRepository.findByUsername(username)

        then:
        foundUser.isPresent()
        foundUser.get() == user 
    }

    // checks that if user with certain username doesnt exist then findByUsername returns null
    def "should not return user with username that does not exist - findByUsername"() {
        given:
        UserRepository userRepository = Mock(UserRepository)
        String usernameTest="user2"

        userRepository.findByUsername(usernameTest) >> Optional.empty()

        when:
        Optional<User> foundUser = userRepository.findByUsername(usernameTest)

        then:
        !foundUser.isPresent()
    }

    // checks if existing username exists with function existsByUsername
    def "should return true for existing user - existsByUsername"() {
        given:
        UserRepository userRepository = Mock(UserRepository)
        User user = Mock(User)

        String username="user1"
        user.getUsername() >> username

        userRepository.existsByUsername(username) >> true

        when:
        Boolean existsByUsername = userRepository.existsByUsername(username)

        then:
        existsByUsername == true
    }

    // checks if non existing user with certain username does not exists with function existsByUsername
    def "should return false for non existing user - existsByUsername"() {
        given:
        UserRepository userRepository = Mock(UserRepository)
        String usernameTest="user2"

        userRepository.existsByUsername(usernameTest) >> false

        when:
        Boolean existsByUsername = userRepository.existsByUsername(usernameTest)

        then:
        existsByUsername == false
    }

    
}