package com.efarm.efarmbackend.repository.user

import com.efarm.efarmbackend.model.user.User
import com.efarm.efarmbackend.model.user.ERole
import com.efarm.efarmbackend.model.farm.Farm
import spock.lang.Specification

class UserRepositorySpec extends Specification {

    UserRepository userRepository = Mock(UserRepository)

    // checks if user is correctly found based on username
    def "should correctly return user - findByUsername"() {
        given:
        User user = Mock(User)

        String username = "user1"
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
        String usernameTest = "user2"

        userRepository.findByUsername(usernameTest) >> Optional.empty()

        when:
        Optional<User> foundUser = userRepository.findByUsername(usernameTest)

        then:
        !foundUser.isPresent()
    }

    // checks if existing username exists with function existsByUsername
    def "should return true for existing user - existsByUsername"() {
        given:
        User user = Mock(User)

        String username = "user1"
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
        String usernameTest = "user2"

        userRepository.existsByUsername(usernameTest) >> false

        when:
        Boolean existsByUsername = userRepository.existsByUsername(usernameTest)

        then:
        existsByUsername == false
    }

    def "should return users from farm" () {
        given:
        Farm farm1 = Mock(Farm)
        farm1.getId() >> 1
        Farm farm2 = Mock(Farm)
        farm2.getId() >> 2

        User user1 = Mock(User)
        user1.getFarm() >> farm1
        User user2 = Mock(User)
        user2.getFarm() >> farm1
        User user3 = Mock(User)
        user3.getFarm() >> farm2

        userRepository.findByFarmId(1) >> [user1,user2]
        when:
        List<User> usersInFarm1 = userRepository.findByFarmId(1)

        then:
        usersInFarm1.size() == 2
        usersInFarm1.contains(user1)
        usersInFarm1.contains(user2)
        !usersInFarm1.contains(user3)
        usersInFarm1.every { it.getFarm() == farm1 }
    }
}