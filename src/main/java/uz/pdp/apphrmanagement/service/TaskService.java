package uz.pdp.apphrmanagement.service;

import org.springframework.stereotype.Service;
import uz.pdp.apphrmanagement.dto.TaskDto;
import uz.pdp.apphrmanagement.entity.Response;

import java.util.UUID;

@Service
public interface TaskService {

    Response save(TaskDto taskDto);

    Response completeTask(Integer id, Integer taskStatus);

    Response checkEmployeeTask(UUID employeeId, Integer status);

}
