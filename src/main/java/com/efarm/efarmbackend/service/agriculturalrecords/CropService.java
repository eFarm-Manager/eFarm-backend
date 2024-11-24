package com.efarm.efarmbackend.service.agriculturalrecords;

import com.efarm.efarmbackend.model.agriculturalrecords.Crop;
import com.efarm.efarmbackend.repository.agriculturalrecords.CropRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CropService {

    private final CropRepository cropRepository;

    public List<String> getAvailableCropNames() {
        return cropRepository.findAll().stream()
                .map(Crop::getName)
                .collect(Collectors.toList());
    }
}
