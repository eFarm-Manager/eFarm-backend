package com.efarm.efarmbackend.service.user;

import com.efarm.efarmbackend.model.user.User;

import java.util.List;

public interface UserNotificationService {

    List<User> filterOperatorsForHelpNotifications(List<Integer> operatorIds, List<User> activeFarmOperators);

    List<User> filterInvalidOperatorsForHelpNotifications(List<Integer> operatorIds, List<User> activeFarmOperators);
}
