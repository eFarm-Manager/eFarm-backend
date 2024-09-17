package com.efarm.efarmbackend.repository.farm

import com.efarm.efarmbackend.model.farm.Farm
import spock.lang.Specification

class FarmRepositorySpec extends Specification{

    FarmRepository farmRepository = Mock(FarmRepository)

    def "should return true for existing farm - existsByFarmName" () {
        given:
        Farm farm = Mock(Farm)
        String farmName = "farmName"
        farm.getFarmName() >> farmName
        farmRepository.existsByFarmName(farmName) >> true

        when:
        Boolean existsByFarmName = farmRepository.existsByFarmName(farmName)

        then:
        existsByFarmName == true

    }

    def "should return false for non existing farm - existsByFarmName" () {
        given:
        String farmName = "farmName"
        farmRepository.existsByFarmName(farmName) >> false

        when:
        Boolean existsByFarmName = farmRepository.existsByFarmName(farmName)

        then:
        existsByFarmName == false

    }

    def "should return active farms" () {
        given:
        Farm farm1 = Mock(Farm)
        farm1.getIsActive() >> true
        farm1.getFarmName() >> "farm1"

        Farm farm2 = Mock(Farm)
        farm2.getIsActive() >> false
        farm2.getFarmName() >> "farm2"

        Farm farm3 = Mock(Farm)
        farm3.getIsActive() >> true
        farm3.getFarmName() >> "farm3"

        farmRepository.findByIsActiveTrue() >> [farm1,farm3]

        when:
        List<Farm> activeFarms = farmRepository.findByIsActiveTrue()

        then:
        activeFarms.size() == 2
        activeFarms.get(0).getFarmName() == "farm1"
        activeFarms.get(1).getFarmName() == "farm3"
        activeFarms.contains(farm1)
        activeFarms.contains(farm3)
        !activeFarms.contains(farm2)
        activeFarms.every { it.getIsActive() == true }
    }
    
}