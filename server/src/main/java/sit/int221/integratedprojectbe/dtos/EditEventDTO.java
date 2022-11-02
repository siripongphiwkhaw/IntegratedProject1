package sit.int221.integratedprojectbe.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Size;
import java.util.Date;

import javax.validation.constraints.*;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EditEventDTO {
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date eventStartTime;

    @Size(max = 500, message = "size must be between 0 and 500")
    private String eventNotes;
}
