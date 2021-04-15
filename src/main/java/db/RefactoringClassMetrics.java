package db;

import javax.persistence.*;

@Entity
@Table(name="refactoringClassMetrics")
public class RefactoringClassMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refactoringClassMetricsId")
    private Long refactoringClassMetricsId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "extractMethodId", nullable = false)
    private ExtractMethod extractMethod;

    @Column(name="isBefore")
    private boolean isBefore;

    @Column(name= "wmc")
    private int wmc;

    @Column(name= "loc")
    private int loc;

    @Column(name="numberOfMethods")
    private int numberOfMethods;

    @Column(name="numberOfMethodInvocations")
    private int numberOfMethodInvocations;

    @Column(name= "numberOfAttributes")
    private int numberOfAttributes;


    public RefactoringClassMetrics(){ }

    public int getNumberOfAttributes() {
        return numberOfAttributes;
    }

    public void setNumberOfAttributes(int numberOfAttributes) {
        this.numberOfAttributes = numberOfAttributes;
    }

    public int getNumberOfMethodInvocations() {
        return numberOfMethodInvocations;
    }

    public void setNumberOfMethodInvocations(int numberOfMethodInvocations) {
        this.numberOfMethodInvocations = numberOfMethodInvocations;
    }

    public int getNumberOfMethods() {
        return numberOfMethods;
    }

    public void setNumberOfMethods(int numberOfMethods) {
        this.numberOfMethods = numberOfMethods;
    }

    public int getLoc() {
        return loc;
    }

    public void setLoc(int loc) {
        this.loc = loc;
    }

    public int getWmc() {
        return wmc;
    }

    public void setWmc(int wmc) {
        this.wmc = wmc;
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
