package congestion.calculator.controller;

import congestion.calculator.service.TollService;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import org.openapitools.api.TollApi;
import org.openapitools.model.TollRequestPostTo;
import org.openapitools.model.TollResponseTo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TollController implements TollApi {

    private final TollService service;

    public TollController(TollService service) {
        this.service = service;
    }

    @Override
    @Timed(value="ProcessTollRequest", description = "processTollRequest", histogram = true)
    @Counted(value = "TollController_processTollRequest", description = "Counter for calculating toll rates")
    public ResponseEntity<TollResponseTo> processTollRequest(TollRequestPostTo tollRequestPostTo) {
        return ResponseEntity.ok(service.getTax(tollRequestPostTo));
    }
}