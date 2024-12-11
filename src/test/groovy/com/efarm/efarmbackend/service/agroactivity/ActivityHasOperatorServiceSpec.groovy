package com.efarm.efarmbackend.service.agroactivity

import com.efarm.efarmbackend.model.agroactivity.ActivityHasOperator
import com.efarm.efarmbackend.model.agroactivity.AgroActivity
import com.efarm.efarmbackend.model.farm.Farm
import com.efarm.efarmbackend.model.user.ERole
import com.efarm.efarmbackend.model.user.Role
import com.efarm.efarmbackend.model.user.User
import com.efarm.efarmbackend.model.user.UserSummaryDTO
import com.efarm.efarmbackend.repository.agroactivity.ActivityHasOperatorRepository
import com.efarm.efarmbackend.repository.user.UserRepository
import com.efarm.efarmbackend.service.user.UserService
import spock.lang.Specification
import spock.lang.Subject

class ActivityHasOperatorServiceSpec extends Specification {

    def userRepository = Mock(UserRepository)
    def userService = Mock(UserService)
    def activityHasOperatorRepository = Mock(ActivityHasOperatorRepository)
    def applicationContext = Mock(org.springframework.context.ApplicationContext)

    @Subject
    ActivityHasOperatorService activityHasOperatorService = new ActivityHasOperatorService(
            userRepository,
            userService,
            applicationContext,
            activityHasOperatorRepository
    )

    /*
    * addOperatorToActivity
    */

    def "should add operators to activity"() {
        given:
        List<Integer> operatorIds = [1, 2]
        AgroActivity agroActivity = Mock(AgroActivity)
        Integer farmId = 1
        Farm farm = Mock(Farm) {
            getId() >> farmId
        }

        User user1 = Mock(User) {
            getId() >> 1
            getIsActive() >> true
            getFarm() >> farm
        }
        User user2 = Mock(User) {
            getId() >> 2
            getIsActive() >> true
            getFarm() >> farm
        }
        userRepository.findById(1) >> Optional.of(user1)
        userRepository.findById(2) >> Optional.of(user2)

        when:
        activityHasOperatorService.addOperatorsToActivity(agroActivity, operatorIds, farmId)

        then:
        2 * activityHasOperatorRepository.save(_)
    }

    def "should add logged in user as operator to activity"() {
        given:
        List<Integer> operatorIds = null
        AgroActivity agroActivity = Mock(AgroActivity)
        Integer farmId = 1
        Farm farm = Mock(Farm) {
            getId() >> farmId
        }

        User user = Mock(User) {
            getId() >> 1
            getIsActive() >> true
            getFarm() >> farm
        }
        userService.getLoggedUser() >> user

        when:
        activityHasOperatorService.addOperatorsToActivity(agroActivity, operatorIds, farmId)

        then:
        1 * activityHasOperatorRepository.save(_)
    }

    def "should throw exception when user not found"() {
        given:
        List<Integer> operatorIds = [1, 2]
        AgroActivity agroActivity = Mock(AgroActivity)
        Integer farmId = 1
        Farm farm = Mock(Farm) {
            getId() >> farmId
        }

        User user1 = Mock(User) {
            getId() >> 1
            getIsActive() >> true
            getFarm() >> farm
        }
        User user2 = Mock(User) {
            getId() >> 2
            getIsActive() >> true
            getFarm() >> farm
        }
        userRepository.findById(1) >> Optional.of(user1)
        userRepository.findById(2) >> Optional.empty()

        when:
        activityHasOperatorService.addOperatorsToActivity(agroActivity, operatorIds, farmId)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == "Nie znaleziono użytkownika o ID: 2"
    }

    def "should throw exception when any user is not active"() {
        given:
        List<Integer> operatorIds = [1, 2]
        AgroActivity agroActivity = Mock(AgroActivity)
        Integer farmId = 1
        Farm farm = Mock(Farm) {
            getId() >> farmId
        }

        User user1 = Mock(User) {
            getId() >> 1
            getIsActive() >> true
            getFarm() >> farm
        }
        User user2 = Mock(User) {
            getId() >> 2
            getFirstName() >> "John"
            getLastName() >> "Doe"
            getIsActive() >> false
            getFarm() >> farm
        }
        userRepository.findById(1) >> Optional.of(user1)
        userRepository.findById(2) >> Optional.of(user2)

        when:
        activityHasOperatorService.addOperatorsToActivity(agroActivity, operatorIds, farmId)

        then:
        IllegalStateException ex = thrown()
        ex.message == 'Użytkownik ' + user2.getFirstName() + ' ' + user2.getLastName() + ' jest niedostępny'
    }

    def "should throw exception when user doesnt belong to current farm"() {
        given:
        List<Integer> operatorIds = [1, 2]
        AgroActivity agroActivity = Mock(AgroActivity)
        Integer farmId = 1
        Farm farm = Mock(Farm) {
            getId() >> farmId
        }

        User user1 = Mock(User) {
            getId() >> 1
            getIsActive() >> true
            getFarm() >> farm
        }
        User user2 = Mock(User) {
            getId() >> 2
            getIsActive() >> true
            getFarm() >> Mock(Farm) {
                getId() >> 2
            }
        }
        userRepository.findById(1) >> Optional.of(user1)
        userRepository.findById(2) >> Optional.of(user2)

        when:
        activityHasOperatorService.addOperatorsToActivity(agroActivity, operatorIds, farmId)

        then:
        IllegalStateException ex = thrown()
        ex.message == 'Użytkownik o ID: 2 nie należy do tej farmy'
    }

    /*
    * getOperatorsForAgroActivity
    */

    def "should get operators for agro activity"() {
        given:
        AgroActivity agroActivity = Mock(AgroActivity)

        User user1 = Mock(User) {
            getId() >> 1
            getFirstName() >> "John"
            getLastName() >> "Doe"
            getRole() >> Mock(Role) {
                getName() >> ERole.ROLE_FARM_OWNER
            }
        }

        User user2 = Mock(User) {
            getId() >> 2
            getFirstName() >> "Jane"
            getLastName() >> "Doe"
            getRole() >> Mock(Role) {
                getName() >> ERole.ROLE_FARM_EQUIPMENT_OPERATOR
            }
        }

        List<ActivityHasOperator> activityHasOperators = [
                Mock(ActivityHasOperator) {
                    getUser() >> user1
                },
                Mock(ActivityHasOperator) {
                    getUser() >> user2
                }
        ]

        activityHasOperatorRepository.findActivityHasOperatorsByAgroActivity(agroActivity) >> activityHasOperators

        when:
        List<UserSummaryDTO> operators = activityHasOperatorService.getOperatorsForAgroActivity(agroActivity)

        then:
        operators.size() == 2
        operators[0].getId() == 1
        operators[0].getFirstName() == 'John'
        operators[0].getLastName() == 'Doe'
        operators[0].getRole() == 'ROLE_FARM_OWNER'
        operators[1].getId() == 2
        operators[1].getFirstName() == 'Jane'
        operators[1].getLastName() == 'Doe'
        operators[1].getRole() == 'ROLE_FARM_EQUIPMENT_OPERATOR'
    }

    /*
    * UpdateOperatorInActivity
    */

    def "should update operator in activity"() {
        given:
        List<Integer> operatorsIds = [1, 2]
        AgroActivity agroActivity = Mock(AgroActivity)
        Integer farmId = 1

        Farm farm = Mock(Farm) {
            getId() >> farmId
        }

        User user1 = Mock(User) {
            getId() >> 1
            getIsActive() >> true
            getFarm() >> farm
        }
        User user2 = Mock(User) {
            getId() >> 2
            getIsActive() >> true
            getFarm() >> farm
        }
        userRepository.findById(1) >> Optional.of(user1)
        userRepository.findById(2) >> Optional.of(user2)

        applicationContext.getBean(ActivityHasOperatorService.class) >> activityHasOperatorService

        when:
        activityHasOperatorService.updateOperatorInActivity(operatorsIds, agroActivity, farmId)

        then:
        1 * activityHasOperatorRepository.deleteActivityHasOperatorsByAgroActivity(agroActivity)
        2 * activityHasOperatorRepository.save(_)
    }
}