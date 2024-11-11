package com.efarm.efarmbackend.repository.agroactivity;

import com.efarm.efarmbackend.model.agroactivity.ActivityHasOperator;
import com.efarm.efarmbackend.model.agroactivity.AgroActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityHasOperatorRepository extends JpaRepository<ActivityHasOperator, Integer> {

    List<ActivityHasOperator> findActivityHasOperatorsByAgroActivity(AgroActivity agroActivity);

    void deleteActivityHasOperatorsByAgroActivity(AgroActivity agroActivity);
}