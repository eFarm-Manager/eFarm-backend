package com.efarm.efarmbackend.service.equipment;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class EquipmentDisplayDataService {

    public List<String> getFieldsForCategory(String categoryName) {
        return switch (categoryName) {
            //Tractors
            case "Ciągniki rolnicze",
                 "Ciągniki sadownicze",
                 "Harwestery",
                 "Forwardery",
                 "Ładowarki teleskopowe",
                 "Ładowarki kołowe",
                 "Ładowarki burtowe" ->
                    Arrays.asList("power", "insurancePolicyNumber", "insuranceExpirationDate", "inspectionExpireDate");

            //Trailers
            case "Przyczepy rolnicze",
                 "Przyczepy do transportu bel",
                 "Przyczepy do transportu drewna",
                 "Przyczepy do transportu zwierząt",
                 "Przyczepy samozbierające",
                 "Przyczepy pojemnościowe",
                 "Naczepy",
                 "Wozy asenizacyjne",
                 "Wozy paszowe" ->
                    Arrays.asList("capacity", "insurancePolicyNumber", "insuranceExpirationDate", "inspectionExpireDate");

            //Harvesters
            case "Kombajny zbożowe",
                 "Kombajny do ziemniaków",
                 "Kombajny do buraków cukrowych",
                 "Kombajny do owoców",
                 "Silosokombajny",
                 "Kombajny inne" ->
                    Arrays.asList("power", "capacity", "workingWidth", "insurancePolicyNumber", "insuranceExpirationDate", "inspectionExpireDate");

            //Agricultural machinery
            case "Agregaty ścierniskowe (Grubery)",
                 "Brony mechaniczne",
                 "Brony talerzowe",
                 "Rębaki",
                 "Kosiarki",
                 "Glebogryzarki",
                 "Głębosze",
                 "Mulczery",
                 "Maszyny do uprawy winorośli",
                 "Wały uprawowe",
                 "Zgrabiarki",
                 "Ładowacze czołowe" -> Arrays.asList("workingWidth");

            //Sprayers, spreaders etc.
            case "Opryskiwacze polowe",
                 "Opryskiwacze sadownicze",
                 "Opryskiwacze samojezdne",
                 "Drony do opryskiwania",
                 "Rozsiewacze nawozów",
                 "Rozsiewacze wapna",
                 "Siewniki punktowe",
                 "Siewniki zbożowe",
                 "Zbieracze kamieni" -> Arrays.asList("capacity", "workingWidth");
            default -> List.of();
        };
    }
}
