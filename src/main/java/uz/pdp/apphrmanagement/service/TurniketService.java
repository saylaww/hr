package uz.pdp.apphrmanagement.service;

import org.springframework.stereotype.Service;
import uz.pdp.apphrmanagement.entity.Response;

@Service
public interface TurniketService {

    Response enterToWork();

    Response exitFromWork();
}
