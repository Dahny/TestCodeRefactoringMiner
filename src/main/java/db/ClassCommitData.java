package db;

import javax.persistence.Table;
import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "classCommitData")
public class ClassCommitData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "classCommitDataid")
    private Long classCommitDataid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "extractMethodId", nullable = false)
    private ExtractMethod extractMethod;

    @Column(name = "commitDate")
    private Date commitDate;

    @Column(name = "isCreated")
    private boolean isCreated;

    @Column(name = "isExtractCommit")
    private boolean isExtractCommit;

    public ClassCommitData() { }

    public ClassCommitData(
            Date commitDate,
            boolean isCreated,
            boolean isExtractCommit
    ) {
        this.commitDate = commitDate;
        this.isCreated = isCreated;
        this.isExtractCommit = isExtractCommit;
    }


    public void setExtractMethod(ExtractMethod extractMethod) {
        this.extractMethod = extractMethod;
    }

    @Override
    public String toString(){
        return "[commitDate: " + commitDate + " isCreated: " + isCreated + " isExtractCommit: " + isExtractCommit + "]";
    }
}
