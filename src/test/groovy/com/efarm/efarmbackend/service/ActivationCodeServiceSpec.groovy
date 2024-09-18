package com.efarm.efarmbackend.service

import com.efarm.efarmbackend.model.farm.ActivationCode
import com.efarm.efarmbackend.payload.response.MessageResponse
import com.efarm.efarmbackend.repository.farm.ActivationCodeRepository
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import java.time.LocalDate

class ActivationCodeServiceSpec extends Specification {

    def activationCodeRepository = Mock(ActivationCodeRepository)

    @Subject
    ActivationCodeService activationCodeService = new ActivationCodeService(
            activationCodeRepository: activationCodeRepository
    )

    def setup() {
        SecurityContextHolder.clearContext()
    }

    @Unroll
    def "should handle correct code"() {
        given:
        String activationCodeName = "validCode"

        ActivationCode activationCode = Mock(ActivationCode)
        activationCode.getCodeName() >> activationCodeName
        activationCode.getExpireDate() >> LocalDate.now().plusDays(1)
        activationCode.getIsUsed() >> false
        activationCodeRepository.findByCode(activationCodeName) >> Optional.of(activationCode)

        when:
        ResponseEntity<MessageResponse> response = activationCodeService.checkActivationCode(activationCodeName)

        then:
        response == ResponseEntity.ok().build()
    }

    @Unroll
    def "should handle invalid code"() {
        given:
        String activationCodeName = "validCode"

        activationCodeRepository.findByCode(activationCodeName) >> Optional.empty()

        when:
        ResponseEntity<MessageResponse> response = activationCodeService.checkActivationCode(activationCodeName)

        then:
        response.statusCodeValue == 400
        response.body.message == "Activation code does not exist."
    }

    @Unroll
    def "should handle expired code"() {
        given:
        String activationCodeName = "validCode"

        ActivationCode activationCode = Mock(ActivationCode)
        activationCode.getCodeName() >> activationCodeName
        activationCode.getExpireDate() >> LocalDate.now().minusDays(1)
        activationCode.getIsUsed() >> false
        activationCodeRepository.findByCode(activationCodeName) >> Optional.of(activationCode)

        when:
        ResponseEntity<MessageResponse> response = activationCodeService.checkActivationCode(activationCodeName)

        then:
        response.statusCodeValue == 400
        response.body.message == "Activation code has expired."
    }

    @Unroll
    def "should handle used code"() {
        given:
        String activationCodeName = "validCode"

        ActivationCode activationCode = Mock(ActivationCode)
        activationCode.getCodeName() >> activationCodeName
        activationCode.getExpireDate() >> LocalDate.now().plusDays(1)
        activationCode.getIsUsed() >> true
        activationCodeRepository.findByCode(activationCodeName) >> Optional.of(activationCode)

        when:
        ResponseEntity<MessageResponse> response = activationCodeService.checkActivationCode(activationCodeName)

        then:
        response.statusCodeValue == 400
        response.body.message == "Activation code has already been used."
    }

    @Unroll
    def "should handle using code correctly"() {
        given:
        String activationCodeName = "validCode"

        ActivationCode activationCode = Mock(ActivationCode)
        activationCode.getCodeName() >> activationCodeName
        activationCode.getExpireDate() >> LocalDate.now().plusDays(1)
        activationCode.getIsUsed() >> false
        activationCodeRepository.findByCode(activationCodeName) >> Optional.of(activationCode)

        when:
        activationCodeService.markActivationCodeAsUsed(activationCodeName)

        then:
        1 * activationCode.setIsUsed(true)
        1 * activationCodeRepository.save(activationCode)
    }

    @Unroll
    def "should handle using code when not found"() {
        given:
        String activationCodeName = "validCode"

        activationCodeRepository.findByCode(activationCodeName) >> Optional.empty()

        when:
        activationCodeService.markActivationCodeAsUsed(activationCodeName)

        then:
        thrown(RuntimeException)
    }
}