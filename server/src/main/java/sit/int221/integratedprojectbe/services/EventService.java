package sit.int221.integratedprojectbe.services;


import de.mkammerer.argon2.Argon2;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;


import sit.int221.integratedprojectbe.dtos.*;
import sit.int221.integratedprojectbe.entities.Event;
import sit.int221.integratedprojectbe.entities.EventCategory;
import sit.int221.integratedprojectbe.entities.File;
import sit.int221.integratedprojectbe.exceptions.ArgumentNotValidException;
import sit.int221.integratedprojectbe.exceptions.DateTimeOverlapException;
import sit.int221.integratedprojectbe.imp.MyUserDetails;
import sit.int221.integratedprojectbe.repositories.EventRepository;
import sit.int221.integratedprojectbe.repositories.UserRepository;
import sit.int221.integratedprojectbe.utils.ListMapper;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class EventService {
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private EventCategoryService eventCategoryService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private ListMapper listMapper;
    @Autowired
    private Argon2 argon2Factory;

    @Autowired
    private EmailService emailService;
    @Autowired
    private FileService fileService;

    public List<EventDetailsDTO> getEvents() {

        return listMapper.mapList(eventRepository.findAllByOrderByEventStartTimeDesc(), EventDetailsDTO.class, modelMapper);
    }

    public List<EventDetailsDTO> getAllEventsByCategoryId(Integer categoryId) {
        return listMapper.mapList(eventRepository.findAllByCategoryId(categoryId), EventDetailsDTO.class, modelMapper);
    }
    public List<EventDetailsDTO> getAllEventByDate (String eventDate){
        return   listMapper.mapList(eventRepository.findAllByEventStartTimeEquals(eventDate), EventDetailsDTO.class, modelMapper);
    }

    public List<EventDetailsDTO> getAllFutureEvent(){
        return listMapper.mapList(eventRepository.findAllByEventStartTimeAfter(), EventDetailsDTO.class, modelMapper);
    }

    public List<EventDetailsDTO> getAllPastEvent(){
        return listMapper.mapList(eventRepository.findAllByEventStartTimeBefore(), EventDetailsDTO.class, modelMapper);
    }

    public List<EventDetailsDTO> getAllEventByBookingEmail(String email) {
        return listMapper.mapList(eventRepository.findAllByBookingEmail(email), EventDetailsDTO.class, modelMapper);
    }
    public List<EventDetailsDTO> getAllEventByOwnerCategory(Integer userId) {
        return listMapper.mapList(eventRepository.findAllEventOfOwnerCategoryByUserId(userId), EventDetailsDTO.class, modelMapper);
    }

    public EventDetailsDTO getOwnedEventByEmail (Integer bookingId, String userEmail) {
        Event event = eventRepository.findByBookingIdAndBookingEmail(bookingId, userEmail).orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Booking ID %s with an email %s is doesn't exist.", bookingId, userEmail)
                ));
        return modelMapper.map(event, EventDetailsDTO.class);
    }

    public EventDetailsDTO getEventOfOwnerCategoryById(Integer bookingId, Integer userId) {
        Event event = eventRepository.findEventOfOwnerCategoryByUserIdAndBookingId(userId, bookingId).orElseThrow(() ->
            new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "This Booking not exist or Category mismatch"
        ));
        return modelMapper.map(event, EventDetailsDTO.class);
    }

    public EventDetailsDTO getEventById(Integer bookingId) {
        Event event = eventRepository.findById(bookingId).orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Booking ID %s is doesn't exist.", bookingId)
                ));
        return modelMapper.map(event, EventDetailsDTO.class);
    }

    public EventDetailsDTO addNewEvent(CreateEventDTO newEvent, MultipartFile file, BindingResult bindingResult) throws IOException {;
        if (bindingResult.hasErrors()) throw new ArgumentNotValidException(bindingResult);

        Event event = modelMapper.map(newEvent, Event.class);
        EventCategory eventCategory = eventCategoryService.getCategoryById(newEvent.getCategoryId());
        event.setCategory(eventCategory);
        event.setEventDuration(eventCategory.getEventDuration());

        if(file != null && !file.isEmpty()){
            try {
                File newFile = fileService.store(file);
                event.setFile(newFile);
            } catch (SizeLimitExceededException ex) {
                FieldError error = new FieldError(
                        "createEventDto",
                        "file",
                        "File size is exceed maximum 10Mb");
                bindingResult.addError(error);
            }
        }

        boolean isOverlap = checkEventPeriodOverlap(event);
        if (isOverlap) {
            throw new DateTimeOverlapException("This time is already reserve");
        }

        EventDetailsDTO eventDTO = modelMapper.map(eventRepository.saveAndFlush(event), EventDetailsDTO.class);
        emailService.sendSimpleMessage(eventDTO);

        return modelMapper.map(eventRepository.saveAndFlush(event), EventDetailsDTO.class);
    }

    public void removeEvent(Integer bookingId) throws IOException {
       Event event= eventRepository.findById(bookingId).orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Booking ID %s is doesn't exist", bookingId)
                ));
       if(event.getFile() != null){
          fileService.deleteById(event.getFile().getId());
       }
        eventRepository.deleteById(bookingId);
    }

    public EventDetailsDTO editEvent(Authentication auth, Integer bookingId, EditEventDTO updateEvent, MultipartFile file, BindingResult bindingResult) throws IOException {
        MyUserDetails myUserDetails = (MyUserDetails) auth.getPrincipal();
        Event event = null;
        if (myUserDetails.hasRole("STUDENT")) {
            event = eventRepository.findByBookingIdAndBookingEmail(bookingId, myUserDetails.getUsername())
                    .map(existingEvent -> mapEvent(existingEvent, updateEvent))
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            String.format("Booking ID %s with an email %s is doesn't exist.", bookingId, myUserDetails.getUsername())
                    ));
        } else {
            event = eventRepository.findById(bookingId)
                    .map(existingEvent -> mapEvent(existingEvent, updateEvent))
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            String.format("Booking ID %s is doesn't exist.", bookingId)
                    ));
        }

        boolean isOverlap = checkEventPeriodOverlap(event);
        if (isOverlap) {
            throw new DateTimeOverlapException("This time is already reserve");
        }

        if (file == null && event.getFile() != null) {
            fileService.deleteById(event.getFile().getId());
            event.setFile(null);
        } else if (file != null) {
            File updatedFile;
            if (event.getFile() != null) {
                updatedFile = fileService.replace(event.getFile().getId(), file);
            } else {
                updatedFile = fileService.store(file);
            }

            event.setFile(updatedFile);
        }

        if (bindingResult.hasErrors()) {
            throw new ArgumentNotValidException(bindingResult);
        }

        return modelMapper.map(eventRepository.saveAndFlush(event), EventDetailsDTO.class);
    }

    public boolean isOwnedEvent(Integer bookingId, String email) {
        return eventRepository.existsByBookingIdAndBookingEmail(bookingId, email);
    }

    private Event mapEvent(Event existingEvent, EditEventDTO updateEvent) {
        if (updateEvent.getEventStartTime() != null) {
            existingEvent.setEventStartTime(updateEvent.getEventStartTime());
        }
        if (updateEvent.getEventNotes() != null) {
            existingEvent.setEventNotes(updateEvent.getEventNotes());
        }
        return existingEvent;
    }

    private boolean checkEventPeriodOverlap(Event event) {
        List<Event> categoryEvents = eventRepository
                .findAllByCategoryIdAndEventStartTimeAfter(event.getCategory().getCategoryId());

        Instant eventCheckStart = event.getEventStartTime().toInstant();
        Instant eventCheckEnd = event.getEventStartTime().toInstant().plus(event.getEventDuration(), ChronoUnit.MINUTES);

        return categoryEvents.stream().anyMatch(e -> {
            if (event.getBookingId() == null || !event.getBookingId().equals(e.getBookingId())) {
                Instant eventStartTime = e.getEventStartTime().toInstant();
                Instant eventEndTime = e.getEventStartTime().toInstant().plus(e.getEventDuration(), ChronoUnit.MINUTES);
                return isEventPeriodOverlap(eventStartTime, eventEndTime, eventCheckStart, eventCheckEnd);
            }
            return false;
        });
    }

    private boolean isEventPeriodOverlap(Instant startTimeA , Instant endTimeA, Instant startTimeB, Instant endTimeB){
        return startTimeA.isBefore(endTimeB) && startTimeB.isBefore(endTimeA);
    }
}





