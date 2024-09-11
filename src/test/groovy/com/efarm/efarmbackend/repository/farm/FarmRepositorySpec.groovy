package com.efarm.efarmbackend.repository.farm

import com.efarm.efarmbackend.model.farm.Farm
import spock.lang.Specification

class FarmRepositorySpec extends Specification{

    def "should return true for existing farm - existsByFarmName" () {
        given:
        FarmRepository farmRepository = Mock(FarmRepository)
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
        FarmRepository farmRepository = Mock(FarmRepository)
        String farmName = "farmName"
        farmRepository.existsByFarmName(farmName) >> false

        when:
        Boolean existsByFarmName = farmRepository.existsByFarmName(farmName)

        then:
        existsByFarmName == false

    }
    
}