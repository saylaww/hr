package uz.pdp.apphrmanagement.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uz.pdp.apphrmanagement.entity.Response;
import uz.pdp.apphrmanagement.entity.Turniket;
import uz.pdp.apphrmanagement.entity.User;
import uz.pdp.apphrmanagement.repository.TurniketRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TurniketServiceImpl implements TurniketService {

    final TurniketRepository turniketRepository;

    public TurniketServiceImpl(TurniketRepository turniketRepository) {
        this.turniketRepository = turniketRepository;
    }

    @Override
    public Response enterToWork() {
        Turniket turniket = new Turniket();
        turniket.setStatus(true);
        turniket.setEnterDateTime(LocalDateTime.now());

        turniketRepository.save(turniket);
        return new Response("Success! You entered!", true);
    }

    @Override
    public Response exitFromWork() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && !authentication.getPrincipal().equals("anonymousUser")) {

            User user = (User) authentication.getPrincipal();


            Optional<Turniket> optionalTurniket = turniketRepository.findByCreatedByAndStatus(user.getId(), true);
            if (!optionalTurniket.isPresent())
                return new Response("Such turniket id not found!", false);

            optionalTurniket.get().setStatus(false);
            optionalTurniket.get().setExitDateTime(LocalDateTime.now());

            turniketRepository.save(optionalTurniket.get());

            return new Response("Success! You exited!", true);
        }

        return new Response("Authentication empty!", false);

    }
}
