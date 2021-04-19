package uz.pdp.apphrmanagement.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uz.pdp.apphrmanagement.dto.TaskDto;
import uz.pdp.apphrmanagement.entity.Response;
import uz.pdp.apphrmanagement.entity.Role;
import uz.pdp.apphrmanagement.entity.Task;
import uz.pdp.apphrmanagement.entity.User;
import uz.pdp.apphrmanagement.entity.enums.TaskStatus;
import uz.pdp.apphrmanagement.repository.TaskRepository;
import uz.pdp.apphrmanagement.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class TaskServiceImpl implements TaskService {

    final TaskRepository taskRepository;
    final UserRepository userRepository;

    public TaskServiceImpl(TaskRepository taskRepository,
                           UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Response save(TaskDto taskDto) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && !authentication.getPrincipal().equals("anonymousUser")) {
            User user = (User) authentication.getPrincipal();
            Set<Role> roles = user.getRoles();

            int role_index = 0;
            for (Role role : roles) {
                switch (role.getRoleName().name()) {
                    case "DIRECTOR":
                        role_index = 1;
                        break;
                    case "HR_MANAGER":
                    case "MANAGER":
                        role_index = 2;
                        break;
                    default:
                        return new Response("Such role id not found!", false);
                }
            }

            Optional<User> optionalResponsive = userRepository.findById(taskDto.getResponsibleId());
            if (!optionalResponsive.isPresent())
                return new Response("Such responsive id not found!", false);

            Set<Role> responsiveRoles = optionalResponsive.get().getRoles();

            int role_index2 = 0;

            for (Role responsiveRole : responsiveRoles) {
                switch (responsiveRole.getRoleName().name()) {
                    case "DIRECTOR":
                        role_index2 = 1;
                        break;
                    case "HR_MANAGER":
                    case "MANAGER":
                        role_index2 = 2;
                        break;
                    case "EMPLOYEE":
                        role_index2 = 3;
                        break;
                    default:
                        return new Response("Such responsive role id not found!", false);
                }
            }

            boolean checkSendTaskStatus = false;

            if (role_index == 1 && role_index2 != 1) {
                checkSendTaskStatus = true;
            }

            if (role_index == 2 && role_index2 != 1 && role_index2 != 2) {
                checkSendTaskStatus = true;
            }

            if (!checkSendTaskStatus)
                return new Response("You can not assign the task to this user!", false);

            Task task = new Task();
            task.setTitle(taskDto.getTitle());
            task.setBody(taskDto.getBody());
            task.setResponsible(optionalResponsive.get());
            task.setStatus(TaskStatus.NEW);
            task.setDeadLine(taskDto.getDeadLine());

            taskRepository.save(task);
            return new Response("Task assigned! From: "
                    + user.getFirstname() + " " + user.getLastname() + " To: "
                    + optionalResponsive.get().getFirstname() + " " + optionalResponsive.get().getLastname(), true);

        }
        return new Response("Authorization empty!", false);
    }

    @Override
    public Response completeTask(Integer id, Integer taskStatus) {

        Optional<Task> optionalTask = taskRepository.findById(id);
        if (!optionalTask.isPresent())
            return new Response("Such task id not found!", false);

        TaskStatus status = TaskStatus.values()[taskStatus];
        optionalTask.get().setStatus(status);

        taskRepository.save(optionalTask.get());
        return new Response("Task status updated!", true);
    }

    @Override
    public Response checkEmployeeTask(UUID employeeId, Integer status) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && !authentication.getPrincipal().equals("anonymousUser")) {
            User user = (User) authentication.getPrincipal();
            Set<Role> roles = user.getRoles();

            boolean checkRoleStatus = false;
            for (Role role : roles) {
                if (role.getRoleName().name().equals("DIRECTOR") || role.getRoleName().name().equals("HR_MANAGER")) {
                    checkRoleStatus = true;
                    break;
                }
            }

            if (!checkRoleStatus)
                return new Response("You don't have access for this operation!", false);

            List<Task> taskList = taskRepository.findAllByStatusAndResponsibleId(TaskStatus.values()[status], employeeId);
            if(taskList.size() == 0)
                return new Response("There is not any task for this data!", false);

            return new Response("Success!", true, taskList);

        }

        return new Response("Authorization empty!", false);
    }
}
