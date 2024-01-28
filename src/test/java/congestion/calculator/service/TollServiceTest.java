package congestion.calculator.service;

import org.junit.jupiter.api.Test;
import org.openapitools.model.VehicleTypeEnum;

import java.time.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TollServiceTest {

    TollService tollService = new TollService(List.of(VehicleTypeEnum.EMERGENCY.getValue()));

    @Test
    void getTax() {

    }

    @Test
    void getTollFee() {
        OffsetDateTime sixFourAm = OffsetDateTime.of(LocalDateTime.of(LocalDate.of(2013, 2, 1),
                LocalTime.of(6, 4, 0)), ZoneOffset.UTC);
        assertEquals(8,  tollService.GetTollFee(sixFourAm, VehicleTypeEnum.GENERAL) );
    }
}