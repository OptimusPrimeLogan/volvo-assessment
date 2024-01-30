package congestion.calculator.service;

import congestion.calculator.exception.TollException;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @BeforeEach
    void setup(){
        openMocks(this);
        underTest = new TollService(List.of("BUS", "DIPLOMAT"), 60L,
                2013, publicHolidaysRepository, tollFeeChartRepository);
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

        PublicHoliday publicHoliday = PublicHoliday.builder().dateMonth(1).monthYear(1).build();
        when(publicHolidaysRepository.findByMonthYear(anyInt())).thenReturn(List.of(publicHoliday));

        LocalTime from = LocalTime.of(0, 0);
        LocalTime to = LocalTime.of(23, 59, 59);
        BigDecimal price = new BigDecimal(20);
        TollFeeChart tollFeeChart = TollFeeChart.builder().startTime(from).endTime(to).price(price).build();
        when(tollFeeChartRepository.findAll()).thenReturn(List.of(tollFeeChart));

        //when
        TollResponseTo responseTo = underTest.getTax(tollRequestPostTo);

        //then
        assertNotNull(responseTo);
        assertEquals(20, responseTo.getTotalAmount().intValue());
    }

    @Test
    void getTax_ForTollFreeVehicle() {

        //Given
        TollRequestPostTo tollRequestPostTo = new TollRequestPostTo("LP", VehicleTypeEnum.DIPLOMAT);
        tollRequestPostTo.addProcessTimesItem(
                OffsetDateTime.of(
                        LocalDate.parse("2013-01-14"),
                        LocalTime.parse("16:00:00"),
                        ZoneOffset.UTC
                )
        );

        //when
        TollResponseTo responseTo = underTest.getTax(tollRequestPostTo);

        //then
        assertNotNull(responseTo);
        assertEquals(0, responseTo.getTotalAmount().intValue());
    }

    @Test
    void getTax_ForTollFreeDate_DayBeforePublicHoliday() {

        //Given
        TollRequestPostTo tollRequestPostTo = new TollRequestPostTo("LP", VehicleTypeEnum.GENERAL);
        tollRequestPostTo.addProcessTimesItem(
                OffsetDateTime.of(
                        LocalDate.parse("2013-01-08"),
                        LocalTime.parse("16:00:00"),
                        ZoneOffset.UTC
                )
        );

        PublicHoliday publicHoliday = PublicHoliday.builder().dateMonth(9).monthYear(1).build();
        when(publicHolidaysRepository.findByMonthYear(anyInt())).thenReturn(List.of(publicHoliday));

        //when
        TollResponseTo responseTo = underTest.getTax(tollRequestPostTo);

        //then
        assertNotNull(responseTo);
        assertEquals(0, responseTo.getTotalAmount().intValue());
    }

    @Test
    void getTax_ForTollFreeDate_MonthOfJuly() {

        //Given
        TollRequestPostTo tollRequestPostTo = new TollRequestPostTo("LP", VehicleTypeEnum.GENERAL);
        tollRequestPostTo.addProcessTimesItem(
                OffsetDateTime.of(
                        LocalDate.parse("2013-07-08"),
                        LocalTime.parse("16:00:00"),
                        ZoneOffset.UTC
                )
        );

        //when
        TollResponseTo responseTo = underTest.getTax(tollRequestPostTo);

        //then
        assertNotNull(responseTo);
        assertEquals(0, responseTo.getTotalAmount().intValue());
    }

    @Test
    void getTax_ForTollFreeDate_Weekends() {

        //Given
        TollRequestPostTo tollRequestPostTo = new TollRequestPostTo("LP", VehicleTypeEnum.GENERAL);
        tollRequestPostTo.addProcessTimesItem(
                OffsetDateTime.of(
                        LocalDate.parse("2013-01-05"),
                        LocalTime.parse("16:00:00"),
                        ZoneOffset.UTC
                )
        );
        tollRequestPostTo.addProcessTimesItem(
                OffsetDateTime.of(
                        LocalDate.parse("2013-01-06"),
                        LocalTime.parse("16:00:00"),
                        ZoneOffset.UTC
                )
        );

        //when
        TollResponseTo responseTo = underTest.getTax(tollRequestPostTo);

        //then
        assertNotNull(responseTo);
        assertEquals(0, responseTo.getTotalAmount().intValue());
    }

    @Test
    void getTax_ForNullVehicleType() {

        //Given
        TollRequestPostTo tollRequestPostTo = new TollRequestPostTo("LP", null);
        tollRequestPostTo.addProcessTimesItem(
                OffsetDateTime.of(
                        LocalDate.parse("2013-01-14"),
                        LocalTime.parse("16:00:00"),
                        ZoneOffset.UTC
                )
        );

        PublicHoliday publicHoliday = PublicHoliday.builder().dateMonth(1).monthYear(1).build();
        when(publicHolidaysRepository.findByMonthYear(anyInt())).thenReturn(List.of(publicHoliday));

        LocalTime from = LocalTime.of(0, 0);
        LocalTime to = LocalTime.of(23, 59, 59);
        BigDecimal price = new BigDecimal(20);
        TollFeeChart tollFeeChart = TollFeeChart.builder().startTime(from).endTime(to).price(price).build();
        when(tollFeeChartRepository.findAll()).thenReturn(List.of(tollFeeChart));

        //when
        TollResponseTo responseTo = underTest.getTax(tollRequestPostTo);

        //then
        assertNotNull(responseTo);
        assertEquals(20, responseTo.getTotalAmount().intValue());
    }

    @Test
    void getTax_ForSingleChargeRule() {

        //Given
        TollRequestPostTo tollRequestPostTo = new TollRequestPostTo("LP", VehicleTypeEnum.GENERAL);
        tollRequestPostTo.addProcessTimesItem(
                OffsetDateTime.of(
                        LocalDate.parse("2013-01-14"),
                        LocalTime.parse("15:00:00"),
                        ZoneOffset.UTC
                )
        );
        tollRequestPostTo.addProcessTimesItem(
                OffsetDateTime.of(
                        LocalDate.parse("2013-01-14"),
                        LocalTime.parse("16:00:00"),
                        ZoneOffset.UTC
                )
        );

        PublicHoliday publicHoliday = PublicHoliday.builder().dateMonth(1).monthYear(1).build();
        when(publicHolidaysRepository.findByMonthYear(anyInt())).thenReturn(List.of(publicHoliday));

        LocalTime from = LocalTime.of(0, 0);
        LocalTime to = LocalTime.of(15, 29, 59);
        BigDecimal price = new BigDecimal(1);
        TollFeeChart tollFeeChartOne = TollFeeChart.builder().startTime(from).endTime(to).price(price).build();

        from = LocalTime.of(15, 30);
        to = LocalTime.of(23, 59, 59);
        price = new BigDecimal(0);
        TollFeeChart tollFeeChartTwo = TollFeeChart.builder().startTime(from).endTime(to).price(price).build();

        when(tollFeeChartRepository.findAll()).thenReturn(List.of(tollFeeChartOne, tollFeeChartTwo));

        //when
        TollResponseTo responseTo = underTest.getTax(tollRequestPostTo);

        //then
        assertNotNull(responseTo);
        assertEquals(1, responseTo.getTotalAmount().intValue());
    }

    @Test
    void getTax_ForMaxTollFees() {

        //Given
        TollRequestPostTo tollRequestPostTo = new TollRequestPostTo("LP", VehicleTypeEnum.GENERAL);
        tollRequestPostTo.addProcessTimesItem(
                OffsetDateTime.of(
                        LocalDate.parse("2013-01-14"),
                        LocalTime.parse("15:00:00"),
                        ZoneOffset.UTC
                )
        );
        tollRequestPostTo.addProcessTimesItem(
                OffsetDateTime.of(
                        LocalDate.parse("2013-01-14"),
                        LocalTime.parse("16:30:00"),
                        ZoneOffset.UTC
                )
        );

        PublicHoliday publicHoliday = PublicHoliday.builder().dateMonth(1).monthYear(1).build();
        when(publicHolidaysRepository.findByMonthYear(anyInt())).thenReturn(List.of(publicHoliday));

        LocalTime from = LocalTime.of(0, 0);
        LocalTime to = LocalTime.of(15, 29, 59);
        BigDecimal price = new BigDecimal(45);
        TollFeeChart tollFeeChartOne = TollFeeChart.builder().startTime(from).endTime(to).price(price).build();

        from = LocalTime.of(15, 30);
        to = LocalTime.of(23, 59, 59);
        price = new BigDecimal(16);
        TollFeeChart tollFeeChartTwo = TollFeeChart.builder().startTime(from).endTime(to).price(price).build();

        when(tollFeeChartRepository.findAll()).thenReturn(List.of(tollFeeChartOne, tollFeeChartTwo));

        //when
        TollResponseTo responseTo = underTest.getTax(tollRequestPostTo);

        //then
        assertNotNull(responseTo);
        assertEquals(60, responseTo.getTotalAmount().intValue());
    }

    @Test
    void dontCalculateTax_WhenYear_IsNot_InScope() {

        //Given
        TollRequestPostTo tollRequestPostTo = new TollRequestPostTo("LP", VehicleTypeEnum.GENERAL);
        tollRequestPostTo.addProcessTimesItem(
                OffsetDateTime.of(
                        LocalDate.parse("2014-01-14"),
                        LocalTime.parse("16:00:00"),
                        ZoneOffset.UTC
                )
        );

        Exception exception = assertThrows(
                Exception.class,
                () -> underTest.getTax(tollRequestPostTo),
                "Expected doThing() to throw, but it didn't"
        );

        assertInstanceOf(TollException.class, exception);
        assertEquals("toll free dates not set for 2014", ((TollException) exception).getReason());
    }

    @Test
    void dontCalculateTax_WhenTollFreeVehicleDetails_IsNotSet(){

        underTest = new TollService(new ArrayList<>(), 60L,
                2013, publicHolidaysRepository, tollFeeChartRepository);

        //Given
        TollRequestPostTo tollRequestPostTo = new TollRequestPostTo("LP", VehicleTypeEnum.GENERAL);
        tollRequestPostTo.addProcessTimesItem(
                OffsetDateTime.of(
                        LocalDate.parse("2013-01-14"),
                        LocalTime.parse("16:00:00"),
                        ZoneOffset.UTC
                )
        );

        Exception exception = assertThrows(
                Exception.class,
                () -> underTest.getTax(tollRequestPostTo),
                "Expected doThing() to throw, but it didn't"
        );

        assertInstanceOf(TollException.class, exception);
        assertEquals("toll free vehicle details not set", ((TollException) exception).getReason());
    }

    @Test
    void dontCalculateTax_WhenPublicHoliday_IsNotSet(){

        //Given
        TollRequestPostTo tollRequestPostTo = new TollRequestPostTo("LP", VehicleTypeEnum.GENERAL);
        tollRequestPostTo.addProcessTimesItem(
                OffsetDateTime.of(
                        LocalDate.parse("2013-01-14"),
                        LocalTime.parse("16:00:00"),
                        ZoneOffset.UTC
                )
        );

        when(publicHolidaysRepository.findByMonthYear(anyInt())).thenReturn(new ArrayList<>());

        Exception exception = assertThrows(
                Exception.class,
                () -> underTest.getTax(tollRequestPostTo),
                "Expected doThing() to throw, but it didn't"
        );

        assertInstanceOf(TollException.class, exception);
        assertEquals("toll free dates not set for 2013", ((TollException) exception).getReason());
    }

    @Test
    void dontCalculateTax_WhenTollFeeChart_IsNotSet(){

        //Given
        TollRequestPostTo tollRequestPostTo = new TollRequestPostTo("LP", VehicleTypeEnum.GENERAL);
        tollRequestPostTo.addProcessTimesItem(
                OffsetDateTime.of(
                        LocalDate.parse("2013-01-14"),
                        LocalTime.parse("16:00:00"),
                        ZoneOffset.UTC
                )
        );

        PublicHoliday publicHoliday = PublicHoliday.builder().dateMonth(1).monthYear(1).build();
        when(publicHolidaysRepository.findByMonthYear(anyInt())).thenReturn(List.of(publicHoliday));

        Exception exception = assertThrows(
                Exception.class,
                () -> underTest.getTax(tollRequestPostTo),
                "Expected doThing() to throw, but it didn't"
        );

        assertInstanceOf(TollException.class, exception);
        assertEquals("toll fee charts not set for 2013", ((TollException) exception).getReason());
    }


}