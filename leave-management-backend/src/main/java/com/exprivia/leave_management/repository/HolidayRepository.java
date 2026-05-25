package com.exprivia.leave_management.repository;

import com.exprivia.leave_management.entity.Holiday;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {
   Optional<Holiday> findByDate(LocalDate date);
}
