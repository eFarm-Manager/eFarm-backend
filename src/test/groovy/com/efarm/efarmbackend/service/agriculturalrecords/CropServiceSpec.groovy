package com.efarm.efarmbackend.service.agriculturalrecords

import com.efarm.efarmbackend.model.agriculturalrecords.Crop
import com.efarm.efarmbackend.repository.agriculturalrecords.CropRepository
import spock.lang.Specification
import spock.lang.Subject

class CropServiceSpec extends Specification {

    def cropRepository = Mock(CropRepository)

    @Subject
    CropService cropService = new CropService(
            cropRepository
    )

    def "should return all available crops"() {
        given:
        List<Crop> crops = [
                new Crop(name: 'Crop1'),
                new Crop(name: 'Crop2')
        ]
        cropRepository.findAll() >> crops

        when:
        List<String> result = cropService.getAvailableCropNames()

        then:
        result == ['Crop1', 'Crop2']
    }

}
