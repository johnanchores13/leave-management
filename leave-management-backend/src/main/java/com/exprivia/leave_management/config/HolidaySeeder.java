package com.exprivia.leave_management.config;

import com.exprivia.leave_management.entity.Holiday;
import com.exprivia.leave_management.repository.HolidayRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HolidaySeeder {
   @Bean
   CommandLineRunner initHolidays(HolidayRepository holidayRepository) {
      return (args) -> {
         if (holidayRepository.count() == 0L) {
            holidayRepository.saveAll(List.of(new Holiday(LocalDate.of(2026, 1, 1), "Capodanno"), new Holiday(LocalDate.of(2026, 1, 6), "Epifania"), new Holiday(LocalDate.of(2026, 4, 6), "Pasquetta"), new Holiday(LocalDate.of(2026, 4, 25), "Festa della Liberazione"), new Holiday(LocalDate.of(2026, 5, 1), "Festa dei Lavoratori"), new Holiday(LocalDate.of(2026, 6, 2), "Festa della Repubblica"), new Holiday(LocalDate.of(2026, 8, 15), "Ferragosto"), new Holiday(LocalDate.of(2026, 11, 1), "Tutti i Santi"), new Holiday(LocalDate.of(2026, 12, 8), "Immacolata"), new Holiday(LocalDate.of(2026, 12, 25), "Natale"), new Holiday(LocalDate.of(2026, 12, 26), "Santo Stefano")));
         }

      };
   }
}
