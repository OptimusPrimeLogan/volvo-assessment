package congestion.calculator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import congestion.calculator.exception.TollException;
import congestion.calculator.service.TollService;
import congestion.calculator.model.TollRequestPostTo;
import congestion.calculator.model.TollResponseTo;
import congestion.calculator.model.VehicleTypeEnum;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TollController.class)
class TollControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    TollService tollService;

    TollResponseTo tollResponseTo = new TollResponseTo("LP", new BigDecimal(0));

    ObjectMapper mapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

    @BeforeEach
    void setUp() {
        openMocks(this);
    }

    @AfterEach
    void tearDown() {
        reset(tollService);
    }

    @Test
    void processTollRequest_Valid() throws Exception {

        TollRequestPostTo tollRequestPostTo = new TollRequestPostTo("LP", VehicleTypeEnum.GENERAL);
        tollRequestPostTo.addProcessTimesItem(
                OffsetDateTime.of(
                        LocalDate.parse("2013-01-14"),
                        LocalTime.parse("16:00:00"),
                        ZoneOffset.UTC
                )
        );

        when(tollService.getTax(any())).thenReturn(tollResponseTo);

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
    void processTollRequest_WhenAnExceptionThrown() throws Exception {

        TollRequestPostTo tollRequestPostTo = new TollRequestPostTo("LP", VehicleTypeEnum.GENERAL);
        tollRequestPostTo.addProcessTimesItem(
                OffsetDateTime.of(
                        LocalDate.parse("2013-01-14"),
                        LocalTime.parse("16:00:00"),
                        ZoneOffset.UTC
                )
        );

        doAnswer((invocation) -> {
            throw TollException.createTollException("LP", 1000L,
                    "TEST");
        }).when(tollService).getTax(any());

        this.mockMvc.perform(post("/api/toll/v1").contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.licensePlate").value("LP"))
                .andExpect(jsonPath("$.errorCode").value("1000"))
                .andExpect(jsonPath("$.reason").value("TEST"))
                .andReturn();
    }

}