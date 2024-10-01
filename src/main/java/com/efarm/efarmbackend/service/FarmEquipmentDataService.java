package com.efarm.efarmbackend.service;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class FarmEquipmentDataService {

    public List<String> getFieldsForCategory(String categoryName) {
        switch (categoryName) {
                //Tractors
            case "Ciągniki rolnicze":
            case "Ciągniki sadownicze":
            case "Harwestery":
            case "Forwardery":
            case "Ładowarki teleskopowe":
            case "Ładowarki kołowe":
            case "Ładowarki burtowe":
                return Arrays.asList("power", "insurancePolicyNumber", "insuranceExpirationDate", "inspectionExpireDate");

                //Trailers
            case "Przyczepy rolnicze":
            case "Przyczepy do transportu bel":
            case "Przyczepy do transportu drewna":
            case "Przyczepy do transportu zwierząt":
            case "Przyczepy samozbierające":
            case "Przyczepy pojemnościowe":
            case "Naczepy":
            case "Wozy asenizacyjne":
            case "Wozy paszowe":
                return Arrays.asList("capacity", "insurancePolicyNumber", "insuranceExpirationDate", "inspectionExpireDate");

                //Harvesters
            case "Kombajny zbożowe":
            case "Kombajny do ziemniaków":
            case "Kombajny do buraków cukrowych":
            case "Kombajny do owoców":
            case "Silosokombajny":
            case "Kombajny inne":
                return Arrays.asList("power", "capacity", "workingWidth", "insurancePolicyNumber", "insuranceExpirationDate", "inspectionExpireDate");

                //Agricultural machinery
            case "Agregaty ścierniskowe (Grubery)":
            case "Brony mechaniczne":
            case "Brony talerzowe":
            case "Rębaki":
            case "Kosiarki":
            case "Glebogryzarki":
            case "Głębosze":
            case "Mulczery":
            case "Maszyny do uprawy winorośli":
            case "Wały uprawowe":
            case "Zgrabiarki":
            case "Ładowacze czołowe":
                return Arrays.asList("workingWidth");

                //Sprayers, spreaders etc.
            case "Opryskiwacze polowe":
            case "Opryskiwacze sadownicze":
            case "Opryskiwacze samojezdne":
            case "Drony do opryskiwania":
            case "Rozsiewacze nawozów":
            case "Rozsiewacze wapna":
            case "Siewniki punktowe":
            case "Siewniki zbożowe":
            case "Zbieracze kamieni":
                return Arrays.asList("capacity", "workingWidth");

            default:
                return List.of();
        }
    }
}
