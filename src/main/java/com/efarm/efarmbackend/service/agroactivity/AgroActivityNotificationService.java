package com.efarm.efarmbackend.service.agroactivity;

import com.efarm.efarmbackend.model.equipment.FarmEquipment;
import com.efarm.efarmbackend.model.landparcel.Landparcel;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.payload.request.agroactivity.NeededHelpRequest;
import com.efarm.efarmbackend.repository.user.UserRepository;
import com.efarm.efarmbackend.service.MainNotificationService;
import com.efarm.efarmbackend.service.equipment.FarmEquipmentService;
import com.efarm.efarmbackend.service.landparcel.LandparcelService;
import com.efarm.efarmbackend.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AgroActivityNotificationService {

    private final UserService userService;
    private final LandparcelService landparcelService;
    private final FarmEquipmentService farmEquipmentService;
    private final UserRepository userRepository;
    private final MainNotificationService mainNotificationService;

    public void handleHelpRequest(NeededHelpRequest request) throws Exception {
        User loggedUser = userService.getLoggedUser();
        Landparcel landparcel = landparcelService.findlandparcelByFarm(request.getLandparcelId(), loggedUser.getFarm());
        List<FarmEquipment> equipmentList = farmEquipmentService.getEquipmentByIds(request.getEquipmentIds(), loggedUser.getFarm());
        List<User> activeFarmOperators = userRepository.findByFarmIdAndIsActive(loggedUser.getFarm().getId(), true);
        List<User> selectedOperators = userService.filterOperatorsForHelpNotifications(request.getOperatorIds(), activeFarmOperators);
        List<User> excludedOperators = userService.filterInvalidOperatorsForHelpNotifications(request.getOperatorIds(), selectedOperators);

        if (!excludedOperators.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder("Nie udało się wysłać prośby o pomoc: ");
            for (User user : excludedOperators) {
                if (user.getFarm().equals(loggedUser.getFarm())) {
                    errorMessage.append("- ").append(user.getFirstName()).append(" ").append(user.getLastName()).append(" - użytkownik jest nieaktywny");
                } else {
                    errorMessage.append("- Nie znaleziono użytkownika o id: ").append(user.getId());
                }
            }
            throw new RuntimeException(errorMessage.toString());
        }

        String subject = "Pracownik " + loggedUser.getFirstName() + " " + loggedUser.getLastName() + " potrzebuje Twojej pomocy";
        String message = buildHelpRequestEmailMessage(request, landparcel, equipmentList, loggedUser);
        for (User operator : selectedOperators) {
            mainNotificationService.sendNotificationToUser(operator, message, subject);
        }
    }

    private String buildHelpRequestEmailMessage(NeededHelpRequest request, Landparcel landParcel, List<FarmEquipment> equipmentList, User sender) {
        StringBuilder content = new StringBuilder();

        content.append("Pracownik ")
                .append(sender.getFirstName()).append(" ").append(sender.getLastName())
                .append(" potrzebuje Twojej pomocy: \n").append(request.getName())
                .append("\n\n");

        content.append("Opis: \n").append(request.getDescription()).append("\n\n");

        content.append("Działka: \n");
        content.append(" - Nazwa: ").append(landParcel.getName()).append("\n");
        content.append(" - Współrzędne: ")
                .append("Lat: ").append(landParcel.getLatitude())
                .append(", Long: ").append(landParcel.getLongitude())
                .append("\n\n");

        content.append("Wymagane maszyny: \n");
        for (FarmEquipment equipment : equipmentList) {
            content.append(" - ").append(equipment.getCategory().getCategoryName())
                    .append(": ").append(equipment.getEquipmentName()).append("\n");
        }
        return content.toString();
    }
}