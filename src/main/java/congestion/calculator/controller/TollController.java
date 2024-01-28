package congestion.calculator.controller;

import congestion.calculator.service.TollService;
import org.openapitools.api.TollApi;
import org.openapitools.model.TollRequestPostTo;
import org.openapitools.model.TollResponseTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TollController implements TollApi {

    @Autowired
    TollService service;

    @Override
    public ResponseEntity<TollResponseTo> processTollRequest(TollRequestPostTo tollRequestPostTo) {
        return ResponseEntity.ok(service.getTax(tollRequestPostTo));
    }
}