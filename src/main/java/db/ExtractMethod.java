package db;

import javax.persistence.*;
import java.sql.Ref;
import java.util.Set;

@Entity
@Table(name = "extractMethod")
public class ExtractMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "extractMethodId")
    private Long extractMethodId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "refactoringDataid")
    private RefactoringData refactoringData;

    @OneToMany(fetch=FetchType.EAGER, mappedBy = "extractMethod")
    private Set<ClassCommitData> classCommitData;

    @Column(name = "methodName")
    public String methodName;

    @Column(name = "extractedLines")
    public int extractedLines;

    @Column(name = "extractedMethodLoc")
    public int extractedMethodLoc;

    @Column(name = "wmcExtractedLines")
    public int wmcExtractedLines;

    @Column(name = "wmcWholeClass")
    public int wmcWholeClass;

    @Column(name = "hasAssertInvolved")
    public Boolean hasAssertInvolved;

    @Column(name = "typeOfReplacement")
    public String typeOfReplacement;

    public ExtractMethod(){
        this.hasAssertInvolved = false;
    }

    public ExtractMethod(RefactoringData refactoringData) {
        this.hasAssertInvolved = false;
        this.refactoringData = refactoringData;
    }


    public void setHasAssertInvolved(Boolean hasAssertInvolved) {
        this.hasAssertInvolved = hasAssertInvolved;
    }

    public void setWmcWholeClass(int wmc) {
        this.wmcWholeClass = wmc;
    }

    public void setWmcExtractedLines(int wmc) {
        this.wmcExtractedLines = wmc;
    }

    public void setExtractedLines(int extractedLines) {
        this.extractedLines = extractedLines;
    }

    public void setExtractedMethodLoc(int extractedMethodLoc) {
        this.extractedMethodLoc = extractedMethodLoc;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void setTypeOfReplacment(String typeOfReplacement) {
        this.typeOfReplacement = typeOfReplacement;
    }

    public void setClassCommitData(Set<ClassCommitData> ccd) { this.classCommitData = ccd; }

    public RefactoringData getRefactoringData(){
        return this.refactoringData;
    }

    public Set<ClassCommitData> getClassCommitData() { return this.classCommitData; }
}
