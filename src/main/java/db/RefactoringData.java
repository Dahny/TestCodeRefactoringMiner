package db;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "refactoringData")
public class RefactoringData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refactoringDataId")
    private Long RefactoringDataId;

    @OneToOne(mappedBy = "refactoringData")
    private ExtractMethod extractMethodId;

    @OneToMany(mappedBy = "refactoringData")
    private Set<ClassCommitData> classCommitDataid;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "projectId", nullable = false)
    private Project project;

    @Column(name = "fileLoc")
    public String fileLoc;

    @Column(name = "className")
    public String className;

    @Column(name = "commitId")
    public String commitId;

    @Column(name = "refactoringType")
    public String refactoringType;

    @Column(name = "refactoringSummary")
    public String refactoringSummary;

    @Column(name = "commitMessage")
    public String commitMessage;

    @Column(name = "commitDate")
    public int commitDate;

    public RefactoringData(Project project){
        this.project = project;
    }

    public RefactoringData(
                       String fileLoc,
                       String className,
                       String commitId,
                       String refactoringSummary,
                       String refactoringType,
                       String commitMessage,
                       int commitDate,
                       Project project){
        this.className = className;
        this.fileLoc = fileLoc;
        this.commitId = commitId;
        this.refactoringType = refactoringType;
        this.refactoringSummary = refactoringSummary;
        this.commitMessage = commitMessage;
        this.commitDate = commitDate;
        this.project = project;
    }

    public Project getProject(){
        return project;
    }
}