package com.efarm.efarmbackend.service.agroactivity

import com.efarm.efarmbackend.model.agroactivity.ActivityCategory;
import com.efarm.efarmbackend.repository.agroactivity.ActivityCategoryRepository;

import java.util.List
import spock.lang.Subject
import spock.lang.Specification

class ActivityCategoryServiceSpec extends Specification {

    def activityCategoryRepository = Mock(ActivityCategoryRepository)

    @Subject
    ActivityCategoryService activityCategoryService = new ActivityCategoryService(
        activityCategoryRepository
    )

    def "should get all available category names"() {
        given:
        ActivityCategory category1 = Mock(ActivityCategory) {
            getName() >> "category1"
        }
        ActivityCategory category2 = Mock(ActivityCategory) {
            getName() >> "category2"
        }
        List<ActivityCategory> categories = [category1,category2]

        activityCategoryRepository.findAll() >> categories

        when:
        List<String> result = activityCategoryService.getAvailableCategoryNames()
        
        then:
        result == categories*.name
    }

}