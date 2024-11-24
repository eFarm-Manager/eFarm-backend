package com.efarm.efarmbackend.service.agroactivity;

import com.efarm.efarmbackend.model.agroactivity.ActivityCategory;
import com.efarm.efarmbackend.repository.agroactivity.ActivityCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ActivityCategoryService {

    private final ActivityCategoryRepository activityCategoryRepository;

    public List<String> getAvailableCategoryNames() {
        return activityCategoryRepository.findAll().stream()
                .map(ActivityCategory::getName)
                .collect(Collectors.toList());
    }
}
