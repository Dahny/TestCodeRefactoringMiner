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

    @Column(name="metaDataId")
    @JoinColumn(name="metaDataId")
    private MetaData metaData;

    @Column(name = "projectName")
    private String projectName;

    // TODO add more project info

    public Project (){ }

    public Project(String name) {
        this.projectName = name;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public MetaData getMetaData() {
        return metaData;
    }

    public void setMetaData(MetaData metaData) {
        this.metaData = metaData;
    }

    public void addToSkippedRefactoringCount() {
        metaData.addToSkippedRefactoringCount();
    }

    public void addToExceptionCount() {
        metaData.addToExceptionCount();
    }
}
