package congestion.calculator.service;

import org.openapitools.model.TollRequestPostTo;
import org.openapitools.model.TollResponseTo;
import org.openapitools.model.VehicleTypeEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequestScope
public class TollService {

    @Value("#{'${toll.free.vehicles}'.split(',')}")
    protected final List<String> tollFreeVehicles;

    public TollService(List<String> tollFreeVehicles) {
        this.tollFreeVehicles = tollFreeVehicles;
    }

    public TollResponseTo getTax(TollRequestPostTo tollRequestPostTo) {

        String licensePlate = tollRequestPostTo.getLicensePlate();
        VehicleTypeEnum vehicle = tollRequestPostTo.getVehicleType();
        List<OffsetDateTime> dates = tollRequestPostTo.getProcessTimes();

        OffsetDateTime intervalStart = dates.get(0);
        int totalFee = 0;

        for (int i = 0; i < dates.size(); i++) {
            OffsetDateTime date = dates.get(0);
            int nextFee = GetTollFee(date, vehicle);
            int tempFee = GetTollFee(intervalStart, vehicle);

            long diffInMillies = date.toInstant().toEpochMilli() - intervalStart.toInstant().toEpochMilli();
            long minutes = diffInMillies / 1000 / 60;

            if (minutes <= 60) {
                if (totalFee > 0) totalFee -= tempFee;
                if (nextFee >= tempFee) tempFee = nextFee;
                totalFee += tempFee;
            } else {
                totalFee += nextFee;
            }
        }

        if (totalFee > 60) totalFee = 60;

        return new TollResponseTo(licensePlate, new BigDecimal(totalFee));

    }

    private boolean IsTollFreeVehicle(VehicleTypeEnum vehicle) {
        if (vehicle == null) return false;
        return tollFreeVehicles.contains(vehicle.getValue());
    }

    public int GetTollFee(OffsetDateTime date, VehicleTypeEnum vehicle) {
        if (IsTollFreeDate(date) || IsTollFreeVehicle(vehicle)) return 0;

        int hour = date.getHour();
        int minute = date.getMinute();

        if (hour == 6 && minute <= 29) return 8;
        else if (hour == 6) return 13;
        else if (hour == 7) return 18;
        else if (hour == 8 && minute <= 29) return 13;
        else if (hour >= 8 && hour <= 14 && minute >= 30) return 8;
        else if (hour == 15 && minute <= 29) return 13;
        else if (hour == 15 || hour == 16) return 18;
        else if (hour == 17) return 13;
        else if (hour == 18 && minute <= 29) return 8;
        else return 0;
    }

    private Boolean IsTollFreeDate(OffsetDateTime date) {
        int year = date.getYear();
        int month = date.getMonthValue();
        DayOfWeek day = date.getDayOfWeek();
        int dayOfMonth = date.getDayOfMonth();

        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) return true;

        if (year == 2013) {
            return (month == 1 && dayOfMonth == 1) ||
                    (month == 3 && (dayOfMonth == 28 || dayOfMonth == 29)) ||
                    (month == 4 && (dayOfMonth == 1 || dayOfMonth == 30)) ||
                    (month == 5 && (dayOfMonth == 1 || dayOfMonth == 8 || dayOfMonth == 9)) ||
                    (month == 6 && (dayOfMonth == 5 || dayOfMonth == 6 || dayOfMonth == 21)) ||
                    (month == 7) ||
                    (month == 11 && dayOfMonth == 1) ||
                    (month == 12 && (dayOfMonth == 24 || dayOfMonth == 25 || dayOfMonth == 26 || dayOfMonth == 31));
        }
        return false;
    }
}
