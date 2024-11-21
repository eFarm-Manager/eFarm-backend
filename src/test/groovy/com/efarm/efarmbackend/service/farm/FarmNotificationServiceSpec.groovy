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

    /*
    * checkActivationCodeDueDateNotifications
    */

    void "should Call Check Activation Code For Each Farm"() {
        given:
        Farm farm1 = Mock(Farm) {
            getId() >> 1
            getIdActivationCode() >> 10
            getFarmName() >> 'Farm1'
            getIsActive() >> true
        }
        Farm farm2 = Mock(Farm) {
            getId() >> 2
            getIdActivationCode() >> 11
            getFarmName() >> 'Farm2'
            getIsActive() >> false
        }
        farmRepository.findAll() >> [farm1, farm2]

        activationCodeRepository.findById(farm1.idActivationCode) >> Optional.of(Mock(ActivationCode) {
            getExpireDate() >> LocalDate.now().plusDays(5)
        })
        activationCodeRepository.findById(farm2.idActivationCode) >> Optional.of(Mock(ActivationCode) {
            getExpireDate() >> LocalDate.now().minusDays(362)
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
        1 * mainNotificationService.sendNotificationToUser(_, _, _)
        1 * mainNotificationService.sendNotificationToUser(_, _, _)
    }

    /*
    * checkAndNotifyForActivationCode
    */

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
        1 * mainNotificationService.sendNotificationToUser(_ as User,
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
        1 * mainNotificationService.sendNotificationToUser(_ as User,
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
        1 * mainNotificationService.sendNotificationToUser(_ as User,
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
        0 * mainNotificationService.sendNotificationToUser(_, _, _)
    }

    /*
    * checkAndNotifyForFarmDeletion
    */

    def "should check and notify for farm deletion when days until deletion 3"() {
        given:
        Farm farm = Mock(Farm) {
            getIdActivationCode() >> 1
            getFarmName() >> 'Farm1'
            getId() >> 1
        }
        ActivationCode activationCode = Mock(ActivationCode) {
            getExpireDate() >> LocalDate.now().minusDays(362) // 365 - 362 = 3
        }
        activationCodeRepository.findById(1) >> Optional.of(activationCode)
        userService.getAllOwnersForFarm(1) >> [Mock(User) {
            getIsActive() >> true
            getEmail() >> 'owner1@gmail.com'
        }]

        when:
        farmNotificationService.checkAndNotifyForFarmDeletion(farm, LocalDate.now())

        then:
        1 * mainNotificationService.sendNotificationToUser(_ as User,
         'Twoja farma Farm1 zostanie trwale usunięta za 3 dni. Zaktualizuj swój kod aktywacyjny, aby temu zapobiec.',
         'Twoja farma zostanie wkrótce usunięta!')
    }

    def "should check and notify for farm deletion when days until deletion 2"() {
        given:
        Farm farm = Mock(Farm) {
            getIdActivationCode() >> 1
            getFarmName() >> 'Farm1'
            getId() >> 1
        }
        ActivationCode activationCode = Mock(ActivationCode) {
            getExpireDate() >> LocalDate.now().minusDays(363) // 365 - 363 = 2
        }
        activationCodeRepository.findById(1) >> Optional.of(activationCode)
        userService.getAllOwnersForFarm(1) >> [Mock(User) {
            getIsActive() >> true
            getEmail() >> 'owner1@gmail.com'
        }]

        when:
        farmNotificationService.checkAndNotifyForFarmDeletion(farm, LocalDate.now())

        then:
        1 * mainNotificationService.sendNotificationToUser(_ as User,
         'Twoja farma Farm1 zostanie trwale usunięta za 2 dni. Zaktualizuj swój kod aktywacyjny, aby temu zapobiec.',
         'Twoja farma zostanie wkrótce usunięta!')
    }

    def "should check and notify for farm deletion when days until deletion 1"() {
        given:
        Farm farm = Mock(Farm) {
            getIdActivationCode() >> 1
            getFarmName() >> 'Farm1'
            getId() >> 1
        }
        ActivationCode activationCode = Mock(ActivationCode) {
            getExpireDate() >> LocalDate.now().minusDays(364) // 365 - 364 = 1
        }
        activationCodeRepository.findById(1) >> Optional.of(activationCode)
        userService.getAllOwnersForFarm(1) >> [Mock(User) {
            getIsActive() >> true
            getEmail() >> 'owner1@gmail.com'
        }]

        when:
        farmNotificationService.checkAndNotifyForFarmDeletion(farm, LocalDate.now())

        then:
        1 * mainNotificationService.sendNotificationToUser(_ as User,
         'Twoja farma Farm1 zostanie trwale usunięta za 1 dni. Zaktualizuj swój kod aktywacyjny, aby temu zapobiec.',
         'Twoja farma zostanie wkrótce usunięta!')
    }



}
