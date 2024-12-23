package com.efarm.efarmbackend.controller;

import com.efarm.efarmbackend.model.agroactivity.AgroActivityDetailDTO;
import com.efarm.efarmbackend.model.agroactivity.AgroActivitySummaryDTO;
import com.efarm.efarmbackend.payload.request.agroactivity.NeededHelpRequest;
import com.efarm.efarmbackend.payload.request.agroactivity.NewAgroActivityRequest;
import com.efarm.efarmbackend.payload.request.agroactivity.UpdateAgroActivityRequest;
import com.efarm.efarmbackend.payload.response.MessageResponse;
import com.efarm.efarmbackend.service.ValidationRequestService;
import com.efarm.efarmbackend.service.agroactivity.ActivityCategoryService;
import com.efarm.efarmbackend.service.agroactivity.AgroActivityFacade;
import com.efarm.efarmbackend.service.agroactivity.AgroActivityNotificationService;
import com.efarm.efarmbackend.service.agroactivity.AgroActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@RequestMapping("/agro-activities")
public class AgroActivityController {

    private final AgroActivityFacade agroActivityFacade;
    private final ValidationRequestService validationRequestService;
    private final AgroActivityService agroActivityService;
    private final ActivityCategoryService activityCategoryService;
    private final AgroActivityNotificationService agroActivityNotificationService;

    @PostMapping("/new")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<MessageResponse> addAgroActivity(@RequestBody @Valid NewAgroActivityRequest request, BindingResult bindingResult) {
        try {
            validationRequestService.validateRequest(bindingResult);
            agroActivityFacade.addAgroActivity(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Pomyślnie dodano nowy zabieg agrotechniczny"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/{agriculturalRecordId}")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<?> getAgroActivitiesByRecord(@PathVariable Integer agriculturalRecordId) {
        try {
            List<AgroActivitySummaryDTO> summaries = agroActivityService.getAgroActivitiesByAgriculturalRecord(agriculturalRecordId);
            return ResponseEntity.ok(summaries);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/details/{agroActivityId}")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')or hasRole('ROLE_FARM_EQUIPMENT_OPERATOR')")
    public ResponseEntity<?> getAgroActivityDetails(@PathVariable Integer agroActivityId) {
        try {
            AgroActivityDetailDTO detail = agroActivityFacade.getAgroActivityDetails(agroActivityId);
            return ResponseEntity.ok(detail);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(e.getMessage()));
        }
    }

    @PutMapping("/{agroActivityId}")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<MessageResponse> updateAgroActivity(@PathVariable Integer agroActivityId, @RequestBody @Valid UpdateAgroActivityRequest request, BindingResult bindingResult) {
        try {
            validationRequestService.validateRequest(bindingResult);
            agroActivityFacade.updateAgroActivity(agroActivityId, request);
            return ResponseEntity.ok(new MessageResponse("Pomyślnie zaktualizowano zabieg agrotechniczny"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{agroActivityId}")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<MessageResponse> deleteAgroActivity(@PathVariable Integer agroActivityId) {
        try {
            agroActivityFacade.deleteAgroActivity(agroActivityId);
            return ResponseEntity.ok(new MessageResponse("Pomyślnie usunięto zabieg agrotechniczny"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/assigned")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER') or hasRole('ROLE_FARM_EQUIPMENT_OPERATOR')")
    public ResponseEntity<List<AgroActivitySummaryDTO>> getAssignedIncompleteAgroActivities() {
        List<AgroActivitySummaryDTO> activities = agroActivityService.getAssignedIncompleteActivitiesForLoggedUser();
        return ResponseEntity.ok(activities);
    }

    @PatchMapping("/complete/{activityId}")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER') or hasRole('ROLE_FARM_EQUIPMENT_OPERATOR')")
    public ResponseEntity<MessageResponse> completeAgroActivity(@PathVariable Integer activityId) {
        try {
            agroActivityService.markActivityAsCompleted(activityId);
            return ResponseEntity.ok(new MessageResponse("Zadanie zostało oznaczone jako zakończone"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/needed-help")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER') or hasRole('ROLE_FARM_EQUIPMENT_OPERATOR')")
    public ResponseEntity<MessageResponse> requestHelp(@RequestBody NeededHelpRequest neededHelpRequest) {
        try {
            agroActivityNotificationService.handleHelpRequest(neededHelpRequest);
            return ResponseEntity.ok(new MessageResponse("Prośba o pomoc na polu została wysłana"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse(e.getMessage()));
        }
    }


    @GetMapping("/available-category")
    @PreAuthorize("hasRole('ROLE_FARM_MANAGER') or hasRole('ROLE_FARM_OWNER')")
    public ResponseEntity<List<String>> getAvailableActivityCategory() {
        List<String> categoryNames = activityCategoryService.getAvailableCategoryNames();
        return ResponseEntity.ok(categoryNames);
    }
}