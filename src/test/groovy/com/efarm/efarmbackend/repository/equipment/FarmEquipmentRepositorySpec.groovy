package com.efarm.efarmbackend.repository.equipment

import com.efarm.efarmbackend.model.equipment.FarmEquipment;
import com.efarm.efarmbackend.model.equipment.FarmEquipmentId;
import spock.lang.Specification

class FarmEquipmentRepositorySpec extends Specification {

    FarmEquipmentRepository farmEquipmentRepository = Mock(FarmEquipmentRepository)
    def "should return list of farm equipment by farm id"() {
        given: 
        Integer farmId = 123
        FarmEquipmentId equipmentId1 = Mock(FarmEquipmentId)
        FarmEquipmentId equipmentId2 = Mock(FarmEquipmentId)
        FarmEquipmentId equipmentId3 = Mock(FarmEquipmentId)
        equipmentId1.getFarmId() >> farmId  
        equipmentId2.getFarmId() >> farmId
        equipmentId3.getFarmId() >> farmId + 1
        FarmEquipment equipment1 = Mock(FarmEquipment)
        FarmEquipment equipment2 = Mock(FarmEquipment)
        FarmEquipment equipment3 = Mock(FarmEquipment)
        equipment1.id >> equipmentId1
        equipment2.id >> equipmentId2
        equipment3.id >> equipmentId3
        equipment1.equipmentName >> "Tractor"
        equipment2.equipmentName >> "Harvester"
        equipment3.equipmentName >> "Combine"

        farmEquipmentRepository.findByFarmIdFarm_Id(farmId) >> [equipment1, equipment2]

        when: 
        List<FarmEquipment> result = farmEquipmentRepository.findByFarmIdFarm_Id(farmId)

        then: 
        result.size() == 2
        result.contains(equipment1)
        result.contains(equipment2)
        !result.contains(equipment3)
        result.equipmentName == ["Tractor", "Harvester"]
    }
}