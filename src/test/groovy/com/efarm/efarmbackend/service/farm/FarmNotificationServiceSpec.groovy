package com.efarm.efarmbackend.service.farm

import com.efarm.efarmbackend.model.farm.ActivationCode
import com.efarm.efarmbackend.model.farm.Farm
import com.efarm.efarmbackend.model.user.User
import com.efarm.efarmbackend.repository.farm.ActivationCodeRepository
import com.efarm.efarmbackend.repository.farm.FarmRepository
import com.efarm.efarmbackend.service.MainNotificationService
import com.efarm.efarmbackend.service.user.UserService

import java.time.LocalDate

import spock.lang.Subject
import spock.lang.Specification

class FarmNotificationServiceSpec extends Specification {

    def userService = Mock(UserService)
    def farmRepository = Mock(FarmRepository)
    def activationCodeRepository = Mock(ActivationCodeRepository)
    def mainNotificationService = Mock(MainNotificationService)

    @Subject
    FarmNotificationService farmNotificationService = new FarmNotificationService(
            userService: userService,
            farmRepository: farmRepository,
            activationCodeRepository: activationCodeRepository,
            mainNotificationService: mainNotificationService
    )

    void "should Call Check Activation Code For Each Farm"() {
        given:
        Farm farm1 = Mock(Farm) {
            getId() >> 1
            idActivationCode >> 10
            isActive >> true
        }
        Farm farm2 = Mock(Farm) {
            getId() >> 2
            idActivationCode >> 11
            isActive >> true
        }
        farmRepository.findByIsActiveTrue() >> [farm1, farm2]

        activationCodeRepository.findById(farm1.idActivationCode) >> Optional.of(Mock(ActivationCode) {
            getExpireDate() >> LocalDate.now().plusDays(5)
        })
        activationCodeRepository.findById(farm2.idActivationCode) >> Optional.of(Mock(ActivationCode) {
            getExpireDate() >> LocalDate.now().plusDays(1)
        })
        userService.getAllOwnersForFarm(1) >> [Mock(User) {
            getIsActive() >> true
            getEmail() >> 'owner1@gmail.com'
        }]
        userService.getAllOwnersForFarm(2) >> [Mock(User) {
            getIsActive() >> true
            getEmail() >> 'owner2@gmail.com'
        }]

        when:
        farmNotificationService.checkActivationCodeDueDateNotifications()

        then:
        1 * mainNotificationService.sendNotificationToOwner(_, _, _)
        1 * mainNotificationService.sendNotificationToOwner(_, _, _)
    }

    def "should checkAndNotifyForActivationCode when days until expire 14"() {
        given:
        Farm farm = Mock(Farm) {
            getIdActivationCode() >> 1
            getFarmName() >> 'Farm1'
            getId() >> 1
        }
        ActivationCode activationCode = Mock(ActivationCode) {
            getExpireDate() >> LocalDate.now().plusDays(14)
        }
        activationCodeRepository.findById(1) >> Optional.of(activationCode)
        userService.getAllOwnersForFarm(1) >> [Mock(User) {
            getIsActive() >> true
            getEmail() >> 'owner1@gmail.com'
        }]

        when:
        farmNotificationService.checkAndNotifyForActivationCode(farm, LocalDate.now())

        then:
        1 * mainNotificationService.sendNotificationToOwner(_ as User,
         'Termin ważności kodu aktywacyjnego dla Twojej farmy Farm1 upływa za 14 dni.',
         'Niedługo wygasa kod aktywacyjny Twojej wirtualnej Farmy!')
    }

    def "should checkAndNotifyForActivationCode when days until expire 5"() {
        given:
        Farm farm = Mock(Farm) {
            getIdActivationCode() >> 1
            getFarmName() >> 'Farm1'
            getId() >> 1
        }
        ActivationCode activationCode = Mock(ActivationCode) {
            getExpireDate() >> LocalDate.now().plusDays(5)
        }
        activationCodeRepository.findById(1) >> Optional.of(activationCode)
        userService.getAllOwnersForFarm(1) >> [Mock(User) {
            getIsActive() >> true
            getEmail() >> 'owner1@gmail.com'
        }]

        when:
        farmNotificationService.checkAndNotifyForActivationCode(farm, LocalDate.now())

        then:
        1 * mainNotificationService.sendNotificationToOwner(_ as User,
         'Termin ważności kodu aktywacyjnego dla Twojej farmy Farm1 upływa za 5 dni.',
         'Niedługo wygasa kod aktywacyjny Twojej wirtualnej Farmy!')
    }

    def "should checkAndNotifyForActivationCode when days until expire 1"() {
        given:
        Farm farm = Mock(Farm) {
            getIdActivationCode() >> 1
            getFarmName() >> 'Farm1'
            getId() >> 1
        }
        ActivationCode activationCode = Mock(ActivationCode) {
            getExpireDate() >> LocalDate.now().plusDays(1)
        }
        activationCodeRepository.findById(1) >> Optional.of(activationCode)
        userService.getAllOwnersForFarm(1) >> [Mock(User) {
            getIsActive() >> true
            getEmail() >> 'owner1@gmail.com'
        }]

        when:
        farmNotificationService.checkAndNotifyForActivationCode(farm, LocalDate.now())

        then:
        1 * mainNotificationService.sendNotificationToOwner(_ as User,
         'Termin ważności kodu aktywacyjnego dla Twojej farmy Farm1 upływa za 1 dni.',
         'Niedługo wygasa kod aktywacyjny Twojej wirtualnej Farmy!')
    }

    def "should not checkAndNotifyForActivationCode when days until expire are neither 14, 5, nor 1"() {
        given:
        Farm farm = Mock(Farm) {
            getIdActivationCode() >> 1
            getFarmName() >> 'Farm1'
            getId() >> 1
        }
        ActivationCode activationCode = Mock(ActivationCode) {
            getExpireDate() >> LocalDate.now().plusDays(10)
        }
        activationCodeRepository.findById(1) >> Optional.of(activationCode)
        userService.getAllOwnersForFarm(1) >> [Mock(User) {
            getIsActive() >> true
            getEmail() >> 'owner1@gmail.com'
        }]

        when:
        farmNotificationService.checkAndNotifyForActivationCode(farm, LocalDate.now())

        then:
        0 * mainNotificationService.sendNotificationToOwner(_, _, _)
    }

}
