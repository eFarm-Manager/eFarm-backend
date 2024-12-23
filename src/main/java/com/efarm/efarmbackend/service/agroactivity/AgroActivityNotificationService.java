package com.efarm.efarmbackend.service.agroactivity;

import com.efarm.efarmbackend.model.equipment.FarmEquipment;
import com.efarm.efarmbackend.model.landparcel.Landparcel;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.payload.request.agroactivity.NeededHelpRequest;

import java.util.List;

public interface AgroActivityNotificationService {
    void handleHelpRequest(NeededHelpRequest request) throws Exception;

    default String buildHelpRequestEmailMessage(NeededHelpRequest request, Landparcel landParcel, List<FarmEquipment> equipmentList, User sender) {
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
