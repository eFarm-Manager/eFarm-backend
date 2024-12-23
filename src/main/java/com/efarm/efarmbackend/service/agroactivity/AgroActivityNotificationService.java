package com.efarm.efarmbackend.service.agroactivity;

import com.efarm.efarmbackend.payload.request.agroactivity.NeededHelpRequest;

public interface AgroActivityNotificationService {
    void handleHelpRequest(NeededHelpRequest request) throws Exception;
}
