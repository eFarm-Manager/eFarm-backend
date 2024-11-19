package com.efarm.efarmbackend.service.agroactivity

import com.efarm.efarmbackend.model.agriculturalrecords.AgriculturalRecord;
import com.efarm.efarmbackend.model.agriculturalrecords.AgriculturalRecordId;
import com.efarm.efarmbackend.model.agroactivity.ActivityCategory;
import com.efarm.efarmbackend.model.agroactivity.AgroActivity;
import com.efarm.efarmbackend.model.agroactivity.AgroActivityId;
import com.efarm.efarmbackend.model.agroactivity.AgroActivitySummaryDTO;
import com.efarm.efarmbackend.model.farm.Farm;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.model.agroactivity.ActivityHasOperator;
import com.efarm.efarmbackend.payload.request.agroactivity.NewAgroActivityRequest;
import com.efarm.efarmbackend.payload.request.agroactivity.UpdateAgroActivityRequest;
import com.efarm.efarmbackend.repository.agriculturalrecords.AgriculturalRecordRepository;
import com.efarm.efarmbackend.repository.agroactivity.ActivityHasEquipmentRepository;
import com.efarm.efarmbackend.repository.agroactivity.ActivityHasOperatorRepository;
import com.efarm.efarmbackend.repository.agroactivity.AgroActivityRepository;
import com.efarm.efarmbackend.service.user.UserService;

import java.time.Instant;
import java.util.List
import spock.lang.Subject
import spock.lang.Specification

class AgroActivityServiceSpec extends Specification {

    def activityHasOperatorRepository = Mock(ActivityHasOperatorRepository)
    def userService = Mock(UserService)
    def activityHasEquipmentRepository = Mock(ActivityHasEquipmentRepository)
    def agroActivityRepository = Mock(AgroActivityRepository)
    def agriculturalRecordRepository = Mock(AgriculturalRecordRepository)

    @Subject
    AgroActivityService agroActivityService = new AgroActivityService(
        activityHasOperatorRepository: activityHasOperatorRepository,
        userService: userService,
        activityHasEquipmentRepository: activityHasEquipmentRepository,
        agroActivityRepository: agroActivityRepository,
        agriculturalRecordRepository: agriculturalRecordRepository
    )

    /*
    * createNewAgroActivity
    */

    def "should create new agro activity"() {
        given:
        NewAgroActivityRequest newAgroActivityRequest = new NewAgroActivityRequest(
            name: "Activity1",
            description: "Description1",
            appliedDose: '',
            usedSubstances: ''
        )
        ActivityCategory activityCategory = Mock(ActivityCategory) {
            getName() >> "category1"
        }
        AgriculturalRecord agriculturalRecord = Mock(AgriculturalRecord) {
            getId() >> new AgriculturalRecordId(1, 1)
        }
        Integer farmId = 1

        agroActivityRepository.findNextFreeIdForFarm(farmId) >> 1

        when:
        AgroActivity result = agroActivityService.createNewAgroActivity(newAgroActivityRequest, activityCategory, agriculturalRecord, farmId)

        then:
        1 * agroActivityRepository.save(_);
        result.getName() == newAgroActivityRequest.name
        result.getDescription() == newAgroActivityRequest.description
        result.getAppliedDose() == newAgroActivityRequest.appliedDose
        result.getUsedSubstances() == newAgroActivityRequest.usedSubstances
        result.getActivityCategory().getName() == activityCategory.getName()
        result.getAgriculturalRecord().getId() == agriculturalRecord.getId()
    }

    /*
    * getAgroActivitiesByAgriculturalRecord
    */

    def "should get agro activities by agricultural record"() {
        given:
        Integer id = 1
        Integer farmId = 1
        Farm farm = Mock(Farm) {
            getId() >> farmId
        }
        AgriculturalRecordId agriculturalRecordId = new AgriculturalRecordId(id, farmId)

        AgriculturalRecord agriculturalRecord = Mock(AgriculturalRecord) {
            getId() >> agriculturalRecordId
        }

        AgroActivity agroActivity1 = Mock(AgroActivity) {
            getId() >> new AgroActivityId(1, farmId)
            getName() >> "Activity1"
            getDate() >> Instant.parse("2024-01-01T00:00:00Z")
            getIsCompleted() >> true
            getActivityCategory() >> Mock(ActivityCategory) {
                getName() >> "category1"
            }
        }
        AgroActivity agroActivity2 = Mock(AgroActivity) {
            getId() >> new AgroActivityId(2, farmId)
            getName() >> "Activity2"
            getDate() >> Instant.parse("2024-01-02T00:00:00Z")
            getIsCompleted() >> false
            getActivityCategory() >> Mock(ActivityCategory) {
                getName() >> "category2"
            }
        }
        List<AgroActivity> agroActivities = [agroActivity1, agroActivity2]

        userService.getLoggedUserFarm() >> farm
        agriculturalRecordRepository.findById(agriculturalRecordId) >> Optional.of(agriculturalRecord)
        agroActivityRepository.findByAgriculturalRecord(agriculturalRecord) >> agroActivities

        when:
        List<AgroActivitySummaryDTO> result = agroActivityService.getAgroActivitiesByAgriculturalRecord(id)

        then:
        result.size() == 2
        result[0].id == agroActivity1.getId().getId()
        result[0].name == agroActivity1.getName()
        result[0].date == agroActivity1.getDate()
        result[0].isCompleted == agroActivity1.getIsCompleted()
        result[0].categoryName == agroActivity1.getActivityCategory().getName()
        result[1].id == agroActivity2.getId().getId()
        result[1].name == agroActivity2.getName()
        result[1].date == agroActivity2.getDate()
        result[1].isCompleted == agroActivity2.getIsCompleted()
        result[1].categoryName == agroActivity2.getActivityCategory().getName()
    }

    def "should throw runtime exception when agricultural record doesnt exist by id"() {
        given:
        Integer id = 1
        Integer farmId = 1
        Farm farm = Mock(Farm) {
            getId() >> farmId
        }
        AgriculturalRecordId agriculturalRecordId = new AgriculturalRecordId(id, farmId)

        userService.getLoggedUserFarm() >> farm
        agriculturalRecordRepository.findById(agriculturalRecordId) >> Optional.empty()

        when:
        List<AgroActivitySummaryDTO> result = agroActivityService.getAgroActivitiesByAgriculturalRecord(id)

        then:
        RuntimeException ex = thrown()
        ex.message == 'Nie znaleziono ewidencji'
    }

    /*
    * findAgroActivityWithDetails
    */

    def "should find agro activity with details"() {
        given:
        Integer id = 1
        Integer farmId = 1

        AgroActivityId agroActivityId = new AgroActivityId(id, farmId)
        AgroActivity agroActivity = Mock(AgroActivity) {
            getId() >> agroActivityId
            getName() >> "Activity1"
            getDescription() >> "Description1"
            getAppliedDose() >> ''
            getUsedSubstances() >> ''
            getDate() >> Instant.parse("2024-01-01T00:00:00Z")
            getIsCompleted() >> true
            getActivityCategory() >> Mock(ActivityCategory) {
                getName() >> "category1"
            }
            getAgriculturalRecord() >> Mock(AgriculturalRecord) {
                getId() >> new AgriculturalRecordId(1, farmId)
            }
        }
        agroActivityRepository.findWithDetailsById(agroActivityId) >> Optional.of(agroActivity)

        when:
        AgroActivity result = agroActivityService.findAgroActivityWithDetails(id,farmId)

        then:
        result.getId().getId() == agroActivity.getId().getId()
        result.getName() == agroActivity.getName()
        result.getDescription() == agroActivity.getDescription()
        result.getAppliedDose() == agroActivity.getAppliedDose()
        result.getUsedSubstances() == agroActivity.getUsedSubstances()
        result.getDate() == agroActivity.getDate()
        result.getIsCompleted() == agroActivity.getIsCompleted()
        result.getActivityCategory().getName() == agroActivity.getActivityCategory().getName()
        result.getAgriculturalRecord().getId() == agroActivity.getAgriculturalRecord().getId()
    }

    def "should throw runtime exception when agro activity doesnt exist by id"() {
        given:
        Integer id = 1
        Integer farmId = 1

        AgroActivityId agroActivityId = new AgroActivityId(id, farmId)

        agroActivityRepository.findWithDetailsById(agroActivityId) >> Optional.empty()

        when:
        AgroActivity result = agroActivityService.findAgroActivityWithDetails(id,farmId)

        then:
        RuntimeException ex = thrown()
        ex.message == 'Nie znaleziono zabiegu agrotechnicznego'
    }

    /*
    * updateAgroActivity
    */

    def "should update agro activity"() {
        given:
        UpdateAgroActivityRequest updateAgroActivityRequest = new UpdateAgroActivityRequest(
            name: "Activity1",
            description: "Description1",
            appliedDose: '',
            usedSubstances: ''
        )
        AgroActivity agroActivity = Mock(AgroActivity)
        ActivityCategory activityCategory = Mock(ActivityCategory) {
            getName() >> "category1"
        }

        when:
        AgroActivity result = agroActivityService.updateAgroActivity(updateAgroActivityRequest, agroActivity, activityCategory)

        then:
        1 * agroActivityRepository.save(_);
    }

    /*
    * deleteAgroActivity
    */

    def "should delete agro activity"() {
        given:
        AgroActivityId agroActivityId = new AgroActivityId(1, 1)
        AgroActivity agroActivity = Mock(AgroActivity) {
            getId() >> agroActivityId
        }

        agroActivityRepository.findById(agroActivityId) >> Optional.of(agroActivity)

        when:
        agroActivityService.deleteAgroActivity(agroActivityId)

        then:
        1 * activityHasOperatorRepository.deleteActivityHasOperatorsByAgroActivity(agroActivity);
        1 * activityHasEquipmentRepository.deleteActivityHasEquipmentsByAgroActivity(agroActivity);
        1 * agroActivityRepository.delete(agroActivity)
    }

    def "should throw runtime exception when agro activity doesnt exist by id"() {
        given:
        AgroActivityId agroActivityId = new AgroActivityId(1, 1)

        agroActivityRepository.findById(agroActivityId) >> Optional.empty()

        when:
        agroActivityService.deleteAgroActivity(agroActivityId)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == 'Nie znaleziono zabiegu agrotechnicznego o ID: 1'
    }

    /*
    * getAssignedIncompleteActivitiesForLoggedUser
    */

    def "should get assigned incomplete activities for logged in user"() {
        given:
        User user = Mock(User) {
            getId() >> 1
        }
        AgroActivity agroActivity1 = Mock(AgroActivity) {
            getId() >> new AgroActivityId(1, 1)
            getName() >> "Activity1"
            getDate() >> Instant.parse("2024-01-01T00:00:00Z")
            getIsCompleted() >> false
            getActivityCategory() >> Mock(ActivityCategory) {
                getName() >> "category1"
            }
        }
        AgroActivity agroActivity2 = Mock(AgroActivity) {
            getId() >> new AgroActivityId(2, 1)
            getName() >> "Activity2"
            getDate() >> Instant.parse("2024-01-02T00:00:00Z")
            getIsCompleted() >> false
            getActivityCategory() >> Mock(ActivityCategory) {
                getName() >> "category2"
            }
        }

        userService.getLoggedUser() >> user
        agroActivityRepository.findIncompleteActivitiesAssignedToUser(user.getId()) >> [agroActivity1, agroActivity2]

        when:
        List<AgroActivitySummaryDTO> result = agroActivityService.getAssignedIncompleteActivitiesForLoggedUser()

        then:
        result.size() == 2
        result[0].id == agroActivity1.getId().getId()
        result[0].name == agroActivity1.getName()
        result[0].date == agroActivity1.getDate()
        result[0].isCompleted == agroActivity1.getIsCompleted()
        result[0].categoryName == agroActivity1.getActivityCategory().getName()
        result[1].id == agroActivity2.getId().getId()
        result[1].name == agroActivity2.getName()
        result[1].date == agroActivity2.getDate()
        result[1].isCompleted == agroActivity2.getIsCompleted()
        result[1].categoryName == agroActivity2.getActivityCategory().getName()
    }

    /*
    * markActivityAsCompleted
    */

    def "should successfully mark activity as completed"() {
        given:
        Farm farm = Mock(Farm) {
            getId() >> 1
        }
        Integer id = 1;
        AgroActivityId agroActivityId = new AgroActivityId(id, farm.getId())
        AgroActivity agroActivity = Mock(AgroActivity) {
            getId() >> agroActivityId
            getIsCompleted() >> false
        }
        User user = Mock(User) {
            getId() >> 1
        }
        ActivityHasOperator activityHasOperator = Mock(ActivityHasOperator) {
            getUser() >> user
            getAgroActivity() >> agroActivity
        }

        agroActivityRepository.findById(agroActivityId) >> Optional.of(agroActivity)
        userService.getLoggedUserFarm() >> farm
        userService.getLoggedUser() >> user
        activityHasOperatorRepository.findActivityHasOperatorsByAgroActivity(agroActivity) >> [activityHasOperator]

        when:
        agroActivityService.markActivityAsCompleted(id)

        then:
        1 * agroActivity.setIsCompleted(true)
        1 * agroActivityRepository.save(agroActivity)
    }

    def "should throw runtime exception when activity is not found"() {
        given:
        Integer id = 1
        Farm farm = Mock(Farm) {
            getId() >> 1
        }
        AgroActivityId agroActivityId = new AgroActivityId(id, farm.getId())

        userService.getLoggedUserFarm() >> farm
        agroActivityRepository.findById(agroActivityId) >> Optional.empty()

        when:
        agroActivityService.markActivityAsCompleted(id)

        then:
        RuntimeException ex = thrown()
        ex.message == 'Nie znaleziono zadania'
    }

    def "should not allow for marking activity as completed for completed task"() {
        given:
        Farm farm = Mock(Farm) {
            getId() >> 1
        }
        Integer id = 1;
        AgroActivityId agroActivityId = new AgroActivityId(id, farm.getId())
        AgroActivity agroActivity = Mock(AgroActivity) {
            getId() >> agroActivityId
            getIsCompleted() >> true
        }
        User user = Mock(User) {
            getId() >> 1
        }
        ActivityHasOperator activityHasOperator = Mock(ActivityHasOperator) {
            getUser() >> user
            getAgroActivity() >> agroActivity
        }

        agroActivityRepository.findById(agroActivityId) >> Optional.of(agroActivity)
        userService.getLoggedUserFarm() >> farm
        userService.getLoggedUser() >> user
        activityHasOperatorRepository.findActivityHasOperatorsByAgroActivity(agroActivity) >> [activityHasOperator]


        when:
        agroActivityService.markActivityAsCompleted(id)

        then:
        RuntimeException ex = thrown()
        ex.message == 'Nie masz dostępu do tego zadania'
    }

    def "should not allow for marking activity as completed for user not assigned in activity"() {
        given:
        Farm farm = Mock(Farm) {
            getId() >> 1
        }
        Integer id = 1;
        AgroActivityId agroActivityId = new AgroActivityId(id, farm.getId())
        AgroActivity agroActivity = Mock(AgroActivity) {
            getId() >> agroActivityId
            getIsCompleted() >> false
        }
        User user = Mock(User) {
            getId() >> 1
        }
        User user2 = Mock(User) {
            getId() >> 2
        }
        ActivityHasOperator activityHasOperator = Mock(ActivityHasOperator) {
            getUser() >> user2
            getAgroActivity() >> agroActivity
        }

        agroActivityRepository.findById(agroActivityId) >> Optional.of(agroActivity)
        userService.getLoggedUserFarm() >> farm
        userService.getLoggedUser() >> user
        activityHasOperatorRepository.findActivityHasOperatorsByAgroActivity(agroActivity) >> [activityHasOperator]


        when:
        agroActivityService.markActivityAsCompleted(id)

        then:
        RuntimeException ex = thrown()
        ex.message == 'Nie masz dostępu do tego zadania'
    }
}