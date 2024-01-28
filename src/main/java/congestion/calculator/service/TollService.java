package congestion.calculator.service;

import congestion.calculator.exception.TollException;
import congestion.calculator.repository.PublicHolidaysRepository;
import congestion.calculator.repository.TollFeeChartRepository;
import congestion.calculator.repository.entity.PublicHolidays;
import lombok.SneakyThrows;
import org.openapitools.model.TollRequestPostTo;
import org.openapitools.model.TollResponseTo;
import org.openapitools.model.VehicleTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequestScope
public class TollService {

    @Value("#{'${toll.free.vehicles}'.split(',')}")
    private List<String> tollFreeVehicles;

    @Value("${minutes.allowed.for:60}")
    private Long minutesAllowedFor;

    @Value("${year.in.scope:2013}")
    private Integer yearInScope;

    @Autowired private PublicHolidaysRepository publicHolidaysRepository;
    @Autowired private TollFeeChartRepository tollFeeChartRepository;

    @SneakyThrows
    public TollResponseTo getTax(TollRequestPostTo tollRequestPostTo) {

        String licensePlate = null;
        VehicleTypeEnum vehicle;
        int totalFee = 0;
        List<OffsetDateTime> dates;

        try {
            licensePlate = tollRequestPostTo.getLicensePlate();
            vehicle = tollRequestPostTo.getVehicleType();
            dates = tollRequestPostTo.getProcessTimes();

            OffsetDateTime intervalStart = dates.get(0);

            for (int i = 0; i < dates.size(); i++) {
                OffsetDateTime date = dates.get(0);
                int nextFee = getTollFee(date, vehicle);
                int tempFee = getTollFee(intervalStart, vehicle);

                long diffInMillis = date.toInstant().toEpochMilli() - intervalStart.toInstant().toEpochMilli();
                long minutes = diffInMillis / 1000 / 60;

                if (minutes <= minutesAllowedFor) {
                    if (totalFee > 0) totalFee -= tempFee;
                    if (nextFee >= tempFee) tempFee = nextFee;
                    totalFee += tempFee;
                } else {
                    totalFee += nextFee;
                }

                if (totalFee > 60) {
                    totalFee = 60;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw TollException.createTollException(licensePlate, 1000L, e.getMessage());
        }

        return new TollResponseTo(licensePlate, new BigDecimal(totalFee));
    }

    private boolean isTollFreeVehicle(VehicleTypeEnum vehicle) {
        if (vehicle == null) return false;
        return tollFreeVehicles.contains(vehicle.getValue());
    }

    private int getTollFee(OffsetDateTime date, VehicleTypeEnum vehicle) {
        if (isTollFreeVehicle(vehicle) || isTollFreeDate(date)) return 0;

        int hour = date.getHour();
        int minute = date.getMinute();
        AtomicInteger price = new AtomicInteger();

        LocalTime derivedTime = LocalTime.of(hour, minute);

        tollFeeChartRepository.findAll().forEach(s -> {
            if(derivedTime.isAfter(s.getStartTime()) && derivedTime.isBefore(s.getEndTime())){
                price.set(s.getPrice().intValue());
            }
        });

        return price.get();
    }

    private Boolean isTollFreeDate(OffsetDateTime date) {
        int year = date.getYear();
        int month = date.getMonthValue();
        int dayOfTheMonth = date.getDayOfMonth();
        DayOfWeek day = date.getDayOfWeek();
        AtomicBoolean tollFreeDate = new AtomicBoolean(false);

        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) return true;

        if (year == yearInScope) {
            List<PublicHolidays> publicHolidays = publicHolidaysRepository.findByMonthYear(month);
            publicHolidays.forEach(s -> {
                OffsetDateTime holidayEve;
                //New Year's Eve
                if(month == 1 && dayOfTheMonth==1){
                    holidayEve = OffsetDateTime.of(LocalDateTime.of(year, 12, 31, 0, 0), ZoneOffset.UTC);
                }else{
                    holidayEve = OffsetDateTime.of(LocalDateTime.of(year, month, s.getDateMonth() - 1, 0, 0), ZoneOffset.UTC);
                }
                if (holidayEve.equals(date))
                    tollFreeDate.set(true);
            });
        }
        return tollFreeDate.get();
    }
}
