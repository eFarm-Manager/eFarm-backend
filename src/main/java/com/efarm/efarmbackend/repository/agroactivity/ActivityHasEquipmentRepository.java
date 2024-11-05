package com.efarm.efarmbackend.repository.agroactivity;

import com.efarm.efarmbackend.model.agroactivity.ActivityHasEquipment;
import com.efarm.efarmbackend.model.agroactivity.AgroActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityHasEquipmentRepository extends JpaRepository<ActivityHasEquipment, Integer> {

    List<ActivityHasEquipment> findActivityHasEquipmentsByAgroActivity(AgroActivity agroActivity);

    void deleteActivityHasEquipmentsByAgroActivity(AgroActivity agroActivity);
}
