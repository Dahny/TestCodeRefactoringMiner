package db;

import javax.persistence.*;
import java.util.Date;
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

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "projectId", nullable = false)
    private Project project;

    @Column(name = "fileLoc")
    private String fileLoc;

    @Column(name = "className")
    private String className;

    @Column(name = "commitId")
    private String commitId;

    @Column(name = "parentCommitId")
    private String parentCommitId;

    @Column(name = "refactoringType")
    private String refactoringType;

    @Column(name = "refactoringSummary")
    private String refactoringSummary;

    @Column(name = "commitMessage")
    private String commitMessage;

    @Column(name = "commitDate")
    private Date commitDate;


    public RefactoringData() {}

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
                       Date commitDate,
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

    public String getFileLoc() {
        return fileLoc;
    }

    public void setFileLoc(String fileLoc) {
        this.fileLoc = fileLoc;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public String getParentCommitId() {
        return parentCommitId;
    }

    public void setParentCommitId(String parentCommitId) {
        this.parentCommitId = parentCommitId;
    }

    public String getRefactoringType() {
        return refactoringType;
    }

    public void setRefactoringType(String refactoringType) {
        this.refactoringType = refactoringType;
    }

    public String getRefactoringSummary() {
        return refactoringSummary;
    }

    public void setRefactoringSummary(String refactoringSummary) {
        this.refactoringSummary = refactoringSummary;
    }

    public String getCommitMessage() {
        return commitMessage;
    }

    public void setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }

    public Date getCommitDate() {
        return commitDate;
    }

    public void setCommitDate(Date commitDate) {
        this.commitDate = commitDate;
    }
}