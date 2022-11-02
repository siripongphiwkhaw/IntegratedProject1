package sit.int221.integratedprojectbe.controllers;

import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import sit.int221.integratedprojectbe.dtos.CreateEventDTO;
import sit.int221.integratedprojectbe.dtos.EditEventDTO;
import sit.int221.integratedprojectbe.dtos.EventDetailsDTO;
import sit.int221.integratedprojectbe.entities.Event;
import sit.int221.integratedprojectbe.entities.File;
import sit.int221.integratedprojectbe.exceptions.ArgumentNotValidException;
import sit.int221.integratedprojectbe.exceptions.DateTimeOverlapException;

import sit.int221.integratedprojectbe.imp.MyUserDetails;
import sit.int221.integratedprojectbe.services.EventService;
import sit.int221.integratedprojectbe.services.FileService;

import javax.annotation.Resource;
import javax.mail.Multipart;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {
    @Autowired
    private EventService eventService;
    @Autowired
    private FileService fileService;

    @GetMapping("")
    public List<EventDetailsDTO> getAllEvents(
            Authentication auth,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String eventDate)
    {
        MyUserDetails myUserDetails = (MyUserDetails) auth.getPrincipal();

        if (myUserDetails.getAuthorities().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No Authorities");
        }
        if (myUserDetails.hasRole("LECTURER")) {
            return eventService.getAllEventByOwnerCategory(myUserDetails.getUserId());
        }
        if (myUserDetails.hasRole("ADMIN")) {
            if (sort != null) {
                if (sort.equals("past")) {
                    return eventService.getAllPastEvent();
                }
                if (sort.equals("upcoming")) {
                    return eventService.getAllFutureEvent();
                }
                if (sort.equals("date") && eventDate != null) {
                    return eventService.getAllEventByDate(eventDate);
                }
                if (sort.equals("category")) {
                    return eventService.getAllEventsByCategoryId(categoryId);
                }
            }

            return eventService.getEvents();
        }

        return eventService.getAllEventByBookingEmail(myUserDetails.getUsername());
    }

    @GetMapping("/{bookingId}")
    public EventDetailsDTO getEventsByBookingId(Authentication auth, @PathVariable Integer bookingId){
        MyUserDetails myUserDetails = (MyUserDetails) auth.getPrincipal();
        if (myUserDetails.hasRole("STUDENT")) {
            return eventService.getOwnedEventByEmail(bookingId, myUserDetails.getUsername());
        }
        if (myUserDetails.hasRole("LECTURER")) {
            return eventService.getEventOfOwnerCategoryById(bookingId, myUserDetails.getUserId());
        }
        return eventService.getEventById(bookingId);
    }

    @GetMapping(value = "/{bookingId}/file")
    public HttpEntity<byte[]> downloadFile (@PathVariable Integer bookingId) {
        EventDetailsDTO event = eventService.getEventById(bookingId);
        File file = fileService.getFile(event.getFile().getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(file.getType()));
        headers.setContentDispositionFormData("attachment", file.getName());

        return new HttpEntity<>(file.getData(), headers);
    }

    @PostMapping(value = "")
    @ResponseStatus(HttpStatus.CREATED)
    public EventDetailsDTO create(
            Authentication auth,
            @Valid @ModelAttribute CreateEventDTO newEvent,
            @RequestParam(name="file", required = false) MultipartFile file,
            BindingResult bindingResult) throws IOException {
        try {
            MyUserDetails myUserDetails = (MyUserDetails) auth.getPrincipal();

            if (myUserDetails.hasRole("STUDENT") && !myUserDetails.getUsername().equals(newEvent.getBookingEmail())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid Email");
            }

            return eventService.addNewEvent(newEvent, file, bindingResult);
        } catch (DateTimeOverlapException ex) {
            FieldError error = new FieldError("createEventDTO", "eventStartTime", ex.getMessage());
            bindingResult.addError(error);
            throw new ArgumentNotValidException(bindingResult);
        } catch (ArgumentNotValidException ex) {
            throw ex;
        }


    }
    
    @DeleteMapping("/{bookingId}")
    public void delete(Authentication auth, @PathVariable Integer bookingId) throws IOException {
        MyUserDetails myUserDetails = (MyUserDetails) auth.getPrincipal();

        if (myUserDetails.hasRole("STUDENT") && !eventService.isOwnedEvent(bookingId, myUserDetails.getUsername())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    String.format("You are not owned this event; booking id %s", bookingId)
            );
        }
        eventService.removeEvent(bookingId);
    }

    @PatchMapping("/{bookingId}")
    public EventDetailsDTO update(Authentication auth, @Valid @ModelAttribute EditEventDTO editEvent,
                                  @RequestParam(name="file", required = false) MultipartFile file,
                                  BindingResult bindingResult, @PathVariable Integer bookingId) throws IOException {
        try {
            return eventService.editEvent(auth, bookingId, editEvent, file, bindingResult);
        } catch (DateTimeOverlapException ex) {
            FieldError error = new FieldError("editEventDTO", "eventStartTime", "overlap");
            bindingResult.addError(error);
            throw new ArgumentNotValidException(bindingResult);
        } catch (ArgumentNotValidException ex) {
            throw ex;
        } catch (IOException ex) {
            throw ex;
        }
    }
}
