package congestion.calculator.controller;

import congestion.calculator.api.TollApi;
import congestion.calculator.model.TollRequestPostTo;
import congestion.calculator.model.TollResponseTo;
import congestion.calculator.service.TollService;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller class to handle toll calculation requests
 */
@RestController
public class TollController implements TollApi {

    private final TollService service;

    /**
     * Constructor injection
     * @param service {@link TollService}
     */
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