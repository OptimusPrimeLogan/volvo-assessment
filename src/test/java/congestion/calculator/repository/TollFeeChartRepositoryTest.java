package congestion.calculator.repository;

import congestion.calculator.repository.entity.PublicHoliday;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class TollFeeChartRepositoryTest {

    @Autowired
    PublicHolidaysRepository publicHolidaysRepository;

    @BeforeEach
    public void setUp() {
        publicHolidaysRepository.deleteAll();
        PublicHoliday publicHoliday = PublicHoliday.builder().dateMonth(1).monthYear(1).build();
        publicHolidaysRepository.save(publicHoliday);
    }

    @AfterEach
    public void destroy() {
        publicHolidaysRepository.deleteAll();
    }

    @Test
    void findByMonthYear() {
        assertEquals(1, publicHolidaysRepository.findAll().size());
        assertEquals(1, publicHolidaysRepository.findByMonthYear(1).size());
    }
}