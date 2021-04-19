package uz.pdp.apphrmanagement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.pdp.apphrmanagement.dto.LoginDto;
import uz.pdp.apphrmanagement.dto.RegisterDto;
import uz.pdp.apphrmanagement.entity.Response;
import uz.pdp.apphrmanagement.entity.Role;
import uz.pdp.apphrmanagement.entity.User;
import uz.pdp.apphrmanagement.repository.RoleRepository;
import uz.pdp.apphrmanagement.repository.UserRepository;
import uz.pdp.apphrmanagement.security.JwtProvider;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService, UserDetailsService {

    final UserRepository userRepository;
    final PasswordEncoder passwordEncoder;
    final RoleRepository roleRepository;
    final JavaMailSender javaMailSender;
    final AuthenticationManager authenticationManager;
    final JwtProvider jwtProvider;

    @Autowired
    public AuthServiceImpl(@Lazy UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           RoleRepository roleRepository,
                           JavaMailSender javaMailSender,
                           AuthenticationManager authenticationManager,
                           JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.javaMailSender = javaMailSender;
        this.authenticationManager = authenticationManager;
        this.jwtProvider = jwtProvider;
    }

    @Override
    public Response register(RegisterDto registerDto) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.getPrincipal().equals("anonymousUser")) {
            User user = saveTempUserData(registerDto);
            user.setEnabled(true);
            user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
            userRepository.save(user);
            return new Response("Director added!", true);
        } else {
            User userContext = (User) authentication.getPrincipal();
            Set<Role> roles = userContext.getRoles();

            int role_index = 0;

            for (Role role : roles) {
                // DIRECTOR bo'lsa MANGER qo'shadi
                if (role.getRoleName().name().equals("DIRECTOR")) {
                    role_index = 1;
                }

                // HR_MANAGER bo'lsa boshqa xodimlarni qo’shadi
                if (role.getRoleName().name().equals("HR_MANAGER")) {
                    role_index = 2;
                }
            }

            User user1 = saveTempUserData(registerDto);

            if (role_index == 2) {

                Set<Role> sentRoles = user1.getRoles();
                int role_index2 = 0;
                for (Role sentRole : sentRoles) {
                    // HR_MANAGER -> DIRECTOR, HR_MANAGER va MANAGER qo'sha olmasligi tekshirildi
                    if (sentRole.getRoleName().name().equals("DIRECTOR")
                            || sentRole.getRoleName().name().equals("HR_MANAGER")
                            || sentRole.getRoleName().name().equals("MANAGER"))
                        role_index2 = 1;


                    // HR_MANAGER -> EMPLOYEE qo'sha olishi tekshirildi
                    if (sentRole.getRoleName().name().equals("EMPLOYEE"))
                        role_index2 = 2;

                }

                if (role_index2 == 1)
                    return new Response("HR MANAGER can add just EMPLOYEE's", false);

            }
            user1.setPassword("");
            User savedUser = userRepository.save(user1);

            //Managerlarni qo’shganda ularning email manziliga link jo’natiladi
            sendEmail(savedUser.getEmail(), savedUser.getEmailCode());


            return new Response("Mail sent!", true);
        }
    }

    public User saveTempUserData(RegisterDto registerDto) {
        User user = new User();
        user.setFirstname(registerDto.getFirstname());
        user.setLastname(registerDto.getLastname());
        user.setEmail(registerDto.getEmail());

        Set<Integer> roleIdList = registerDto.getRoleIdList();
        Set<Role> roleSet = new HashSet<>();

        for (Integer roleId : roleIdList) {
            Optional<Role> optionalRole = roleRepository.findById(roleId);
            optionalRole.ifPresent(roleSet::add);
        }
        user.setRoles(roleSet);
        user.setEmailCode(UUID.randomUUID().toString());
        return user;
    }

    public void sendEmail(String sendingEmail, String emailCode) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom("gm.khamza@gmail.com");
            mailMessage.setTo(sendingEmail);
            mailMessage.setSubject("Account confirmation!");
            mailMessage.setText("<a href='http://localhost:8080/api/auth/verify?emailCode=" + emailCode + "&email=" + sendingEmail + "'>Confirm</a>");

            javaMailSender.send(mailMessage);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public Response login(LoginDto loginDto) {

        try {
            Authentication authenticate =
                    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()));

            User user = (User) authenticate.getPrincipal();
            String token = jwtProvider.generateToken(user.getUsername(), user.getRoles());
            return new Response("Token", true, token);
        } catch (BadCredentialsException badCredentialsException) {
            return new Response("Username or password failed!", false);
        }
    }

    @Override
    public Response verifyEmail(String emailCode, String email, LoginDto loginDto) {
        Optional<User> optionalUser = userRepository.findByEmailAndEmailCode(email, emailCode);
        if (optionalUser.isPresent()) {
            optionalUser.get().setEnabled(true);
            optionalUser.get().setEmailCode(null);
            optionalUser.get().setPassword(passwordEncoder.encode(loginDto.getPassword()));
            userRepository.save(optionalUser.get());
            return new Response("Account confirmed!", true);
        }
        return new Response("Account is already confirmed!", false);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException(username + " not found"));
    }
}
