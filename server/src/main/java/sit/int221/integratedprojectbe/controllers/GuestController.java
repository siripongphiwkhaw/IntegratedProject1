package sit.int221.integratedprojectbe.controllers;


import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sit.int221.integratedprojectbe.dtos.CreateEventDTO;
import sit.int221.integratedprojectbe.dtos.EventDetailsDTO;
import sit.int221.integratedprojectbe.entities.Event;
import sit.int221.integratedprojectbe.entities.EventCategory;
import sit.int221.integratedprojectbe.exceptions.ArgumentNotValidException;
import sit.int221.integratedprojectbe.exceptions.DateTimeOverlapException;
import sit.int221.integratedprojectbe.repositories.EventRepository;
import sit.int221.integratedprojectbe.services.EmailService;
import sit.int221.integratedprojectbe.services.EventCategoryService;
import sit.int221.integratedprojectbe.services.EventService;

import javax.validation.Valid;
import java.io.IOException;

@RestController
@RequestMapping("/api/guests")
public class GuestController {
    @Autowired
    private EventService eventService;

    @PostMapping(value = "")
    @ResponseStatus(HttpStatus.CREATED)
    public EventDetailsDTO addEventGuest(
            @Valid @ModelAttribute CreateEventDTO newEvent,
            @RequestParam(name="file", required = false) MultipartFile file,
            BindingResult bindingResult) throws IOException {
        try {
            return eventService.addNewEvent(newEvent, file, bindingResult);
        } catch (DateTimeOverlapException ex) {
            FieldError error = new FieldError("createEventDTO", "eventStartTime", ex.getMessage());
            bindingResult.addError(error);
            throw new ArgumentNotValidException(bindingResult);
        } catch (ArgumentNotValidException ex) {
            throw ex;
        }
    }
}
