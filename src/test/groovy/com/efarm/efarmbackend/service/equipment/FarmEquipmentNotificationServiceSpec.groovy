package com.efarm.efarmbackend.service.equipment;

import com.efarm.efarmbackend.model.equipment.FarmEquipment;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.model.farm.Farm
import com.efarm.efarmbackend.repository.equipment.FarmEquipmentRepository;
import com.efarm.efarmbackend.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import spock.lang.Specification
import spock.lang.Subject
import java.time.LocalDate;
import java.util.List;

class FarmEquipmentNotificationServiceSpec extends Specification {

    def farmEquipmentRepository = Mock(FarmEquipmentRepository)
    def mailSender = Mock(JavaMailSender)
    def userService = Mock(UserService)

    @Subject
    FarmEquipmentNotificationService farmEquipmentNotificationService = new FarmEquipmentNotificationService(
            farmEquipmentRepository: farmEquipmentRepository,
            mailSender: mailSender,
            userService: userService
    )

    def "should send notification to owner"() {
        given:
        User owner = Mock(User) {
            getEmail() >> "owner@example.com"
        }
        String message = "Test message"
        String subject = "Test subject"

        when:
        farmEquipmentNotificationService.sendNotificationToOwner(owner, message, subject)

        then:
        1 * mailSender.send({ SimpleMailMessage msg ->
            msg.to[0] == "owner@example.com" &&
            msg.subject == subject &&
            msg.text == message
        })
    }

    def "should send notification for insurance expiring in 14 days"() {
        given:
        LocalDate today = LocalDate.now()
        FarmEquipment equipment = Mock(FarmEquipment) {
            getInsuranceExpirationDate() >> today.plusDays(14)
            getEquipmentName() >> "Tractor"
            getInsurancePolicyNumber() >> "INS123"
            getFarmIdFarm() >> Mock(Farm) { getId() >> 1 }
        }
        User owner = Mock(User) { getEmail() >> "owner@example.com" }
        userService.getAllOwnersForFarm(1) >> [owner]

        when:
        farmEquipmentNotificationService.checkAndNotifyForInsurance(equipment, today)

        then:
        1 * mailSender.send({ SimpleMailMessage msg ->
            msg.to[0] == "owner@example.com" &&
            msg.subject == "Ubezpieczenie sprzętu wygasa!" &&
            msg.text == "W twoim sprzęcie Tractor polisa ubezpieczeniowa o numerze INS123 wygasa za 14 dni."
        })
    }

    def "should send notification for insurance expiring in 3 days"() {
        given:
        LocalDate today = LocalDate.now()
        FarmEquipment equipment = Mock(FarmEquipment) {
            getInsuranceExpirationDate() >> today.plusDays(3)
            getEquipmentName() >> "Plow"
            getInsurancePolicyNumber() >> "INS456"
            getFarmIdFarm() >> Mock(Farm) { getId() >> 2 }
        }
        User owner = Mock(User) { getEmail() >> "owner@example.com" }
        userService.getAllOwnersForFarm(2) >> [owner]

        when:
        farmEquipmentNotificationService.checkAndNotifyForInsurance(equipment, today)

        then:
        1 * mailSender.send({ SimpleMailMessage msg ->
            msg.to[0] == "owner@example.com" &&
            msg.subject == "Ubezpieczenie sprzętu wygasa!" &&
            msg.text == "W twoim sprzęcie Plow polisa ubezpieczeniowa o numerze INS456 wygasa za 3 dni."
        })
    }

    def "should send notification for insurance expiring in 1 day"() {
        given:
        LocalDate today = LocalDate.now()
        FarmEquipment equipment = Mock(FarmEquipment) {
            getInsuranceExpirationDate() >> today.plusDays(1)
            getEquipmentName() >> "Seeder"
            getInsurancePolicyNumber() >> "INS789"
            getFarmIdFarm() >> Mock(Farm) { getId() >> 3 }
        }
        User owner = Mock(User) { getEmail() >> "owner@example.com" }
        userService.getAllOwnersForFarm(3) >> [owner]

        when:
        farmEquipmentNotificationService.checkAndNotifyForInsurance(equipment, today)

        then:
        1 * mailSender.send({ SimpleMailMessage msg ->
            msg.to[0] == "owner@example.com" &&
            msg.subject == "Ubezpieczenie sprzętu wygasa!" &&
            msg.text == "W twoim sprzęcie Seeder polisa ubezpieczeniowa o numerze INS789 wygasa za 1 dni."
        })
    }

    def "should not send notification if insurance is valid for more than 14 days"() {
        given:
        LocalDate today = LocalDate.now()
        FarmEquipment equipment = Mock(FarmEquipment) {
            getInsuranceExpirationDate() >> today.plusDays(15)
        }

        when:
        farmEquipmentNotificationService.checkAndNotifyForInsurance(equipment, today)

        then:
        0 * mailSender.send(_)
    }

    def "should not send notification if insurance date is null"() {
        given:
        LocalDate today = LocalDate.now()
        FarmEquipment equipment = Mock(FarmEquipment) {
            getInsuranceExpirationDate() >> null
        }

        when:
        farmEquipmentNotificationService.checkAndNotifyForInsurance(equipment, today)

        then:
        0 * mailSender.send(_)
    }

    // insurance

    def "should send notification for inspection expiring in 14 days"() {
        given:
        LocalDate today = LocalDate.now()
        FarmEquipment equipment = Mock(FarmEquipment) {
            getInspectionExpireDate() >> today.plusDays(14)
            getEquipmentName() >> "Tractor"
            getFarmIdFarm() >> Mock(Farm) { getId() >> 1 }
        }
        User owner = Mock(User) { getEmail() >> "owner@example.com" }
        userService.getAllOwnersForFarm(1) >> [owner]

        when:
        farmEquipmentNotificationService.checkAndNotifyForInspection(equipment, today)

        then:
        1 * mailSender.send({ SimpleMailMessage msg ->
            msg.to[0] == "owner@example.com" &&
            msg.subject == "Przegląd techniczny wygasa!" &&
            msg.text == "W twoim sprzęcie Tractor przegląd techniczny wygasa za 14 dni."
        })
    }

    def "should send notification for inspection expiring in 3 days"() {
        given:
        LocalDate today = LocalDate.now()
        FarmEquipment equipment = Mock(FarmEquipment) {
            getInspectionExpireDate() >> today.plusDays(3)
            getEquipmentName() >> "Plow"
            getFarmIdFarm() >> Mock(Farm) { getId() >> 2 }
        }
        User owner = Mock(User) { getEmail() >> "owner@example.com" }
        userService.getAllOwnersForFarm(2) >> [owner]

        when:
        farmEquipmentNotificationService.checkAndNotifyForInspection(equipment, today)

        then:
        1 * mailSender.send({ SimpleMailMessage msg ->
            msg.to[0] == "owner@example.com" &&
            msg.subject == "Przegląd techniczny wygasa!" &&
            msg.text == "W twoim sprzęcie Plow przegląd techniczny wygasa za 3 dni."
        })
    }

    def "should send notification for inspection expiring in 1 day"() {
        given:
        LocalDate today = LocalDate.now()
        FarmEquipment equipment = Mock(FarmEquipment) {
            getInspectionExpireDate() >> today.plusDays(1)
            getEquipmentName() >> "Seeder"
            getFarmIdFarm() >> Mock(Farm) { getId() >> 3 }
        }
        User owner = Mock(User) { getEmail() >> "owner@example.com" }
        userService.getAllOwnersForFarm(3) >> [owner]

        when:
        farmEquipmentNotificationService.checkAndNotifyForInspection(equipment, today)

        then:
        1 * mailSender.send({ SimpleMailMessage msg ->
            msg.to[0] == "owner@example.com" &&
            msg.subject == "Przegląd techniczny wygasa!" &&
            msg.text == "W twoim sprzęcie Seeder przegląd techniczny wygasa za 1 dni."
        })
    }

    def "should not send notification if inspection is valid for more than 14 days"() {
        given:
        LocalDate today = LocalDate.now()
        FarmEquipment equipment = Mock(FarmEquipment) {
            getInspectionExpireDate() >> today.plusDays(15)
        }

        when:
        farmEquipmentNotificationService.checkAndNotifyForInspection(equipment, today)

        then:
        0 * mailSender.send(_)
    }

    def "should not send notification if inspection date is null"() {
        given:
        LocalDate today = LocalDate.now()
        FarmEquipment equipment = Mock(FarmEquipment) {
            getInspectionExpireDate() >> null
        }

        when:
        farmEquipmentNotificationService.checkAndNotifyForInspection(equipment, today)

        then:
        0 * mailSender.send(_)
    }

}