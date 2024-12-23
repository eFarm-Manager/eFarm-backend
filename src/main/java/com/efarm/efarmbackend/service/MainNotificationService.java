package com.efarm.efarmbackend.service;

import com.efarm.efarmbackend.model.user.User;

public interface MainNotificationService {
    void sendNotificationToUser(User user, String message, String subject);
}
