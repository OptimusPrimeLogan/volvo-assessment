package congestion.calculator.service;

import congestion.calculator.exception.TollException;
import congestion.calculator.model.TollDateResponseTo;
import congestion.calculator.model.TollRequestPostTo;
import congestion.calculator.model.TollResponseTo;
import congestion.calculator.model.VehicleTypeEnum;
import congestion.calculator.repository.PublicHolidaysRepository;
import congestion.calculator.repository.TollFeeChartRepository;
import congestion.calculator.repository.entity.PublicHoliday;
import congestion.calculator.repository.entity.TollFeeChart;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequestScope
@Slf4j
public class TollService {

    private final List<String> tollFreeVehicles;

    // a vehicle that passes several tolling stations within the below configurable minutes
    private final Long minutesAllowedFor;

    private final Integer yearInScope;

    private final PublicHolidaysRepository publicHolidaysRepository;
    private final TollFeeChartRepository tollFeeChartRepository;

    public TollService(@Value("#{'${toll.free.vehicles}'.split(',')}") List<String> tollFreeVehicles,
                       @Value("${minutes.allowed.for:60}") Long minutesAllowedFor,
                       @Value("${year.in.scope:2013}") Integer yearInScope,
                       PublicHolidaysRepository publicHolidaysRepository, TollFeeChartRepository tollFeeChartRepository) {
        this.tollFreeVehicles = tollFreeVehicles;
        this.minutesAllowedFor = minutesAllowedFor;
        this.yearInScope = yearInScope;
        this.publicHolidaysRepository = publicHolidaysRepository;
        this.tollFeeChartRepository = tollFeeChartRepository;
    }

    /***
     * Method to handle the request from the controller
     * @param tollRequestPostTo {@link TollRequestPostTo}
     * @return {@link TollResponseTo}
     *@throws TollException if the master values are not set
     */
    @SneakyThrows
    public TollResponseTo getTax(TollRequestPostTo tollRequestPostTo) {

        String licensePlate = null;
        VehicleTypeEnum vehicle;
        int totalFee = 0;
        List<OffsetDateTime> dates;
        List<TollDateResponseTo> tollResponseTos = new ArrayList<>();

        try {
            vehicle = tollRequestPostTo.getVehicleType();
            licensePlate = tollRequestPostTo.getLicensePlate();

            //Can be returned as ZERO here itself for toll-free vehicles
            if (isTollFreeVehicle(vehicle)) {
                TollDateResponseTo tollDateResponseTo = new TollDateResponseTo(LocalDate.now()).totalAmount(BigDecimal.ZERO);
                return new TollResponseTo(licensePlate, List.of(tollDateResponseTo), BigDecimal.ZERO);
            }

            dates = tollRequestPostTo.getProcessTimes();

            Map<Integer, List<OffsetDateTime>> dateBuckets = dates.stream()
                    .collect(Collectors.groupingBy(item -> item.toLocalDate().getDayOfMonth()));

            for (List<OffsetDateTime> dateBucket: dateBuckets.values()){//Per Date
                OffsetDateTime intervalStart = dateBucket.get(0);
                int dayTollFee = 0;
                for (OffsetDateTime date : dateBucket) {// Per Time
                    int nextFee = getTollFee(date);
                    int tempFee = getTollFee(intervalStart);

                    long diffInMillis = date.toInstant().toEpochMilli() - intervalStart.toInstant().toEpochMilli();
                    long minutes = diffInMillis / 1000 / 60;

                    if (minutes <= minutesAllowedFor) {
                        if (dayTollFee > 0) dayTollFee -= tempFee;
                        if (nextFee >= tempFee) tempFee = nextFee;
                        dayTollFee += tempFee;
                    } else {
                        dayTollFee += nextFee;
                    }

                    if (dayTollFee > 60) {
                        dayTollFee = 60;
                        break;
                    }
                }
                TollDateResponseTo tollDateResponseTo = new TollDateResponseTo(intervalStart.toLocalDate()).totalAmount(new BigDecimal(dayTollFee));
                tollResponseTos.add(tollDateResponseTo);
                totalFee += dayTollFee;
            }


        } catch (Exception e) {
            log.error("Unable to calculate due to " + e.getMessage());
            throw TollException.createTollException(licensePlate, 1000L, e.getMessage());
        }

        return new TollResponseTo(licensePlate, tollResponseTos, new BigDecimal(totalFee));
    }

    /**
     * Method to check if the vehicle is toll-free
     * @param vehicle {@link VehicleTypeEnum}
     * @return a boolean
     * @throws Exception if the value is not set
     */
    @SneakyThrows
    private boolean isTollFreeVehicle(VehicleTypeEnum vehicle) {
        if (vehicle == null) return false;

        if (tollFreeVehicles.isEmpty()) {
            String errorInfo = "toll free vehicle details not set";
            log.error(errorInfo);
            throw new Exception(errorInfo);
        }

        return tollFreeVehicles.contains(vehicle.getValue());
    }

    /**
     * Method to get the toll fee based on inputs
     * @param date {@link OffsetDateTime}
     * @return the toll amount
     * @throws Exception if the value is not set
     */
    @SneakyThrows
    private int getTollFee(OffsetDateTime date) {
        if (isTollFreeDate(date)) return 0;

        int hour = date.getHour();
        int minute = date.getMinute();
        int year = date.getYear();

        LocalTime derivedTime = LocalTime.of(hour, minute);
        //If data per year has to be maintained, the query can be changed to findByYear
        List<TollFeeChart> tollFeeCharts = tollFeeChartRepository.findAll();

        if (tollFeeCharts.isEmpty()) {
            log.error("toll fee charts not set for " + date.getYear());
            throw new Exception("toll fee charts not set for " + year);
        }

        for (TollFeeChart tollFeeChart : tollFeeCharts) {
            if ((derivedTime.isAfter(tollFeeChart.getStartTime()) || derivedTime.equals(tollFeeChart.getStartTime()))
                    && (derivedTime.isBefore(tollFeeChart.getEndTime())) || derivedTime.equals(tollFeeChart.getEndTime())) {
                return tollFeeChart.getPrice().intValue();
            }
        }

        return 0;
    }

    /**
     * Method to find the dates are meant for no toll collection
     * @param date {@link OffsetDateTime}
     * @return a boolean
     * @throws Exception if the value is not set
     */
    @SneakyThrows
    private boolean isTollFreeDate(OffsetDateTime date) {
        int year = date.getYear();
        Month month = date.getMonth();
        DayOfWeek day = date.getDayOfWeek();
        String errorInfo = "toll free dates not set for ".concat(String.valueOf(year));

        if (month == Month.JULY) return true;

        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) return true;

        if (year == yearInScope) {
            List<PublicHoliday> publicHolidays = publicHolidaysRepository.findAll();

            if (publicHolidays.isEmpty()) {
                log.error(errorInfo);
                throw new Exception(errorInfo);
            }

            for (PublicHoliday publicHoliday : publicHolidays) {
                OffsetDateTime publicHolidayOffset = OffsetDateTime.of(
                        LocalDateTime.of(yearInScope, publicHoliday.getMonthYear(), publicHoliday.getDateMonth(), date.getHour(), 0), ZoneOffset.UTC);

                if (date.truncatedTo(ChronoUnit.HOURS).plusDays(1).equals(publicHolidayOffset)) {
                    return true;
                }
            }

        } else {
            log.error(errorInfo);
            throw new Exception(errorInfo);
        }
        return false;
    }
}
