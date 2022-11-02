package sit.int221.integratedprojectbe.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int221.integratedprojectbe.entities.EventCategoryHasUser;
import sit.int221.integratedprojectbe.entities.EventCategoryHasUserKey;

public interface EventCategoryHasUserRepository extends JpaRepository<EventCategoryHasUser, EventCategoryHasUserKey> {

}