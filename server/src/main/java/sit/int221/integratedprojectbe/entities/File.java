package sit.int221.integratedprojectbe.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "file")
public class File {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @Column
    private String name;

    @Column
    private String type;

    @Lob
    @Column
    private byte[] data;

    @Column
    private Long size;

    @OneToOne(mappedBy = "file", fetch = FetchType.LAZY)
    private Event event;
}
