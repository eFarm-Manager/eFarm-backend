package com.efarm.efarmbackend.service.agroactivity

import com.efarm.efarmbackend.model.agriculturalrecords.AgriculturalRecord
import com.efarm.efarmbackend.model.agriculturalrecords.AgriculturalRecordId
import com.efarm.efarmbackend.model.agroactivity.ActivityCategory
import com.efarm.efarmbackend.model.agroactivity.AgroActivity
import com.efarm.efarmbackend.model.agroactivity.AgroActivityDetailDTO
import com.efarm.efarmbackend.model.agroactivity.AgroActivityId
import com.efarm.efarmbackend.model.equipment.EquipmentSummaryDTO
import com.efarm.efarmbackend.model.farm.Farm
import com.efarm.efarmbackend.model.landparcel.Landparcel
import com.efarm.efarmbackend.model.landparcel.LandparcelId
import com.efarm.efarmbackend.model.user.UserSummaryDTO
import com.efarm.efarmbackend.payload.request.agroactivity.NewAgroActivityRequest
import com.efarm.efarmbackend.payload.request.agroactivity.UpdateAgroActivityRequest
import com.efarm.efarmbackend.repository.agroactivity.ActivityCategoryRepository
import com.efarm.efarmbackend.service.agriculturalrecords.AgriculturalRecordService
import com.efarm.efarmbackend.service.user.UserService
import spock.lang.Specification
import spock.lang.Subject

import java.time.Instant

class AgroActivityFacadeSpec extends Specification {

    def activityCategoryRepository = Mock(ActivityCategoryRepository)
    def userService = Mock(UserService)
    def agroActivityService = Mock(AgroActivityService)
    def agriculturalRecordService = Mock(AgriculturalRecordService)
    def activityHasEquipmentService = Mock(ActivityHasEquipmentService)
    def activityHasOperatorService = Mock(ActivityHasOperatorService)

    @Subject
    AgroActivityFacade agroActivityFacade = new AgroActivityFacade(
            activityCategoryRepository,
            userService,
            agroActivityService,
            agriculturalRecordService,
            activityHasEquipmentService,
            activityHasOperatorService
    )

    /*
    * addAgroActivity
    */

    def "should add agro activity"() {
        given:
        NewAgroActivityRequest request = new NewAgroActivityRequest(
                name: 'name',
                date: Instant.now(),
                description: 'description',
                activityCategoryName: 'category',
                operatorIds: [1, 2],
                equipmentIds: [1, 2],
        )

        Integer farmId = 1
        Farm farm = Mock(Farm) {
            getId() >> farmId
        }
        ActivityCategory activityCategory = Mock(ActivityCategory)
        AgriculturalRecord agriculturalRecord = Mock(AgriculturalRecord) {
            getId() >> new AgriculturalRecordId(1, farmId)
        }

        AgroActivity agroActivity = Mock(AgroActivity)

        userService.getLoggedUserFarm() >> farm
        activityCategoryRepository.findByName(request.getActivityCategoryName()) >> Optional.of(activityCategory)
        agriculturalRecordService.findAgriculturalRecordById(request.getAgriculturalRecordId(), farmId) >> agriculturalRecord
        agroActivityService.createNewAgroActivity(request, activityCategory, agriculturalRecord, farmId) >> agroActivity

        when:
        agroActivityFacade.addAgroActivity(request)

        then:
        1 * activityHasOperatorService.addOperatorsToActivity(agroActivity, request.getOperatorIds(), farmId);
        1 * activityHasEquipmentService.addEquipmentToActivity(request.getEquipmentIds(), agroActivity, farmId);
    }

    def "should throw runtime exception when activity category is not found"() {
        given:
        NewAgroActivityRequest request = new NewAgroActivityRequest(
                name: 'name',
                date: Instant.now(),
                description: 'description',
                activityCategoryName: 'category',
                operatorIds: [1, 2],
                equipmentIds: [1, 2],
        )

        Integer farmId = 1
        Farm farm = Mock(Farm) {
            getId() >> farmId
        }

        userService.getLoggedUserFarm() >> farm
        activityCategoryRepository.findByName(request.getActivityCategoryName()) >> Optional.empty()

        when:
        agroActivityFacade.addAgroActivity(request)

        then:
        RuntimeException e = thrown()
        e.message == "Nie znaleziono kategorii zabiegu"
    }

    /*
    * getAgroActivityDetails
    */

    def "should get agro activity details"() {
        given:
        Integer farmId = 1
        Integer activityId = 1
        Farm farm = Mock(Farm) {
            getId() >> farmId
        }
        AgroActivity agroActivity = Mock(AgroActivity) {
            getId() >> new AgroActivityId(activityId, farmId)
            getName() >> 'name'
            getAgriculturalRecord() >> Mock(AgriculturalRecord) {
                getId() >> new AgriculturalRecordId(1, farmId)
                getLandparcel() >> Mock(Landparcel) {
                    getId() >> new LandparcelId(1, farmId)
                    getName() >> 'name'
                }
                getArea() >> 22.0
            }
            getActivityCategory() >> Mock(ActivityCategory) {
                getName() >> 'category'
            }
            getDate() >> Instant.now()
            getIsCompleted() >> true
        }
        UserSummaryDTO userSummaryDTO = new UserSummaryDTO(
                id: 1,
                firstName: 'firstName',
                lastName: 'lastName',
                role: 'role'
        )
        EquipmentSummaryDTO equipmentSummaryDTO = new EquipmentSummaryDTO(
                equipmentId: 1,
                equipmentName: 'equipmentName',
                category: 'category',
                brand: 'brand',
                model: 'model'
        )

        userService.getLoggedUserFarm() >> farm
        agroActivityService.findAgroActivityWithDetails(activityId, farmId) >> agroActivity
        activityHasOperatorService.getOperatorsForAgroActivity(agroActivity) >> [userSummaryDTO]
        activityHasEquipmentService.getEquipmentsForAgroActivity(agroActivity) >> [equipmentSummaryDTO]

        when:
        AgroActivityDetailDTO result = agroActivityFacade.getAgroActivityDetails(activityId)

        then:
        result.getId() == activityId
        result.getName() == agroActivity.getName()
        result.getDate() == agroActivity.getDate()
        result.getIsCompleted() == agroActivity.getIsCompleted()
        result.getCategoryName() == agroActivity.getActivityCategory().getName()
        result.getArea() == agroActivity.getAgriculturalRecord().getArea()
        result.getLandparcel().getName() == agroActivity.getAgriculturalRecord().getLandparcel().getName()
        result.getOperators() == [userSummaryDTO]
        result.getEquipment() == [equipmentSummaryDTO]
    }

    /*
    * updateAgroActivity
    */

    def "should update agro activity"() {
        given:
        UpdateAgroActivityRequest request = new UpdateAgroActivityRequest(
                name: 'name',
                date: Instant.now(),
                description: 'description',
                activityCategoryName: 'category',
                operatorIds: [1, 2],
                equipmentIds: [1, 2],
        )
        Integer intAgroActivityId = 1
        Integer farmId = 1
        Farm farm = Mock(Farm) {
            getId() >> farmId
        }
        AgroActivity agroActivity = Mock(AgroActivity) {
            getId() >> new AgroActivityId(intAgroActivityId, farmId)
        }
        ActivityCategory activityCategory = Mock(ActivityCategory) {
            getName() >> request.getActivityCategoryName()
        }

        userService.getLoggedUserFarm() >> farm
        agroActivityService.findAgroActivityWithDetails(intAgroActivityId, farmId) >> agroActivity
        activityCategoryRepository.findByName(request.getActivityCategoryName()) >> Optional.of(activityCategory)

        when:
        agroActivityFacade.updateAgroActivity(intAgroActivityId, request)

        then:
        1 * agroActivityService.updateAgroActivity(request, agroActivity, activityCategory);
        1 * activityHasOperatorService.updateOperatorInActivity(request.getOperatorIds(), agroActivity, farmId);
        1 * activityHasEquipmentService.updateEquipmentInActivity(request.getEquipmentIds(), agroActivity, farmId);
    }

    def "should throw runtime exception when activity category is not found"() {
        given:
        UpdateAgroActivityRequest request = new UpdateAgroActivityRequest(
                name: 'name',
                date: Instant.now(),
                description: 'description',
                activityCategoryName: 'category',
                operatorIds: [1, 2],
                equipmentIds: [1, 2],
        )
        Integer intAgroActivityId = 1
        Integer farmId = 1

        Farm farm = Mock(Farm) {
            getId() >> farmId
        }
        AgroActivity agroActivity = Mock(AgroActivity) {
            getId() >> new AgroActivityId(intAgroActivityId, farmId)
        }

        userService.getLoggedUserFarm() >> farm
        agroActivityService.findAgroActivityWithDetails(intAgroActivityId, farmId) >> agroActivity
        activityCategoryRepository.findByName(request.getActivityCategoryName()) >> Optional.empty()

        when:
        agroActivityFacade.updateAgroActivity(intAgroActivityId, request)

        then:
        RuntimeException e = thrown()
        e.message == "Nie znaleziono kategorii zabiegu"
    }

    /*
    * deleteAgroActivity
    */

    def "should delete agro activity"() {
        given:
        Integer id = 1
        Integer farmId = 1
        Farm farm = Mock(Farm) {
            getId() >> farmId
        }
        AgroActivityId agroActivityId = new AgroActivityId(id, farmId)

        userService.getLoggedUserFarm() >> farm

        when:
        agroActivityFacade.deleteAgroActivity(id)

        then:
        1 * agroActivityService.deleteAgroActivity(agroActivityId)
    }
}