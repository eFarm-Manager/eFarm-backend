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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AgroActivityNotificationServiceImpl implements AgroActivityNotificationService {

    private final UserService userService;
    private final LandparcelService landparcelService;
    private final FarmEquipmentService farmEquipmentService;
    private final UserRepository userRepository;
    private final MainNotificationService mainNotificationService;

    @Override
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

}