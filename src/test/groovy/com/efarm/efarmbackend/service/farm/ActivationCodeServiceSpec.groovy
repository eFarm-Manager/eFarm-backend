package com.efarm.efarmbackend.service

import com.efarm.efarmbackend.model.user.User
import com.efarm.efarmbackend.model.user.Role
import com.efarm.efarmbackend.model.user.ERole
import com.efarm.efarmbackend.model.farm.ActivationCode
import com.efarm.efarmbackend.model.farm.Farm
import com.efarm.efarmbackend.payload.response.MessageResponse
import com.efarm.efarmbackend.repository.farm.ActivationCodeRepository
import com.efarm.efarmbackend.repository.farm.FarmRepository
import com.efarm.efarmbackend.security.jwt.JwtUtils
import com.efarm.efarmbackend.security.services.UserDetailsImpl
import com.efarm.efarmbackend.security.services.BruteForceProtectionService
import com.efarm.efarmbackend.service.farm.ActivationCodeService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.http.HttpStatus
import spock.lang.Specification
import spock.lang.Subject
import org.springframework.http.ResponseCookie
import org.springframework.http.HttpHeaders

import java.time.LocalDate

class ActivationCodeServiceSpec extends Specification {

    def activationCodeRepository = Mock(ActivationCodeRepository)
    def farmRepository = Mock(FarmRepository)
    def jwtUtils = Mock(JwtUtils)
    def bruteForceProtectionService = Mock(BruteForceProtectionService)

    @Subject
    ActivationCodeService activationCodeService = new ActivationCodeService(
            activationCodeRepository: activationCodeRepository,
            farmRepository: farmRepository,
            jwtUtils: jwtUtils,
            bruteForceProtectionService: bruteForceProtectionService,
            daysToShowExpireActivationCodeNotification: 14,
    )

    def setup() {
        SecurityContextHolder.clearContext()
    }

    def "should handle correct code"() {
        given:
        String activationCodeName = "validCode"

        ActivationCode activationCode = Mock(ActivationCode)
        activationCode.getCode() >> activationCodeName
        activationCode.getExpireDate() >> LocalDate.now().plusDays(1)
        activationCode.getIsUsed() >> false
        activationCodeRepository.findByCode(activationCodeName) >> Optional.of(activationCode)

        when:
        ResponseEntity<MessageResponse> response = activationCodeService.validateActivationCode(activationCodeName)

        then:
        response == ResponseEntity.ok().build()
    }

    def "should handle invalid code"() {
        given:
        String activationCodeName = "validCode"

        activationCodeRepository.findByCode(activationCodeName) >> Optional.empty()

        when:
        ResponseEntity<MessageResponse> response = activationCodeService.validateActivationCode(activationCodeName)

        then:
        response.statusCodeValue == 400
        response.body.message == "Activation code does not exist."
    }

    def "should handle expired code"() {
        given:
        String activationCodeName = "validCode"

        ActivationCode activationCode = Mock(ActivationCode)
        activationCode.getCode() >> activationCodeName
        activationCode.getExpireDate() >> LocalDate.now().minusDays(1)
        activationCode.getIsUsed() >> false
        activationCodeRepository.findByCode(activationCodeName) >> Optional.of(activationCode)

        when:
        ResponseEntity<MessageResponse> response = activationCodeService.validateActivationCode(activationCodeName)

        then:
        response.getStatusCode() == HttpStatus.BAD_REQUEST
        response.body.message == "Activation code has expired."
    }

    def "should handle used code"() {
        given:
        String activationCodeName = "validCode"

        ActivationCode activationCode = Mock(ActivationCode)
        activationCode.getCode() >> activationCodeName
        activationCode.getExpireDate() >> LocalDate.now().plusDays(1)
        activationCode.getIsUsed() >> true
        activationCodeRepository.findByCode(activationCodeName) >> Optional.of(activationCode)

        when:
        ResponseEntity<MessageResponse> response = activationCodeService.validateActivationCode(activationCodeName)

        then:
        response.getStatusCode() == HttpStatus.BAD_REQUEST
        response.body.message == "Activation code has already been used."
    }

    def "should handle using code correctly"() {
        given:
        String activationCodeName = "validCode"

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
        String activationCodeName = "validCode"

        activationCodeRepository.findByCode(activationCodeName) >> Optional.empty()

        when:
        activationCodeService.markActivationCodeAsUsed(activationCodeName)

        then:
        thrown(RuntimeException)
    }

    def "should sign in with expire code info for owner"() {
        given:
        ActivationCode activationCode = Mock(ActivationCode)
        activationCode.getExpireDate() >> LocalDate.now().plusDays(1)
        Role role_owner = Mock(Role)
        role_owner.getName() >> ERole.ROLE_FARM_OWNER
        Farm farm = Mock(Farm)
        farm.getId() >> 1
        farm.getIdActivationCode() >> activationCode.getId()
        User currentUser = Mock(User)
        currentUser.getUsername() >> "currentUser"
        currentUser.getId() >> 1
        currentUser.getRole() >> role_owner
        currentUser.getFarm() >> farm

        UserDetailsImpl currentUserDetails = UserDetailsImpl.build(currentUser)
        farmRepository.findById(farm.getId()) >> Optional.of(farm)
        activationCodeRepository.findById(farm.getIdActivationCode()) >> Optional.of(activationCode)
        ResponseCookie mockCookie = ResponseCookie.from("jwtToken", "mockTokenValue")
                .path("/api")
                .httpOnly(true)
                .build()

        jwtUtils.generateJwtCookie(currentUserDetails) >> mockCookie

        when:
        ResponseEntity<?> response = activationCodeService.signinWithExpireCodeInfo(currentUserDetails, farm, ["ROLE_FARM_OWNER"])

        then:
        response.getStatusCode() == HttpStatus.OK
        response.getHeaders().get(HttpHeaders.SET_COOKIE).contains(mockCookie.toString())
        response.getBody().expireCodeInfo.contains("Kod aktywacyjny wygasa za 1 dni.")
    }

    def "should no info during sign in with expire code for owner when its more than 14 to expire"() {
        given:
        ActivationCode activationCode = Mock(ActivationCode)
        activationCode.getExpireDate() >> LocalDate.now().plusDays(19)
        Role role_owner = Mock(Role)
        role_owner.getName() >> ERole.ROLE_FARM_OWNER
        Farm farm = Mock(Farm)
        farm.getId() >> 1
        farm.getIdActivationCode() >> activationCode.getId()
        User currentUser = Mock(User)
        currentUser.getUsername() >> "currentUser"
        currentUser.getId() >> 1
        currentUser.getRole() >> role_owner
        currentUser.getFarm() >> farm

        UserDetailsImpl currentUserDetails = UserDetailsImpl.build(currentUser)
        farmRepository.findById(farm.getId()) >> Optional.of(farm)
        activationCodeRepository.findById(farm.getIdActivationCode()) >> Optional.of(activationCode)
        ResponseCookie mockCookie = ResponseCookie.from("jwtToken", "mockTokenValue")
                .path("/api")
                .httpOnly(true)
                .build()

        jwtUtils.generateJwtCookie(currentUserDetails) >> mockCookie

        when:
        ResponseEntity<?> response = activationCodeService.signinWithExpireCodeInfo(currentUserDetails, farm, ["ROLE_FARM_OWNER"])

        then:
        response == null
    }

    def "should no info during sign in with expire code info for manager"() {
        given:
        ActivationCode activationCode = Mock(ActivationCode)
        activationCode.getExpireDate() >> LocalDate.now().plusDays(1)
        Role role_manager = Mock(Role)
        role_manager.getName() >> ERole.ROLE_FARM_MANAGER
        Farm farm = Mock(Farm)
        farm.getId() >> 1
        farm.getIdActivationCode() >> activationCode.getId()
        User currentUser = Mock(User)
        currentUser.getUsername() >> "currentUser"
        currentUser.getId() >> 1
        currentUser.getRole() >> role_manager
        currentUser.getFarm() >> farm
        UserDetailsImpl currentUserDetails = UserDetailsImpl.build(currentUser)


        when:
        ResponseEntity<?> response = activationCodeService.signinWithExpireCodeInfo(currentUserDetails, farm, ["ROLE_FARM_MANAGER"])

        then:
        response == null
    }

    def "find activation code by farm id"() {
        given:
        String activationCodeName = "validCode"
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
        String activationCodeName = "validCode"
        ActivationCode activationCode = Mock(ActivationCode)
        activationCode.getCode() >> activationCodeName
        activationCode.getExpireDate() >> LocalDate.now().plusDays(1)
        Farm farm = Mock(Farm)
        farm.getId() >> 1
        farm.getIdActivationCode() >> activationCode.getId()

        farmRepository.findById(farm.getId()) >> Optional.of(farm)
        activationCodeRepository.findById(farm.getIdActivationCode()) >> Optional.empty()


        when:
        ActivationCode farmCode = activationCodeService.findActivationCodeByFarmId(farm.getId())

        then:
        thrown(RuntimeException)
    }

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
        ActivationCode foundActCode = activationCodeService.findActivationCodeById(1)

        then:
        thrown(RuntimeException)
    }

    def "should update activation code for farm"() {
        given:
        String newActivationCode = "newCode"
        ActivationCode newActivationCodeEntity = Mock(ActivationCode)
        newActivationCodeEntity.getCode() >> newActivationCode
        newActivationCodeEntity.getExpireDate() >> LocalDate.now().plusDays(1)
        newActivationCodeEntity.getIsUsed() >> false
        ActivationCode currentCode = Mock(ActivationCode)
        currentCode.getCode() >> "oldCode"
        currentCode.getExpireDate() >> LocalDate.now().plusDays(1)
        currentCode.getIsUsed() >> true
        Farm farm = Mock(Farm)
        farm.getId() >> 1
        farm.getIdActivationCode() >> currentCode.getId()
        String username = "testUsername"

        bruteForceProtectionService.isBlocked(username) >> false
        activationCodeRepository.findByCode(newActivationCode) >> Optional.of(newActivationCodeEntity)
        bruteForceProtectionService.loginSucceeded(username) >> {}
        farmRepository.findById(farm.getId()) >> Optional.of(farm)
        activationCodeRepository.findById(farm.getIdActivationCode()) >> Optional.of(currentCode)

        when:
        ResponseEntity<MessageResponse> response = activationCodeService.updateActivationCodeForFarm(newActivationCode, farm.getId(), username)

        then:
        1 * activationCodeRepository.delete(currentCode)
        1 * farmRepository.save(farm)
        1 * activationCodeRepository.save(newActivationCodeEntity)
        response.getStatusCode() == HttpStatus.OK
        response.getBody().message.contains("Activation code updated successfully for the farm.")
    }

    def "should catch new code as not ok in here used"() {
        given:
        String newActivationCode = "newCode"
        ActivationCode newActivationCodeEntity = Mock(ActivationCode)
        newActivationCodeEntity.getCode() >> newActivationCode
        newActivationCodeEntity.getExpireDate() >> LocalDate.now().plusDays(1)
        newActivationCodeEntity.getIsUsed() >> true
        String username = "testUsername"

        bruteForceProtectionService.isBlocked(username) >> false
        activationCodeRepository.findByCode(newActivationCode) >> Optional.of(newActivationCodeEntity)

        when:
        ResponseEntity<MessageResponse> response = activationCodeService.updateActivationCodeForFarm(newActivationCode, 1, username)

        then:
        response.getStatusCode() == HttpStatus.BAD_REQUEST
        response.getBody().message.contains("Activation code has already been used.")
    }

    def "should block user for too many attempts"() {
        given:
        String newActivationCode = "newCode"
        String username = "testUsername"

        bruteForceProtectionService.isBlocked(username) >> true

        when:
        ResponseEntity<MessageResponse> response = activationCodeService.updateActivationCodeForFarm(newActivationCode, 1, username)

        then:
        response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS
        response.body.message == "Too many failed attempts. Please try again later."
    }

}