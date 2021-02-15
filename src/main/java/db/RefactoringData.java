package db;

import java.util.Date;

public class RefactoringData {
    public String commitId;
    public String refactoringType;
    public String refactoringSummary;
    public String className;
    public int commitDate;

    public RefactoringData(){
    }

    public RefactoringData(
                       String commitId,
                       String className,
                       String refactoringSummary,
                       String refactoringType,
                       int commitDate){
        this.commitId = commitId;
        this.refactoringType = refactoringType;
        this.refactoringSummary = refactoringSummary;
        this.className = className;
        this.commitDate = commitDate;
    }
}