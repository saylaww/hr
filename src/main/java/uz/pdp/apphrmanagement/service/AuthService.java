package uz.pdp.apphrmanagement.service;

import org.springframework.stereotype.Service;
import uz.pdp.apphrmanagement.dto.LoginDto;
import uz.pdp.apphrmanagement.dto.RegisterDto;
import uz.pdp.apphrmanagement.entity.Response;

@Service
public interface AuthService {

    Response register(RegisterDto registerDto);

    Response login(LoginDto loginDto);

    Response verifyEmail(String emailCode, String email, LoginDto loginDto);
}
