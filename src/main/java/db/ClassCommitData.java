package db;

import javax.persistence.Table;
import javax.persistence.*;

@Entity
@Table(name = "classCommitData")
public class ClassCommitData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "classCommitDataid")
    public Long classCommitDataid;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "refactoringDataId", nullable = false)
    public RefactoringData refactoringData;

    @Column(name = "commitDate")
    public String commitDate;

    @Column(name = "isCreated")
    public boolean isCreated;

    @Column(name = "isExtractCommit")
    public boolean isExtractCommit;

    public ClassCommitData() { }

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
