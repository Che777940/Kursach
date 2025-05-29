package Server.Models;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CreditApplication {
    private int id;
    private int clientId;
    private BigDecimal loanAmount;
    private int loanTerm;
    private String purpose;
    private String status;
    private LocalDate submissionDate;

    public CreditApplication(int id, int clientId, BigDecimal loanAmount, int loanTerm, String purpose, String status, LocalDate submissionDate) {
        this.id = id;
        this.clientId = clientId;
        this.loanAmount = loanAmount;
        this.loanTerm = loanTerm;
        this.purpose = purpose;
        this.status = status;
        this.submissionDate = submissionDate;
    }

    // Геттеры
    public int getId() { return id; }
    public int getClientId() { return clientId; }
    public BigDecimal getLoanAmount() { return loanAmount; }
    public int getLoanTerm() { return loanTerm; }
    public String getPurpose() { return purpose; }
    public String getStatus() { return status; }
    public LocalDate getSubmissionDate() { return submissionDate; }

    // Сеттеры
    public void setId(int id) { this.id = id; }
    public void setClientId(int clientId) { this.clientId = clientId; }
    public void setLoanAmount(BigDecimal loanAmount) { this.loanAmount = loanAmount; }
    public void setLoanTerm(int loanTerm) { this.loanTerm = loanTerm; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public void setStatus(String status) { this.status = status; }
    public void setSubmissionDate(LocalDate submissionDate) { this.submissionDate = submissionDate; }
}