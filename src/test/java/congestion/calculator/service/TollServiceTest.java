package congestion.calculator.service;

import congestion.calculator.repository.PublicHolidaysRepository;
import congestion.calculator.repository.TollFeeChartRepository;
import congestion.calculator.repository.entity.PublicHoliday;
import congestion.calculator.repository.entity.TollFeeChart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.model.TollRequestPostTo;
import org.openapitools.model.TollResponseTo;
import org.openapitools.model.VehicleTypeEnum;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@ExtendWith(MockitoExtension.class)
class TollServiceTest {

    private TollService underTest;

    @Mock
    PublicHolidaysRepository publicHolidaysRepository;

    @Mock
    TollFeeChartRepository tollFeeChartRepository;

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.of("UTC"));

    @BeforeEach
    void setup(){
        openMocks(this);
        underTest = new TollService(publicHolidaysRepository, tollFeeChartRepository);
    }

    @Test
    void getTax() {

        //Given
        TollRequestPostTo tollRequestPostTo = new TollRequestPostTo("LP", VehicleTypeEnum.GENERAL);
        tollRequestPostTo.addProcessTimesItem(
                OffsetDateTime.of(
                        LocalDate.parse("2013-01-14"),
                        LocalTime.parse("16:00:00"),
                        ZoneOffset.UTC
                )
        );

        PublicHoliday publicHoliday = new PublicHoliday(1L, 1, 1);
        when(publicHolidaysRepository.findByMonthYear(anyInt())).thenReturn(List.of(publicHoliday));

        TollFeeChart tollFeeChart = new TollFeeChart(1L, LocalTime.of(0, 0),
                LocalTime.of(23, 59, 59), new BigDecimal(20));
        when(tollFeeChartRepository.findAll()).thenReturn(List.of(tollFeeChart));

        //when
        TollResponseTo responseTo = underTest.getTax(tollRequestPostTo);

        //then
        assertNotNull(responseTo);
        assertEquals(20, tollFeeChart.getPrice().intValue());


    }

}