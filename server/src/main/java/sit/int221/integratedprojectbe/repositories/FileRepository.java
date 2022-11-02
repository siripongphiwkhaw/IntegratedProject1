package sit.int221.integratedprojectbe.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sit.int221.integratedprojectbe.entities.File;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<File, String> {
}
