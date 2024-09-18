package com.efarm.efarmbackend.repository.farm

import com.efarm.efarmbackend.model.farm.ActivationCode
import spock.lang.Specification

class ActivationCodeRepositorySpec extends Specification {

    // returns activation code by code string
    def "should correctly return Activation Code - findByCode"() {
        given:
        ActivationCodeRepository activationCodeRepository = Mock(ActivationCodeRepository)
        ActivationCode activationCode = Mock(ActivationCode)
        String codeString = "nawbfhawuin"
        activationCode.getCode() >> codeString
        activationCodeRepository.findByCode(codeString) >> Optional.of(activationCode)

        when:
        Optional<ActivationCode> foundActivationCode = activationCodeRepository.findByCode(codeString)

        then:
        foundActivationCode.isPresent()
        foundActivationCode.get() == activationCode
    }

    def "should not find not existing code - findByCode"() {
        given:
        ActivationCodeRepository activationCodeRepository = Mock(ActivationCodeRepository)
        String codeString = "nawbfhawuin"
        activationCodeRepository.findByCode(codeString) >> Optional.empty()

        when:
        Optional<ActivationCode> foundActivationCode = activationCodeRepository.findByCode(codeString)

        then:
        !foundActivationCode.isPresent()
    }
}