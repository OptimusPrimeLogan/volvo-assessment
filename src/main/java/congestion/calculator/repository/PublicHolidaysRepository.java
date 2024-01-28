package congestion.calculator.repository;

import congestion.calculator.repository.entity.PublicHolidays;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PublicHolidaysRepository extends JpaRepository<PublicHolidays, Long> {
    List<PublicHolidays> findByMonthYear(Integer monthYear);
}
