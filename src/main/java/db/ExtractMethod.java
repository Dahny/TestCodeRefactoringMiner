package db;

import java.sql.Ref;
import java.util.Date;

public class ExtractMethod extends RefactoringData {

    public String methodName;
    public int extractedLines;
    public int extractedMethodLoc;
    public int newMethodLoc;
    public int wmcBefore;
    public int wmcAfter;
    public Boolean hasAssertInvolved;
    public String typeOfReplacement;

    public ExtractMethod(){
        this.hasAssertInvolved = false;
    }

    public ExtractMethod(int extractedLines,
                         int extractedMethodLoc,
                         int newMethodLoc,
                         String commitId,
                         String refactoringName,
                         String className,
                         String methodName,
                         String refactoringSummary,
                         boolean hasAssertInvolved,
                         int commitDate){
        super(commitId, refactoringName, className, refactoringSummary, commitDate);
        this.methodName = methodName;
        this.extractedLines = extractedLines;
        this.extractedMethodLoc = extractedMethodLoc;
        this.newMethodLoc = newMethodLoc;
        this.hasAssertInvolved = hasAssertInvolved;
    }

    public ExtractMethod(
            RefactoringData refactoringData
    ) {
        super();
        this.commitId = refactoringData.commitId;
        this.commitDate = refactoringData.commitDate;
        this.className = refactoringData.className;
        this.refactoringType = refactoringData.refactoringType;
        this.refactoringSummary = refactoringData.refactoringSummary;
    }

    public void setHasAssertInvolved(Boolean hasAssertInvolved) {
        this.hasAssertInvolved = hasAssertInvolved;
    }

    public void setWmcAfter(int wmcAfter) {
        this.wmcAfter = wmcAfter;
    }

    public void setWmcBefore(int wmcBefore) {
        this.wmcBefore = wmcBefore;
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

    public void setNewMethodLoc(int newMethodLoc) {
        this.newMethodLoc = newMethodLoc;
    }

    public void setTypeOfReplacment(String typeOfReplacement) {
        this.typeOfReplacement = typeOfReplacement;
    }
}
