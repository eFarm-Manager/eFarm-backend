package com.efarm.efarmbackend.model.user;

import com.efarm.efarmbackend.model.agroactivity.ActivityHasOperator;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserSummaryDTO {
    private Integer id;
    private String firstName;
    private String lastName;
    private String role;

    public UserSummaryDTO(User user) {
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.role = user.getRole().getName().toString();
    }

    public UserSummaryDTO(ActivityHasOperator activityHasOperator) {
        this.id = activityHasOperator.getUser().getId();
        this.firstName = activityHasOperator.getUser().getFirstName();
        this.lastName = activityHasOperator.getUser().getLastName();
        this.role = activityHasOperator.getUser().getRole().getName().toString();
    }
}