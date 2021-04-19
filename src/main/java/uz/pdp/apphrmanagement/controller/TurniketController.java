package uz.pdp.apphrmanagement.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.apphrmanagement.entity.Response;
import uz.pdp.apphrmanagement.service.TurniketService;

@RestController
@RequestMapping("/api/turniket")
public class TurniketController {

    final TurniketService turniketService;

    public TurniketController(TurniketService turniketService) {
        this.turniketService = turniketService;
    }

    @PostMapping
    public HttpEntity<?> enterToWork() {
        Response response = turniketService.enterToWork();
        return ResponseEntity.status(response.isSuccess() ? 200 : 401).body(response);
    }

    @PutMapping
    public HttpEntity<?> exitFromWork(){
        Response response = turniketService.exitFromWork();
        return ResponseEntity.status(response.isSuccess() ? 200 : 401).body(response);
    }


}
