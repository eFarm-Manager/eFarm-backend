package com.efarm.efarmbackend.service;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import spock.lang.Specification
import spock.lang.Subject
import org.springframework.validation.FieldError
import org.springframework.http.HttpStatus

class ValidationRequestServiceSpec extends Specification {
    @Subject
    ValidationRequestService validationRequestService = new ValidationRequestService()

    def "should return bad request with error messages when there are validation errors"() {
        given:
        BindingResult bindingResult = Mock(BindingResult) {
            hasErrors() >> true
            getFieldErrors() >> [Mock(FieldError) {
                getField() >> "username"
                getDefaultMessage() >> "Username cannot be empty"
            }]
        }

        when:
        ResponseEntity<?> response = validationRequestService.validateRequest(bindingResult)

        then:
        response.getStatusCode() == HttpStatus.BAD_REQUEST
    }

    def "should return null when there are no validation errors"() {
        given:
        BindingResult bindingResult = Mock(BindingResult) {
            hasErrors() >> false
        }

        when:
        ResponseEntity<?> response = validationRequestService.validateRequest(bindingResult)

        then:
        response == null
    }

}