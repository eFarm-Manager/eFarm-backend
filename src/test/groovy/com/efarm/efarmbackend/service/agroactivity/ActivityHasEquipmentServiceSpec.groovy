package com.efarm.efarmbackend.service.agroactivity


import com.efarm.efarmbackend.model.agroactivity.ActivityHasEquipment
import com.efarm.efarmbackend.model.agroactivity.AgroActivity
import com.efarm.efarmbackend.model.equipment.EquipmentCategory
import com.efarm.efarmbackend.model.equipment.EquipmentSummaryDTO
import com.efarm.efarmbackend.model.equipment.FarmEquipment
import com.efarm.efarmbackend.model.equipment.FarmEquipmentId
import com.efarm.efarmbackend.repository.agroactivity.ActivityHasEquipmentRepository
import com.efarm.efarmbackend.repository.equipment.FarmEquipmentRepository
import spock.lang.Specification
import spock.lang.Subject

class ActivityHasEquipmentServiceSpec extends Specification {

    def farmEquipmentRepository = Mock(FarmEquipmentRepository)
    def activityHasEquipmentRepository = Mock(ActivityHasEquipmentRepository)
    def applicationContext = Mock(org.springframework.context.ApplicationContext)

    @Subject
    ActivityHasEquipmentService activityHasEquipmentService = new ActivityHasEquipmentService(
            farmEquipmentRepository,
            applicationContext,
            activityHasEquipmentRepository
    )
    /*
    * addEquipmentToActivity
    */

    def "should add equipment to activity"() {
        given:
        List<Integer> equipmentIds = [1, 2]
        AgroActivity agroActivity = Mock(AgroActivity)
        Integer farmId = 1

        FarmEquipment farmEquipment1 = Mock(FarmEquipment) {
            getId() >> new FarmEquipmentId(1, farmId)
            getIsAvailable() >> true
        }
        FarmEquipment farmEquipment2 = Mock(FarmEquipment) {
            getId() >> new FarmEquipmentId(2, farmId)
            getIsAvailable() >> true
        }
        List<FarmEquipment> farmEquipmentList = [farmEquipment1, farmEquipment2]
        farmEquipmentRepository.findAllById(_) >> farmEquipmentList

        when:
        activityHasEquipmentService.addEquipmentToActivity(equipmentIds, agroActivity, farmId)

        then:
        2 * activityHasEquipmentRepository.save(_)
    }

    def "should throw illegal state exception when any equipment is not available"() {
        given:
        List<Integer> equipmentIds = [1, 2]
        AgroActivity agroActivity = Mock(AgroActivity)
        Integer farmId = 1

        FarmEquipment farmEquipment1 = Mock(FarmEquipment) {
            getId() >> new FarmEquipmentId(1, farmId)
            getEquipmentName() >> 'Zetor'
            getIsAvailable() >> false
        }
        FarmEquipment farmEquipment2 = Mock(FarmEquipment) {
            getId() >> new FarmEquipmentId(2, farmId)
            getIsAvailable() >> false
        }
        List<FarmEquipment> farmEquipmentList = [farmEquipment1, farmEquipment2]
        farmEquipmentRepository.findAllById(_) >> farmEquipmentList

        when:
        activityHasEquipmentService.addEquipmentToActivity(equipmentIds, agroActivity, farmId)

        then:
        IllegalStateException ex = thrown()
        ex.message == 'Sprzęt Zetor jest niedostępny'
    }

    def "should throw exception when equipment id doesnt exist"() {
        given:
        List<Integer> equipmentIds = [1, 2, 3]
        AgroActivity agroActivity = Mock(AgroActivity)
        Integer farmId = 1

        FarmEquipment farmEquipment1 = Mock(FarmEquipment) {
            getId() >> new FarmEquipmentId(1, farmId)
            getIsAvailable() >> true
        }
        FarmEquipment farmEquipment2 = Mock(FarmEquipment) {
            getId() >> new FarmEquipmentId(2, farmId)
            getIsAvailable() >> true
        }
        List<FarmEquipment> farmEquipmentList = [farmEquipment1, farmEquipment2]
        farmEquipmentRepository.findAllById(_) >> farmEquipmentList

        when:
        activityHasEquipmentService.addEquipmentToActivity(equipmentIds, agroActivity, farmId)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == 'Sprzęty o następujących identyfikatorach nie istnieją: [3]'
    }

    /*
    * getEquipmentsForAgroActivity
    */

    def "should get equipments for agro activity"() {
        given:
        AgroActivity agroActivity = Mock(AgroActivity)
        FarmEquipment farmEquipment1 = Mock(FarmEquipment) {
            getId() >> new FarmEquipmentId(1, 1)
            getEquipmentName() >> 'Zetor'
            getCategory() >> Mock(EquipmentCategory) {
                getCategoryName() >> 'Ciągniki rolnicze'
            }
            getBrand() >> ''
        }
        FarmEquipment farmEquipment2 = Mock(FarmEquipment) {
            getId() >> new FarmEquipmentId(2, 1)
            getEquipmentName() >> 'Lamborghini'
            getCategory() >> Mock(EquipmentCategory) {
                getCategoryName() >> 'Przyczepy'
            }
            getModel() >> 'Model 1'
        }
        List<ActivityHasEquipment> activityHasEquipmentList = [
                Mock(ActivityHasEquipment) {
                    getFarmEquipment() >> farmEquipment1
                },
                Mock(ActivityHasEquipment) {
                    getFarmEquipment() >> farmEquipment2
                }
        ]
        activityHasEquipmentRepository.findActivityHasEquipmentsByAgroActivity(agroActivity) >> activityHasEquipmentList

        when:
        List<EquipmentSummaryDTO> result = activityHasEquipmentService.getEquipmentsForAgroActivity(agroActivity)

        then:
        result.size() == 2
        result[0].getEquipmentId() == 1
        result[0].getEquipmentName() == 'Zetor'
        result[0].getCategory() == 'Ciągniki rolnicze'
        result[0].getBrand() == ''
        result[0].getModel() == null
        result[1].getEquipmentId() == 2
        result[1].getEquipmentName() == 'Lamborghini'
        result[1].getCategory() == 'Przyczepy'
        result[1].getBrand() == null
        result[1].getModel() == 'Model 1'
    }

    /*
    * updateEquipmentInActivity
    */

    def "should update equipment in activity"() {
        given:
        List<Integer> equipmentIds = [1, 2]
        AgroActivity agroActivity = Mock(AgroActivity)
        Integer farmId = 1

        FarmEquipment farmEquipment1 = Mock(FarmEquipment) {
            getId() >> new FarmEquipmentId(1, farmId)
            getIsAvailable() >> true
        }
        FarmEquipment farmEquipment2 = Mock(FarmEquipment) {
            getId() >> new FarmEquipmentId(2, farmId)
            getIsAvailable() >> true
        }
        List<FarmEquipment> farmEquipmentList = [farmEquipment1, farmEquipment2]
        farmEquipmentRepository.findAllById(_) >> farmEquipmentList

        applicationContext.getBean(ActivityHasEquipmentService.class) >> activityHasEquipmentService

        when:
        activityHasEquipmentService.updateEquipmentInActivity(equipmentIds, agroActivity, farmId)

        then:
        1 * activityHasEquipmentRepository.deleteActivityHasEquipmentsByAgroActivity(agroActivity)
        2 * activityHasEquipmentRepository.save(_)
    }
}