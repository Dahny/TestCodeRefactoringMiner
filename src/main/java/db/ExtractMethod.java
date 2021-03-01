package db;

import javax.persistence.*;
import java.sql.Ref;

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

    public void setWmcWholeClass(int wmcAfter) {
        this.wmcWholeClass = wmcAfter;
    }

    public void setWmcExtractedLines(int wmcBefore) {
        this.wmcExtractedLines = wmcBefore;
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

    public RefactoringData getRefactoringData(){
        return this.refactoringData;
    }
}
