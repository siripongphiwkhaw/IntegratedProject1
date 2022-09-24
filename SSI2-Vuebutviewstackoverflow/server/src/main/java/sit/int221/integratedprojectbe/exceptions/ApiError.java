package sit.int221.integratedprojectbe.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import java.time.ZonedDateTime;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class ApiError {
    private ZonedDateTime timestamp;
    private Integer status;
    private String path;
    private String error;
    private String message;

    public ApiError (HttpStatus status, HttpServletRequest request, String message) {
        this.timestamp = ZonedDateTime.now();
        this.status = status.value();
        this.path = request.getRequestURI();
        this.error = status.getReasonPhrase();
        this.message = message;
    }
}
