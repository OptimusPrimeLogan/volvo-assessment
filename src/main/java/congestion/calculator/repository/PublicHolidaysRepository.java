package congestion.calculator.repository;

import congestion.calculator.repository.entity.PublicHoliday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PublicHolidaysRepository extends JpaRepository<PublicHoliday, Long> {
    List<PublicHoliday> findByMonthYear(Integer monthYear);
}
