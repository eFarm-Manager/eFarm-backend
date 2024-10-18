package com.efarm.efarmbackend.service

import org.springframework.validation.BindingResult
import spock.lang.Specification
import spock.lang.Subject
import org.springframework.validation.FieldError

class ValidationRequestServiceSpec extends Specification {

        @Subject
    def validationRequestService = new ValidationRequestService()

    def "test validateRequestWithException with errors"() {
        given:
        def bindingResult = Mock(BindingResult)
        bindingResult.hasErrors() >> true
        bindingResult.getFieldErrors() >> [
            new FieldError('objectName', 'field1', 'error1'),
            new FieldError('objectName', 'field2', 'error2')
        ]

        when:
        validationRequestService.validateRequestWithException(bindingResult)

        then:
        def e = thrown(Exception)
        e.message == 'field1: error1, field2: error2'
    }

    def "test validateRequestWithException without errors"() {
        given:
        def bindingResult = Mock(BindingResult)
        bindingResult.hasErrors() >> false

        when:
        validationRequestService.validateRequestWithException(bindingResult)

        then:
        noExceptionThrown()
    }

}
