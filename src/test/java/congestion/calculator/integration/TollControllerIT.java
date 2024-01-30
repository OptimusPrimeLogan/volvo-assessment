package congestion.calculator.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.openapitools.model.TollRequestPostTo;
import org.openapitools.model.VehicleTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class TollControllerIT {

    @Autowired
    private MockMvc mockMvc;

    ObjectMapper mapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

    @Test
    void generalVehicle() throws Exception {

        TollRequestPostTo tollRequestPostTo = new TollRequestPostTo("LP", VehicleTypeEnum.GENERAL);
        tollRequestPostTo.addProcessTimesItem(
                OffsetDateTime.of(
                        LocalDate.parse("2013-01-14"),
                        LocalTime.parse("16:00:00"),
                        ZoneOffset.UTC
                )
        );

        this.mockMvc.perform(post("/api/toll/v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(tollRequestPostTo))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.licensePlate").value("LP"))
                .andExpect(jsonPath("$.totalAmount").value("18"))
                .andReturn();
    }

    @Test
    void tollFreeVehicle() throws Exception {

        TollRequestPostTo tollRequestPostTo = new TollRequestPostTo("LP", VehicleTypeEnum.DIPLOMAT);
        tollRequestPostTo.addProcessTimesItem(
                OffsetDateTime.of(
                        LocalDate.parse("2013-01-14"),
                        LocalTime.parse("16:00:00"),
                        ZoneOffset.UTC
                )
        );

        this.mockMvc.perform(post("/api/toll/v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(tollRequestPostTo))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.licensePlate").value("LP"))
                .andExpect(jsonPath("$.totalAmount").value("0"))
                .andReturn();
    }

    @Test
    void dayBeforePublicHoliday() throws Exception {

        TollRequestPostTo tollRequestPostTo = new TollRequestPostTo("LP", VehicleTypeEnum.GENERAL);
        tollRequestPostTo.addProcessTimesItem(
                OffsetDateTime.of(
                        LocalDate.parse("2013-04-30"),
                        LocalTime.parse("16:00:00"),
                        ZoneOffset.UTC
                )
        );

        this.mockMvc.perform(post("/api/toll/v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(tollRequestPostTo))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.licensePlate").value("LP"))
                .andExpect(jsonPath("$.totalAmount").value("0"))
                .andReturn();
    }

    @Test
    void monthOfJuly() throws Exception {

        TollRequestPostTo tollRequestPostTo = new TollRequestPostTo("LP", VehicleTypeEnum.GENERAL);
        tollRequestPostTo.addProcessTimesItem(
                OffsetDateTime.of(
                        LocalDate.parse("2013-07-30"),
                        LocalTime.parse("16:00:00"),
                        ZoneOffset.UTC
                )
        );

        this.mockMvc.perform(post("/api/toll/v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(tollRequestPostTo))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.licensePlate").value("LP"))
                .andExpect(jsonPath("$.totalAmount").value("0"))
                .andReturn();
    }

    @Test
    void weekends() throws Exception {

        TollRequestPostTo tollRequestPostTo = new TollRequestPostTo("LP", VehicleTypeEnum.GENERAL);
        tollRequestPostTo.addProcessTimesItem(
                OffsetDateTime.of(
                        LocalDate.parse("2013-07-30"),
                        LocalTime.parse("16:00:00"),
                        ZoneOffset.UTC
                )
        );

        this.mockMvc.perform(post("/api/toll/v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(tollRequestPostTo))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.licensePlate").value("LP"))
                .andExpect(jsonPath("$.totalAmount").value("0"))
                .andReturn();
    }

    @Test
    void singleChargeRule() throws Exception {

        TollRequestPostTo tollRequestPostTo = new TollRequestPostTo("LP", VehicleTypeEnum.GENERAL);
        tollRequestPostTo.addProcessTimesItem(
                OffsetDateTime.of(
                        LocalDate.parse("2013-01-14"),
                        LocalTime.parse("06:29:00"),
                        ZoneOffset.UTC
                )
        );
        tollRequestPostTo.addProcessTimesItem(
                OffsetDateTime.of(
                        LocalDate.parse("2013-01-14"),
                        LocalTime.parse("06:59:00"),
                        ZoneOffset.UTC
                )
        );

        this.mockMvc.perform(post("/api/toll/v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(tollRequestPostTo))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.licensePlate").value("LP"))
                .andExpect(jsonPath("$.totalAmount").value("13"))
                .andReturn();
    }

    @Test
    void singleChargeRulePlusOneMoreEntry() throws Exception {

        TollRequestPostTo tollRequestPostTo = new TollRequestPostTo("LP", VehicleTypeEnum.GENERAL);
        tollRequestPostTo.addProcessTimesItem(
                OffsetDateTime.of(
                        LocalDate.parse("2013-01-14"),
                        LocalTime.parse("06:29:00"),
                        ZoneOffset.UTC
                )
        );
        tollRequestPostTo.addProcessTimesItem(
                OffsetDateTime.of(
                        LocalDate.parse("2013-01-14"),
                        LocalTime.parse("06:59:00"),
                        ZoneOffset.UTC
                )
        );
        tollRequestPostTo.addProcessTimesItem(
                OffsetDateTime.of(
                        LocalDate.parse("2013-01-14"),
                        LocalTime.parse("08:59:00"),
                        ZoneOffset.UTC
                )
        );

        this.mockMvc.perform(post("/api/toll/v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(tollRequestPostTo))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.licensePlate").value("LP"))
                .andExpect(jsonPath("$.totalAmount").value("21"))
                .andReturn();
    }


    @Test
    void maxTollFees() throws Exception {

        TollRequestPostTo tollRequestPostTo = new TollRequestPostTo("LP", VehicleTypeEnum.GENERAL);
        tollRequestPostTo.addProcessTimesItem(
                OffsetDateTime.of(
                        LocalDate.parse("2013-01-14"),
                        LocalTime.parse("06:29:00"),
                        ZoneOffset.UTC
                )
        );
        tollRequestPostTo.addProcessTimesItem(
                OffsetDateTime.of(
                        LocalDate.parse("2013-01-14"),
                        LocalTime.parse("07:59:00"),
                        ZoneOffset.UTC
                )
        );
        tollRequestPostTo.addProcessTimesItem(
                OffsetDateTime.of(
                        LocalDate.parse("2013-01-14"),
                        LocalTime.parse("09:01:00"),
                        ZoneOffset.UTC
                )
        );
        tollRequestPostTo.addProcessTimesItem(
                OffsetDateTime.of(
                        LocalDate.parse("2013-01-14"),
                        LocalTime.parse("10:12:00"),
                        ZoneOffset.UTC
                )
        );
        tollRequestPostTo.addProcessTimesItem(
                OffsetDateTime.of(
                        LocalDate.parse("2013-01-14"),
                        LocalTime.parse("15:12:00"),
                        ZoneOffset.UTC
                )
        );
        tollRequestPostTo.addProcessTimesItem(
                OffsetDateTime.of(
                        LocalDate.parse("2013-01-14"),
                        LocalTime.parse("16:25:00"),
                        ZoneOffset.UTC
                )
        );

        this.mockMvc.perform(post("/api/toll/v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(tollRequestPostTo))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.licensePlate").value("LP"))
                .andExpect(jsonPath("$.totalAmount").value("60"))
                .andReturn();
    }

}