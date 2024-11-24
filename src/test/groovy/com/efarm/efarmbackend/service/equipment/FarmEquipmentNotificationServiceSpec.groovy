package com.efarm.efarmbackend.service.equipment;

import com.efarm.efarmbackend.model.equipment.FarmEquipment;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.model.farm.Farm
import com.efarm.efarmbackend.repository.equipment.FarmEquipmentRepository;
import com.efarm.efarmbackend.service.user.UserService;
import com.efarm.efarmbackend.service.MainNotificationService
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
    def mainNotificationService = Mock(MainNotificationService)
    def userService = Mock(UserService)
    @Subject
    FarmEquipmentNotificationService farmEquipmentNotificationService = new FarmEquipmentNotificationService(
            farmEquipmentRepository,
	        mainNotificationService,
	        userService
    )

    def "should send notification for insurance expiring in 14 days"() {
        given:
        LocalDate today = LocalDate.now()
        FarmEquipment equipment = Mock(FarmEquipment) {
            getInsuranceExpirationDate() >> today.plusDays(14)
            getEquipmentName() >> "Tractor"
            getInsurancePolicyNumber() >> "INS123"
            getFarmIdFarm() >> Mock(Farm) { 
                getId() >> 1 
                getIsActive() >> true
            }
        }
        User owner = Mock(User) { 
		getEmail() >> "owner@example.com"
		getIsActive() >> true
 }
        userService.getAllOwnersForFarm(1) >> [owner]

        when:
        farmEquipmentNotificationService.checkAndNotifyForInsurance(equipment, today)

        then:
        1 * mainNotificationService.sendNotificationToUser(owner, "W twoim sprzęcie Tractor polisa ubezpieczeniowa o numerze INS123 wygasa za 14 dni.", "Ubezpieczenie sprzętu wygasa!")
    }

    def "should send notification for insurance expiring in 3 days"() {
        given:
        LocalDate today = LocalDate.now()
        FarmEquipment equipment = Mock(FarmEquipment) {
            getInsuranceExpirationDate() >> today.plusDays(3)
            getEquipmentName() >> "Plow"
            getInsurancePolicyNumber() >> "INS456"
            getFarmIdFarm() >> Mock(Farm) { 
                getId() >> 2
                getIsActive() >> true
            }
        }
        User owner = Mock(User) { 
		getEmail() >> "owner@example.com" 
		getIsActive() >> true
	}
        userService.getAllOwnersForFarm(2) >> [owner]

        when:
        farmEquipmentNotificationService.checkAndNotifyForInsurance(equipment, today)

        then:
        1 * mainNotificationService.sendNotificationToUser(owner,"W twoim sprzęcie Plow polisa ubezpieczeniowa o numerze INS456 wygasa za 3 dni.", "Ubezpieczenie sprzętu wygasa!")
    }

    def "should send notification for insurance expiring in 1 day"() {
        given:
        LocalDate today = LocalDate.now()
        FarmEquipment equipment = Mock(FarmEquipment) {
            getInsuranceExpirationDate() >> today.plusDays(1)
            getEquipmentName() >> "Seeder"
            getInsurancePolicyNumber() >> "INS789"
            getFarmIdFarm() >> Mock(Farm) { 
                getId() >> 3
                getIsActive() >> true
            }
        }
        User owner = Mock(User) { 
		getEmail() >> "owner@example.com" 
		getIsActive() >> true
	}
        userService.getAllOwnersForFarm(3) >> [owner]

        when:
        farmEquipmentNotificationService.checkAndNotifyForInsurance(equipment, today)

        then:
        1 * mainNotificationService.sendNotificationToUser(owner, "W twoim sprzęcie Seeder polisa ubezpieczeniowa o numerze INS789 wygasa za 1 dni.", "Ubezpieczenie sprzętu wygasa!")
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
        0 * mainNotificationService.sendNotificationToUser(_,_,_)
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
        0 * mainNotificationService.sendNotificationToUser(_,_,_)
    }

    // insurance

    def "should send notification for inspection expiring in 14 days"() {
        given:
        LocalDate today = LocalDate.now()
        FarmEquipment equipment = Mock(FarmEquipment) {
            getInspectionExpireDate() >> today.plusDays(14)
            getEquipmentName() >> "Tractor"
            getFarmIdFarm() >> Mock(Farm) { 
                getId() >> 1 
                getIsActive() >> true
            }
        }
        User owner = Mock(User) { getEmail() >> "owner@example.com" }
        userService.getAllOwnersForFarm(1) >> [owner]

        when:
        farmEquipmentNotificationService.checkAndNotifyForInspection(equipment, today)

        then:
        1 * mainNotificationService.sendNotificationToUser(owner, "W twoim sprzęcie Tractor przegląd techniczny wygasa za 14 dni.", "Przegląd techniczny wygasa!")
    }

    def "should send notification for inspection expiring in 3 days"() {
        given:
        LocalDate today = LocalDate.now()
        FarmEquipment equipment = Mock(FarmEquipment) {
            getInspectionExpireDate() >> today.plusDays(3)
            getEquipmentName() >> "Plow"
            getFarmIdFarm() >> Mock(Farm) { 
                getId() >> 2
                getIsActive() >> true
            }
        }
        User owner = Mock(User) { getEmail() >> "owner@example.com" }
        userService.getAllOwnersForFarm(2) >> [owner]

        when:
        farmEquipmentNotificationService.checkAndNotifyForInspection(equipment, today)

        then:
        1 * mainNotificationService.sendNotificationToUser(owner, "W twoim sprzęcie Plow przegląd techniczny wygasa za 3 dni.", "Przegląd techniczny wygasa!")
    }

    def "should send notification for inspection expiring in 1 day"() {
        given:
        LocalDate today = LocalDate.now()
        FarmEquipment equipment = Mock(FarmEquipment) {
            getInspectionExpireDate() >> today.plusDays(1)
            getEquipmentName() >> "Seeder"
            getFarmIdFarm() >> Mock(Farm) { 
                getId() >> 3
                getIsActive() >> true
            }
        }
        User owner = Mock(User) { getEmail() >> "owner@example.com" }
        userService.getAllOwnersForFarm(3) >> [owner]

        when:
        farmEquipmentNotificationService.checkAndNotifyForInspection(equipment, today)

        then:
        1 * mainNotificationService.sendNotificationToUser(owner, "W twoim sprzęcie Seeder przegląd techniczny wygasa za 1 dni.", "Przegląd techniczny wygasa!")
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
        0 * mainNotificationService.sendNotificationToUser(_,_,_)
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
        0 * mainNotificationService.sendNotificationToUser(_,_,_)
    }

}
