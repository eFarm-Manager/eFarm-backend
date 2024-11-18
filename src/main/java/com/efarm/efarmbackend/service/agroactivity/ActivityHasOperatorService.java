package com.efarm.efarmbackend.service.agroactivity;

import com.efarm.efarmbackend.model.agroactivity.ActivityHasOperator;
import com.efarm.efarmbackend.model.agroactivity.AgroActivity;
import com.efarm.efarmbackend.model.user.User;
import com.efarm.efarmbackend.model.user.UserSummaryDTO;
import com.efarm.efarmbackend.repository.agroactivity.ActivityHasOperatorRepository;
import com.efarm.efarmbackend.repository.user.UserRepository;
import com.efarm.efarmbackend.service.user.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ActivityHasOperatorService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ActivityHasOperatorRepository activityHasOperatorRepository;

    @Transactional
    public void addOperatorsToActivity(AgroActivity agroActivity, List<Integer> operatorIds, Integer loggedUserFarmId) {
        List<User> operators;
        if (operatorIds != null && !operatorIds.isEmpty()) {
            operators = operatorIds.stream()
                    .map(Long::valueOf)
                    .map(userId -> userRepository.findById(userId)
                            .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono użytkownika o ID: " + userId)))
                    .peek(user -> {
                        if (!user.getIsActive()) {
                            throw new IllegalStateException("Użytkownik " + user.getFirstName() + " " + user.getLastName() + " jest niedostępny");
                        }
                        if (!user.getFarm().getId().equals(loggedUserFarmId)) {
                            throw new IllegalStateException("Użytkownik o ID: " + user.getId() + " nie należy do tej farmy");
                        }
                    })
                    .collect(Collectors.toList());
        } else {
            operators = List.of(userService.getLoggedUser());
        }

        for (User operator : operators) {
            ActivityHasOperator activityHasOperator = new ActivityHasOperator();
            activityHasOperator.setAgroActivity(agroActivity);
            activityHasOperator.setUser(operator);
            activityHasOperator.setFarmId(loggedUserFarmId);
            activityHasOperatorRepository.save(activityHasOperator);
        }
    }

    public List<UserSummaryDTO> getOperatorsForAgroActivity(AgroActivity agroActivity) {
        return activityHasOperatorRepository.findActivityHasOperatorsByAgroActivity(agroActivity).stream()
                .map(UserSummaryDTO::new)
                .collect(Collectors.toList());
    }

    public void updateOperatorInActivity(List<Integer> operatorsIds, AgroActivity agroActivity, Integer loggedUserFarmId) {
        activityHasOperatorRepository.deleteActivityHasOperatorsByAgroActivity(agroActivity);
        ActivityHasOperatorService self = applicationContext.getBean(ActivityHasOperatorService.class);
        self.addOperatorsToActivity(agroActivity, operatorsIds, loggedUserFarmId);
    }
}