package com.efarm.efarmbackend.service

import com.efarm.efarmbackend.exception.TooManyRequestsException
import com.efarm.efarmbackend.model.farm.ActivationCode
import com.efarm.efarmbackend.model.farm.Farm
import com.efarm.efarmbackend.payload.response.MessageResponse
import com.efarm.efarmbackend.repository.farm.ActivationCodeRepository
import com.efarm.efarmbackend.repository.farm.FarmRepository
import com.efarm.efarmbackend.security.services.BruteForceProtectionService
import com.efarm.efarmbackend.service.farm.ActivationCodeService
import org.springframework.security.core.context.SecurityContextHolder
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDate

class ActivationCodeServiceSpec extends Specification {

    def activationCodeRepository = Mock(ActivationCodeRepository)
    def farmRepository = Mock(FarmRepository)
    def bruteForceProtectionService = Mock(BruteForceProtectionService)

    @Subject
    ActivationCodeService activationCodeService = new ActivationCodeService(
            activationCodeRepository: activationCodeRepository,
            farmRepository: farmRepository,
            bruteForceProtectionService: bruteForceProtectionService,
            daysToShowExpireActivationCodeNotification: 14,
    )

    def setup() {
        SecurityContextHolder.clearContext()
    }
    /*
    * validateActivationCode
    */

    def "should handle correct code"() {
        given:
        String activationCodeName = 'validCode'

        ActivationCode activationCode = Mock(ActivationCode)
        activationCode.getCode() >> activationCodeName
        activationCode.getExpireDate() >> LocalDate.now().plusDays(1)
        activationCode.getIsUsed() >> false
        activationCodeRepository.findByCode(activationCodeName) >> Optional.of(activationCode)

        when:
        activationCodeService.validateActivationCode(activationCodeName)

        then:
        noExceptionThrown()
    }

    def "should handle invalid code"() {
        given:
        String activationCodeName = 'validCode'

        activationCodeRepository.findByCode(activationCodeName) >> Optional.empty()

        when:
        activationCodeService.validateActivationCode(activationCodeName)

        then:
        RuntimeException ex = thrown()
        ex.message == 'Podany kod aktywacyjny nie istnieje!'
    }

    def "should handle expired code"() {
        given:
        String activationCodeName = 'validCode'

        ActivationCode activationCode = Mock(ActivationCode)
        activationCode.getCode() >> activationCodeName
        activationCode.getExpireDate() >> LocalDate.now().minusDays(1)
        activationCode.getIsUsed() >> false
        activationCodeRepository.findByCode(activationCodeName) >> Optional.of(activationCode)

        when:
        activationCodeService.validateActivationCode(activationCodeName)

        then:
        RuntimeException ex = thrown()
        ex.message == 'Kod aktywacyjny wygasł!'
    }

    def "should handle used code"() {
        given:
        String activationCodeName = 'validCode'

        ActivationCode activationCode = Mock(ActivationCode)
        activationCode.getCode() >> activationCodeName
        activationCode.getExpireDate() >> LocalDate.now().plusDays(1)
        activationCode.getIsUsed() >> true
        activationCodeRepository.findByCode(activationCodeName) >> Optional.of(activationCode)

        when:
        activationCodeService.validateActivationCode(activationCodeName)

        then:
        RuntimeException ex = thrown()
        ex.message == 'Podany kod aktywacyjny został już wykorzystany!'
    }

    /*
    * markActivationCodeAsUsed
    */

    def "should handle using code correctly"() {
        given:
        String activationCodeName = 'validCode'

        ActivationCode activationCode = Mock(ActivationCode)
        activationCode.getCode() >> activationCodeName
        activationCode.getExpireDate() >> LocalDate.now().plusDays(1)
        activationCode.getIsUsed() >> false
        activationCodeRepository.findByCode(activationCodeName) >> Optional.of(activationCode)

        when:
        activationCodeService.markActivationCodeAsUsed(activationCodeName)

        then:
        1 * activationCode.setIsUsed(true)
        1 * activationCodeRepository.save(activationCode)
    }

    def "should handle using code when not found"() {
        given:
        String activationCodeName = 'validCode'

        activationCodeRepository.findByCode(activationCodeName) >> Optional.empty()

        when:
        activationCodeService.markActivationCodeAsUsed(activationCodeName)

        then:
        thrown(RuntimeException)
    }

    /*
    * generateExpireCodeInfo
    */

    def "should sign in with expire code info for owner"() {
        given:
        ActivationCode activationCode = Mock(ActivationCode)
        activationCode.getExpireDate() >> LocalDate.now().plusDays(1)
        Farm farm = Mock(Farm)
        farm.getId() >> 1
        farm.getIdActivationCode() >> activationCode.getId()

        farmRepository.findById(farm.getId()) >> Optional.of(farm)
        activationCodeRepository.findById(farm.getIdActivationCode()) >> Optional.of(activationCode)

        when:
        String response = activationCodeService.generateExpireCodeInfo(farm, ['ROLE_FARM_OWNER'])

        then:
        response == 'Kod aktywacyjny wygasa za 1 dni.'
    }

    def "should no info during sign in with expire code for owner when its more than 14 to expire"() {
        given:
        ActivationCode activationCode = Mock(ActivationCode)
        activationCode.getExpireDate() >> LocalDate.now().plusDays(19)
        Farm farm = Mock(Farm)
        farm.getId() >> 1
        farm.getIdActivationCode() >> activationCode.getId()
        farmRepository.findById(farm.getId()) >> Optional.of(farm)
        activationCodeRepository.findById(farm.getIdActivationCode()) >> Optional.of(activationCode)

        when:
        String response = activationCodeService.generateExpireCodeInfo(farm, ['ROLE_FARM_OWNER'])

        then:
        response == null
    }

    def "should no info during sign in with expire code info for manager"() {
        given:
        ActivationCode activationCode = Mock(ActivationCode)
        activationCode.getExpireDate() >> LocalDate.now().plusDays(1)
        Farm farm = Mock(Farm)
        farm.getId() >> 1
        farm.getIdActivationCode() >> activationCode.getId()

        when:
        String response = activationCodeService.generateExpireCodeInfo(farm, ['ROLE_FARM_MANAGER'])

        then:
        response == null
    }

    /*
    * findActivationCodeByFarmId
    */

    def "find activation code by farm id"() {
        given:
        String activationCodeName = 'validCode'
        ActivationCode activationCode = Mock(ActivationCode)
        activationCode.getCode() >> activationCodeName
        activationCode.getExpireDate() >> LocalDate.now().plusDays(1)
        Farm farm = Mock(Farm)
        farm.getId() >> 1
        farm.getIdActivationCode() >> activationCode.getId()

        farmRepository.findById(farm.getId()) >> Optional.of(farm)
        activationCodeRepository.findById(farm.getIdActivationCode()) >> Optional.of(activationCode)

        when:
        ActivationCode farmCode = activationCodeService.findActivationCodeByFarmId(farm.getId())

        then:
        farmCode.getCode() == activationCodeName
        farmCode.getExpireDate() == LocalDate.now().plusDays(1)
    }

    def "find activation code by farm id not found activation code"() {
        given:
        String activationCodeName = 'validCode'
        ActivationCode activationCode = Mock(ActivationCode)
        activationCode.getCode() >> activationCodeName
        activationCode.getExpireDate() >> LocalDate.now().plusDays(1)
        Farm farm = Mock(Farm)
        farm.getId() >> 1
        farm.getIdActivationCode() >> activationCode.getId()

        farmRepository.findById(farm.getId()) >> Optional.of(farm)
        activationCodeRepository.findById(farm.getIdActivationCode()) >> Optional.empty()

        when:
        activationCodeService.findActivationCodeByFarmId(farm.getId())

        then:
        thrown(RuntimeException)
    }

    /*
    * findActivationCodeById
    */

    def "shpuld find activation code by id"() {
        given:
        ActivationCode activationCode = Mock(ActivationCode)
        activationCode.getId() >> 1

        activationCodeRepository.findById(1) >> Optional.of(activationCode)

        when:
        ActivationCode foundActCode = activationCodeService.findActivationCodeById(1)

        then:
        foundActCode == activationCode
    }

    def "should not find activation code with runtime exception"() {
        given:
        activationCodeRepository.findById(1) >> Optional.empty()

        when:
        activationCodeService.findActivationCodeById(1)

        then:
        thrown(RuntimeException)
    }

    /*
    * updateActivationCodeForFarm
    */

    def "should update activation code for farm"() {
        given:
        String newActivationCode = 'newCode'
        ActivationCode newActivationCodeEntity = Mock(ActivationCode)
        newActivationCodeEntity.getCode() >> newActivationCode
        newActivationCodeEntity.getExpireDate() >> LocalDate.now().plusDays(1)
        newActivationCodeEntity.getIsUsed() >> false
        ActivationCode currentCode = Mock(ActivationCode)
        currentCode.getCode() >> 'oldCode'
        currentCode.getExpireDate() >> LocalDate.now().plusDays(1)
        currentCode.getIsUsed() >> true
        Farm farm = Mock(Farm)
        farm.getId() >> 1
        farm.getIdActivationCode() >> currentCode.getId()
        String username = 'testUsername'

        bruteForceProtectionService.isBlocked(username) >> false
        activationCodeRepository.findByCode(newActivationCode) >> Optional.of(newActivationCodeEntity)
        bruteForceProtectionService.loginSucceeded(username) >> { }
        farmRepository.findById(farm.getId()) >> Optional.of(farm)
        activationCodeRepository.findById(farm.getIdActivationCode()) >> Optional.of(currentCode)

        when:
        activationCodeService.updateActivationCodeForFarm(newActivationCode, farm.getId(), username)

        then:
        1 * activationCodeRepository.delete(currentCode)
        1 * farmRepository.save(farm)
        1 * activationCodeRepository.save(newActivationCodeEntity)
    }

    def "should catch new code as not ok in here used"() {
        given:
        String newActivationCode = 'newCode'
        ActivationCode newActivationCodeEntity = Mock(ActivationCode)
        newActivationCodeEntity.getCode() >> newActivationCode
        newActivationCodeEntity.getExpireDate() >> LocalDate.now().plusDays(1)
        newActivationCodeEntity.getIsUsed() >> true
        String username = 'testUsername'

        bruteForceProtectionService.isBlocked(username) >> false
        activationCodeRepository.findByCode(newActivationCode) >> Optional.of(newActivationCodeEntity)

        when:
        activationCodeService.updateActivationCodeForFarm(newActivationCode, 1, username)

        then:
        RuntimeException ex = thrown()
        ex.message == 'Podany kod aktywacyjny został już wykorzystany!'
    }

    def "should block user for too many attempts"() {
        given:
        String newActivationCode = 'newCode'
        String username = 'testUsername'

        bruteForceProtectionService.isBlocked(username) >> true

        when:
        activationCodeService.updateActivationCodeForFarm(newActivationCode, 1, username)

        then:
        TooManyRequestsException ex = thrown()
        ex.message == 'Zbyt wiele nieudanych prób logowania! Spróbuj ponownie później.'
    }

}
