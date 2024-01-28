package congestion.calculator.controller;

import congestion.calculator.exception.TollException;
import congestion.calculator.service.TollService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.model.TollResponseTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

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

        TollResponseTo tollResponseTo = new TollResponseTo("LP", new BigDecimal(0));

        when(tollService.getTax(any())).thenReturn(tollResponseTo);

        this.mockMvc.perform(post("/api/toll/v1").contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.licensePlate").value("LP"))
                .andExpect(jsonPath("$.totalAmount").value("0"))
                .andReturn();
    }

    @Test
    void processTollRequest_WhenAnExceptionThrown() throws Exception {
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