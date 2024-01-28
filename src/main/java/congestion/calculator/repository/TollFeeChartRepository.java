package congestion.calculator.repository;

import congestion.calculator.repository.entity.TollFeeChart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TollFeeChartRepository extends JpaRepository<TollFeeChart, Long> {
}
