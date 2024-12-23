package com.efarm.efarmbackend.service.equipment;

import com.efarm.efarmbackend.model.equipment.EquipmentCategoryDTO;
import com.efarm.efarmbackend.repository.equipment.EquipmentCategoryRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class EquipmentDisplayDataServiceImpl implements EquipmentDisplayDataService {

    private final EquipmentCategoryRepository equipmentCategoryRepository;

    private final Map<String, List<String>> categoryFieldsCache = new HashMap<>();
    private final List<EquipmentCategoryDTO> cachedCategoryList = new ArrayList<>();

    @Override
    @PostConstruct
    public void initializeCache() {
        List<String> categories = equipmentCategoryRepository.findAllCategoryNames();
        List<String> commonFields = Arrays.asList("equipmentName", "category", "brand", "model");

        for (String categoryName : categories) {
            List<String> fields = new ArrayList<>(commonFields);
            fields.addAll(getFieldsForCategory(categoryName));
            categoryFieldsCache.put(categoryName, fields);
        }
        cachedCategoryList.addAll(
                categories.stream()
                        .map(categoryName -> new EquipmentCategoryDTO(
                                categoryName,
                                categoryFieldsCache.get(categoryName))
                        ).toList()
        );
    }

    @Override
    public List<String> getFieldsForCategory(String categoryName) {
        return switch (categoryName) {
            case "Ciągniki rolnicze",
                 "Ciągniki sadownicze",
                 "Harwestery",
                 "Forwardery",
                 "Ładowarki teleskopowe",
                 "Ładowarki kołowe",
                 "Ładowarki burtowe" ->
                    Arrays.asList("power", "insurancePolicyNumber", "insuranceExpirationDate", "inspectionExpireDate");

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

            case "Kombajny zbożowe",
                 "Kombajny do ziemniaków",
                 "Kombajny do buraków cukrowych",
                 "Kombajny do owoców",
                 "Silosokombajny",
                 "Kombajny inne" ->
                    Arrays.asList("power", "capacity", "workingWidth", "insurancePolicyNumber", "insuranceExpirationDate", "inspectionExpireDate");

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
                 "Ładowacze czołowe" -> List.of("workingWidth");

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

    @Override
    public List<EquipmentCategoryDTO> getAllCategoriesWithFields() {
        return new ArrayList<>(cachedCategoryList);
    }
}