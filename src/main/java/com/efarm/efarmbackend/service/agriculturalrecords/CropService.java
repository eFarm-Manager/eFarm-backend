package com.efarm.efarmbackend.service.agriculturalrecords;

import com.efarm.efarmbackend.model.agriculturalrecords.Crop;
import com.efarm.efarmbackend.repository.agriculturalrecords.CropRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CropService {

    @Autowired
    private CropRepository cropRepository;

    public List<String> getAvailableCropNames() {
        return cropRepository.findAll().stream()
                .map(Crop::getName)
                .collect(Collectors.toList());
    }
}
