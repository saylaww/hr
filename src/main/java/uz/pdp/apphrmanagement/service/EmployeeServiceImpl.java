package uz.pdp.apphrmanagement.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uz.pdp.apphrmanagement.dto.SalaryDto;
import uz.pdp.apphrmanagement.entity.*;
import uz.pdp.apphrmanagement.entity.enums.RoleName;
import uz.pdp.apphrmanagement.entity.enums.TaskStatus;
import uz.pdp.apphrmanagement.repository.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    final UserRepository userRepository;
    final RoleRepository roleRepository;
    final SalaryHistoryRepository salaryHistoryRepository;
    final TurniketRepository turniketRepository;
    final TaskRepository taskRepository;

    public EmployeeServiceImpl(UserRepository userRepository,
                               RoleRepository roleRepository,
                               SalaryHistoryRepository salaryHistoryRepository,
                               TurniketRepository turniketRepository,
                               TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.salaryHistoryRepository = salaryHistoryRepository;
        this.turniketRepository = turniketRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    public Response findAllEmployees() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && !authentication.getPrincipal().equals("anonymousUser")) {

            User user = (User) authentication.getPrincipal();
            Set<Role> roles = user.getRoles();

            boolean checkRole = false;
            for (Role role : roles) {
                if (role.getRoleName().name().equals("DIRECTOR") || role.getRoleName().name().equals("HR_MANAGER")) {
                    checkRole = true;
                    break;
                }
            }
            if (!checkRole)
                return new Response("You don't have access for this operation", false);

            Optional<Role> optionalRole = roleRepository.findByRoleName(RoleName.EMPLOYEE);
            if (!optionalRole.isPresent())
                return new Response("Role not found!", false);

            Set<Role> roleSet = new HashSet<>();
            roleSet.add(optionalRole.get());

            List<User> employeeList = userRepository.findAllByRolesIn(Collections.singleton(roleSet));

            return new Response("Success!", true, employeeList);
        }

        return new Response("Authorization empty!", false);
    }

    @Override
    public EmpResponse findOneByData(UUID id, Timestamp start, Timestamp finish) {

        LocalDateTime startLocal = start.toLocalDateTime();
        LocalDateTime finishLocal = finish.toLocalDateTime();

        Optional<User> optionalEmployee = userRepository.findById(id);
        if (!optionalEmployee.isPresent())
            return new EmpResponse("Such employee id not found!", false);

        Set<Role> roles = optionalEmployee.get().getRoles();
        boolean checkEmployeeRole = false;
        for (Role role : roles) {
            if (role.getRoleName().name().equals("EMPLOYEE")) {
                checkEmployeeRole = true;
                break;
            }
        }

        List<Turniket> turniketList =
                turniketRepository.findAllByCreatedByAndEnterDateTimeAndExitDateTimeBefore(id, startLocal, finishLocal);

        if (turniketList.isEmpty())
            return new EmpResponse("Data not found!", false);

        EmpResponse empResponse = new EmpResponse();
        empResponse.setTurniketList(turniketList);

        if (checkEmployeeRole) {
            List<Task> taskList = taskRepository.findAllByStatusAndResponsibleId(TaskStatus.COMPLETED, id);
            empResponse.setTaskList(taskList);
        }

        return empResponse;
    }

    @Override
    public Response payMonthly(SalaryDto salaryDto) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && !authentication.getPrincipal().equals("anonymousUser")) {
            User user = (User) authentication.getPrincipal();
            Set<Role> roles = user.getRoles();

            boolean checkRole = false;
            for (Role role : roles) {
                if (role.getRoleName().name().equals("DIRECTOR") || role.getRoleName().name().equals("HR_MANAGER")) {
                    checkRole = true;
                    break;
                }
            }
            if (!checkRole)
                return new Response("You don't have access for this operation", false);

            Optional<User> optionalEmployee = userRepository.findById(salaryDto.getEmployeeId());
            if (!optionalEmployee.isPresent())
                return new Response("Such Employee was not found!", false);

            SalaryHistory salaryHistory = new SalaryHistory();
            salaryHistory.setEmployee(optionalEmployee.get());
            salaryHistory.setSalaryAmount(salaryDto.getSalaryAmount());
            salaryHistory.setWorkStartDate(salaryDto.getWorkStartDate());
            salaryHistory.setWorkEndDate(salaryDto.getWorkEndDate());

            salaryHistoryRepository.save(salaryHistory);

            return new Response("Salary Saved! To: " + optionalEmployee.get().getFirstname(), true);
        }
        return new Response("Authorization empty!", false);
    }

    @Override
    public Response getSalariesByMonth(String year, Integer monthNumber) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && !authentication.getPrincipal().equals("anonymousUser")) {
            User user = (User) authentication.getPrincipal();
            Set<Role> roles = user.getRoles();

            boolean checkRole = false;
            for (Role role : roles) {
                if (role.getRoleName().name().equals("DIRECTOR") || role.getRoleName().name().equals("HR_MANAGER")) {
                    checkRole = true;
                    break;
                }
            }
            if (!checkRole)
                return new Response("You don't have access for this operation", false);

            String month = monthNumber + "";
            if (monthNumber < 10)
                month = "0" + monthNumber;

            String full = year + "-" + month + "-01 05:00";
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime dateTime = LocalDateTime.parse(full, formatter);

            Timestamp start = Timestamp.valueOf(dateTime);
            List<SalaryHistory> salaryHistoryList = salaryHistoryRepository.findAllByWorkStartDate(start);
            if (salaryHistoryList.size() == 0)
                return new Response("Salary list empty!", false);

            return new Response("Success!", true, salaryHistoryList);
        }
        return new Response("Authorization empty!", false);
    }

    @Override
    public Response getSalariesByUserId(UUID id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && !authentication.getPrincipal().equals("anonymousUser")) {
            User user = (User) authentication.getPrincipal();
            Set<Role> roles = user.getRoles();

            boolean checkRole = false;
            for (Role role : roles) {
                if (role.getRoleName().name().equals("DIRECTOR") || role.getRoleName().name().equals("HR_MANAGER")) {
                    checkRole = true;
                    break;
                }
            }
            if (!checkRole)
                return new Response("You don't have access for this operation", false);
            List<SalaryHistory> salaryHistoryList = salaryHistoryRepository.findAllByEmployeeId(id);
            if (salaryHistoryList.size() == 0)
                return new Response("Such employee did not get salary!", false);

            return new Response("Success!", true, salaryHistoryList);
        }
        return new Response("Authorization empty!", false);
    }
}
