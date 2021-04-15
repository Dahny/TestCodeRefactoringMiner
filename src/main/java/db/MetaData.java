package db;

import javax.persistence.*;

@Entity
@Table(name="metaData")
public class MetaData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "metaDataId")
    private Long metaDataId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "project")
    private Project project;

    @Column(name="skippedRefactoringCount")
    private int skippedRefactoringCount;

    @Column(name="exceptionCount")
    private int exceptionCount;

    public MetaData(){
        skippedRefactoringCount = 0;
        exceptionCount = 0;
    }

    public void addToSkippedRefactoringCount() {
        skippedRefactoringCount += 1;
    }

    public void addToExceptionCount() {
        exceptionCount += 1;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
