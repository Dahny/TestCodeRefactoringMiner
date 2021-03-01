package db;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "project")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "projectId")
    private Long projectId;

    @OneToMany(mappedBy = "project")
    private Set<RefactoringData> RefactoringData;

    @Column(name = "projectName")
    public String projectName;

    // TODO add more project info

    public Project(String name){
        this.projectName = name;
    }

}
