package sit.int221.integratedprojectbe.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sit.int221.integratedprojectbe.entities.Event;

import java.util.List;
import java.util.Optional;


public interface EventRepository extends JpaRepository<Event, Integer> {
        List<Event> findAllByOrderByEventStartTimeDesc();

        @Query(
                value ="SELECT * FROM event WHERE categoryId = :categoryId ORDER BY categoryId DESC",
                nativeQuery = true
        )
        List<Event> findAllByCategoryId(Integer categoryId);
        Optional<Event> findByBookingIdAndBookingEmail (Integer bookingId, String bookingEmail);

        @Query(
                value = "SELECT * FROM event WHERE DATE_ADD(eventStartTime, INTERVAL eventDuration MINUTE) >= NOW() ORDER BY eventStartTime DESC",
                nativeQuery = true
        )
        List<Event> findAllByEventStartTimeAfter();

        @Query(
                value = "SELECT * FROM event WHERE categoryId = :categoryId AND DATE_ADD(eventStartTime, INTERVAL eventDuration MINUTE) >= NOW() ORDER BY eventStartTime DESC",
                nativeQuery = true
        )
        List<Event> findAllByCategoryIdAndEventStartTimeAfter(Integer categoryId);

        @Query(
                value = "SELECT * FROM event WHERE DATE_ADD(eventStartTime, INTERVAL eventDuration MINUTE) <= NOW() ORDER BY eventStartTime DESC",
                nativeQuery = true
        )
        List<Event> findAllByEventStartTimeBefore();

        @Query(
                value = "SELECT * FROM event WHERE DATE(eventStartTime) LIKE concat(:eventDate, '%') ORDER BY eventStartTime DESC",
                nativeQuery = true
        )
        List<Event> findAllByEventStartTimeEquals(@Param("eventDate") String eventDate);

        List<Event> findAllByBookingEmail(String bookingEmail);

        @Query(
                value = "select e.* from `event` e join `event_category_has_user` eco on e.categoryId = eco.categoryId join `user` u on eco.userId = u.userId where u.userId =:userId",
                nativeQuery = true
        )
        List<Event> findAllEventOfOwnerCategoryByUserId(@Param("userId") Integer userId);

        @Query(
                value = "select e.* from `event` e join `event_category_has_user` eco on e.categoryId = eco.categoryId join `user` u on eco.userId = u.userId where e.bookingId =:bookingId and u.userId =:userId",
                nativeQuery = true
        )
        Optional<Event> findEventOfOwnerCategoryByUserIdAndBookingId(@Param("userId") Integer userId,@Param("bookingId") Integer bookingId);

        boolean existsByBookingIdAndBookingEmail(Integer bookingId, String bookingEmail);
}