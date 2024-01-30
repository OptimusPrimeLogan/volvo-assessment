package congestion.calculator.repository;

import congestion.calculator.repository.entity.PublicHoliday;
import congestion.calculator.repository.entity.TollFeeChart;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class TollFeeChartRepositoryTest {

    @Autowired
    TollFeeChartRepository tollFeeChartRepository;

    @BeforeEach
    public void setUp() {
        tollFeeChartRepository.deleteAll();
        TollFeeChart tollFeeChart = TollFeeChart.builder().startTime(LocalTime.now()).endTime(LocalTime.now()).price(new BigDecimal(0)).build();
        tollFeeChartRepository.save(tollFeeChart);
    }

    @AfterEach
    public void destroy() {
        tollFeeChartRepository.deleteAll();
    }

    @Test
    void findByMonthYear() {
        assertEquals(1, tollFeeChartRepository.findAll().size());
    }
}