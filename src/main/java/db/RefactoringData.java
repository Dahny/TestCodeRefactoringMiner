package db;

import java.util.Date;

public class RefactoringData {
    public int RefactoringDataId;
    public String commitId;
    public String refactoringType;
    public String refactoringSummary;
    public String commitMessage;
    public int commitDate;

    public RefactoringData(){
    }

    public RefactoringData(
                       String commitId,
                       String refactoringSummary,
                       String refactoringType,
                       String commitMessage,
                       int commitDate){
        this.commitId = commitId;
        this.refactoringType = refactoringType;
        this.refactoringSummary = refactoringSummary;
        this.commitMessage = commitMessage;
        this.commitDate = commitDate;
    }
}