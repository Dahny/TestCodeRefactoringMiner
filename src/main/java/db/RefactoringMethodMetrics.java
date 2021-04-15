package db;

import javax.persistence.*;

@Entity
@Table(name="refactoringMethodMetrics")
public class RefactoringMethodMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RefactoringMethodMetricsId")
    private Long RefactoringMethodMetricsId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "extractMethodId", nullable = false)
    private ExtractMethod extractMethod;

    @Column(name="isBefore")
    private boolean isBefore;

    @Column(name="methodName")
    private String methodName;

    @Column(name="loc")
    private int loc;

    @Column(name="complexity")
    private int complexity;

    @Column(name="coupling")
    private int coupling;

    @Column(name="involvesAssertion")
    private boolean involvesAssertion;

    public RefactoringMethodMetrics() {}

    public int getLoc() {
        return loc;
    }

    public void setLoc(int loc) {
        this.loc = loc;
    }

    public int getComplexity() {
        return complexity;
    }

    public void setComplexity(int complexity) {
        this.complexity = complexity;
    }

    public int getCoupling() {
        return coupling;
    }

    public void setCoupling(int coupling) {
        this.coupling = coupling;
    }

    public boolean isInvolvesAssertion() {
        return involvesAssertion;
    }

    public void setInvolvesAssertion(boolean involvesAssertion) {
        this.involvesAssertion = involvesAssertion;
    }

    public String getName() {
        return methodName;
    }

    public void setMethodName(String name) {
        this.methodName = name;
    }

    public boolean isBefore() {
        return isBefore;
    }

    public void setBefore(boolean before) {
        this.isBefore = before;
    }

    public void setExtractMethod(ExtractMethod extractMethod) {
        this.extractMethod = extractMethod;
    }

}
