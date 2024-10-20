package com.efarm.efarmbackend.controller;

import com.efarm.efarmbackend.payload.request.farmfield.MergeLandparcelsRequest;
import com.efarm.efarmbackend.payload.response.MessageResponse;
import com.efarm.efarmbackend.service.ValidationRequestService;
import com.efarm.efarmbackend.service.farmfield.FarmfieldFacade;
import com.efarm.efarmbackend.service.farmfield.FarmfieldService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/farmfield")
public class FarmfieldController {

    @Autowired
    private ValidationRequestService validationRequestService;

    @Autowired
    private FarmfieldFacade farmfieldFacade;

    @PostMapping("/merge")
    @PreAuthorize("hasRole('ROLE_FARM_OWNER') or hasRole('ROLE_FARM_MANAGER')")
    public ResponseEntity<MessageResponse> mergeFarmfields(@Valid @RequestBody MergeLandparcelsRequest mergeLandparcelsRequest, BindingResult bindingResult) {
        try {
            //TODO rozważyć przypadek, w którym na scalonym polu chcemy zrobić kilka upraw
            validationRequestService.validateRequestWithException(bindingResult);
            farmfieldFacade.mergeLandparcels(mergeLandparcelsRequest);
            return ResponseEntity.ok(new MessageResponse("Pola zostały pomyślnie scalone"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

//    @PostMapping("/split")
//    public ResponseEntity<MessageResponse> splitFarmfields(@Valid @RequestBody SplitFarmfieldRequest splitFarmfieldsRequest, BindingResult bindingResult) {
//        try {
//            validationRequestService.validateRequestWithException(bindingResult);
//            farmfieldFacade.splitFarmfields(splitFarmfieldsRequest);
//            return ResponseEntity.ok(new MessageResponse("Pomyślnie rozdzielono pole"));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
//        }
//    }
}
