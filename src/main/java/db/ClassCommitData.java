package db;

public class ClassCommitData {
    public int classCommitDataid;
    public int refactoringClassId;
    public String commitDate;
    public boolean isCreated;
    public boolean isExtractCommit;

    public ClassCommitData(
            String commitDate,
            boolean isCreated,
            boolean isExtractCommit
    ) {
        this.commitDate = commitDate;
        this.isCreated = isCreated;
        this.isExtractCommit = isExtractCommit;
    }
}
