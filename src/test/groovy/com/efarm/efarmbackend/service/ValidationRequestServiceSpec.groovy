package com.efarm.efarmbackend.service

import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import spock.lang.Specification
import spock.lang.Subject

class ValidationRequestServiceSpec extends Specification {

    @Subject
    def validationRequestService = new ValidationRequestServiceImpl()

    def "test validateRequestWithException with errors"() {
        given:
        def bindingResult = Mock(BindingResult)
        bindingResult.hasErrors() >> true
        bindingResult.getFieldErrors() >> [
                new FieldError('objectName', 'field1', 'error1'),
                new FieldError('objectName', 'field2', 'error2')
        ]

        when:
        validationRequestService.validateRequest(bindingResult)

        then:
        def e = thrown(Exception)
        e.message == 'field1: error1, field2: error2'
    }

    def "test validateRequestWithException without errors"() {
        given:
        def bindingResult = Mock(BindingResult)
        bindingResult.hasErrors() >> false

        when:
        validationRequestService.validateRequest(bindingResult)

        then:
        noExceptionThrown()
    }

}
