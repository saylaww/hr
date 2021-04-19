package uz.pdp.apphrmanagement.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmpResponse {

    /*
    *
    * Har bir xodim haqidagi ma’lumotlarni ko’rmochi bo’lsa ushbu xodimning belgilangan oraliq
    * vaqt bo’yicha ishga kelib-ketishi va bajargan tasklari haqida ma’lumot chiqishi kerak
    *
    * */
    private String message;
    private boolean success;
    private List<Turniket> turniketList;
    private List<Task> taskList;

    public EmpResponse(String message, boolean success) {
        this.message = message;
        this.success = success;
    }
}
