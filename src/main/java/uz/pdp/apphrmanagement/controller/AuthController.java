package uz.pdp.apphrmanagement.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.apphrmanagement.dto.LoginDto;
import uz.pdp.apphrmanagement.dto.RegisterDto;
import uz.pdp.apphrmanagement.entity.Response;
import uz.pdp.apphrmanagement.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    private HttpEntity<?> register(@RequestBody RegisterDto registerDto) {
        Response response = authService.register(registerDto);
        return ResponseEntity.status(response.isSuccess() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST).body(response);
    }

    @PostMapping("/login")
    public HttpEntity<?> login(@RequestBody LoginDto loginDto) {
        Response login = authService.login(loginDto);
        return ResponseEntity.status(login.isSuccess() ? 200 : 401).body(login);
    }

    @PostMapping("/verify")
    public HttpEntity<?> verifyEmail(@RequestParam String emailCode, @RequestParam String email, @RequestBody LoginDto loginDto) {
        Response response = authService.verifyEmail(emailCode, email, loginDto);
        return ResponseEntity.status(response.isSuccess() ? HttpStatus.ACCEPTED : HttpStatus.ALREADY_REPORTED).body(response);
    }
}
