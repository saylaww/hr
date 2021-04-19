package uz.pdp.apphrmanagement.service;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import uz.pdp.apphrmanagement.dto.SalaryDto;
import uz.pdp.apphrmanagement.entity.EmpResponse;
import uz.pdp.apphrmanagement.entity.Response;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public interface EmployeeService {

    Response findAllEmployees();

    EmpResponse findOneByData(UUID id, Timestamp start, Timestamp end); // Berilgan id li xodimning belgilangan oraliq vaqt bo’yicha ishga kelib-ketishi va bajargan tasklari haqida

    Response payMonthly(SalaryDto salaryDto);    // Oylik maoshi berish

    Response getSalariesByMonth(String year, Integer monthNumber);

    Response getSalariesByUserId(UUID id);

}
