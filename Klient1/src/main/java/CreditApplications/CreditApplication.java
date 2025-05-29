package CreditApplications;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CreditApplication {
    private final int id;
    private final int clientId;
    private final BigDecimal loanAmount;
    private final int loanTerm;
    private final String purpose;
    private final String status;
    private final LocalDate submissionDate;

    public CreditApplication(int id, int clientId, BigDecimal loanAmount, int loanTerm, String purpose, String status, LocalDate submissionDate) {
        this.id = id;
        this.clientId = clientId;
        this.loanAmount = loanAmount;
        this.loanTerm = loanTerm;
        this.purpose = purpose;
        this.status = status;
        this.submissionDate = submissionDate;
    }

    public int getId() { return id; }
    public int getClientId() { return clientId; }
    public BigDecimal getLoanAmount() { return loanAmount; }
    public int getLoanTerm() { return loanTerm; }
    public String getPurpose() { return purpose; }
    public String getStatus() { return status; }
    public LocalDate getSubmissionDate() { return submissionDate; }
}