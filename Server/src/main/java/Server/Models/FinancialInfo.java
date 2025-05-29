package Server.Models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

public class FinancialInfo {
    private int id;
    private int clientId;
    private BigDecimal creditBalance;
    private BigDecimal overdueAmount;
    private int openCredits;
    private int creditInquiries;
    private LocalDate lastUpdated;
    private BigDecimal creditLimit;
    private String creditTypes;
    private LocalDate firstCreditDate;

    public FinancialInfo(int id, int clientId, BigDecimal creditBalance, BigDecimal overdueAmount,
                         int openCredits, int creditInquiries, LocalDate lastUpdated,
                         BigDecimal creditLimit, String creditTypes, LocalDate firstCreditDate) {
        this.id = id;
        this.clientId = clientId;
        this.creditBalance = creditBalance;
        this.overdueAmount = overdueAmount;
        this.openCredits = openCredits;
        this.creditInquiries = creditInquiries;
        this.lastUpdated = lastUpdated;
        this.creditLimit = creditLimit;
        this.creditTypes = creditTypes;
        this.firstCreditDate = firstCreditDate;
    }

    // Геттеры
    public int getId() { return id; }
    public int getClientId() { return clientId; }
    public BigDecimal getCreditBalance() { return creditBalance; }
    public BigDecimal getOverdueAmount() { return overdueAmount; }
    public int getOpenCredits() { return openCredits; }
    public int getCreditInquiries() { return creditInquiries; }
    public LocalDate getLastUpdated() { return lastUpdated; }
    public BigDecimal getCreditLimit() { return creditLimit; }
    public String getCreditTypes() { return creditTypes; }
    public LocalDate getFirstCreditDate() { return firstCreditDate; }

    // Сеттеры
    public void setId(int id) { this.id = id; }
    public void setClientId(int clientId) { this.clientId = clientId; }
    public void setCreditBalance(BigDecimal creditBalance) { this.creditBalance = creditBalance; }
    public void setOverdueAmount(BigDecimal overdueAmount) { this.overdueAmount = overdueAmount; }
    public void setOpenCredits(int openCredits) { this.openCredits = openCredits; }
    public void setCreditInquiries(int creditInquiries) { this.creditInquiries = creditInquiries; }
    public void setLastUpdated(LocalDate lastUpdated) { this.lastUpdated = lastUpdated; }
    public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }
    public void setCreditTypes(String creditTypes) { this.creditTypes = creditTypes; }
    public void setFirstCreditDate(LocalDate firstCreditDate) { this.firstCreditDate = firstCreditDate; }

    // Методы для расчёта FICO
    public int calculatePaymentHistoryScore() {
        // Платёжная история: 0 просрочки — максимальный балл, больше просрочка — меньше балл
        if (overdueAmount.compareTo(BigDecimal.ZERO) == 0) {
            return 100; // Максимальный балл
        } else if (overdueAmount.compareTo(new BigDecimal("5000")) <= 0) {
            return 50; // Средний балл
        } else {
            return 0; // Минимальный балл
        }
    }

    public int calculateDebtAmountScore() {
        // Сумма задолженности: меньше долг — выше балл
        if (creditBalance.compareTo(new BigDecimal("10000")) <= 0) {
            return 100; // Низкий долг
        } else if (creditBalance.compareTo(new BigDecimal("50000")) <= 0) {
            return 50; // Средний долг
        } else {
            return 0; // Высокий долг
        }
    }

    public int calculateCreditHistoryLengthScore() {
        // Длительность кредитной истории: больше лет — выше балл
        if (firstCreditDate == null) {
            return 0;
        }
        int years = Period.between(firstCreditDate, LocalDate.now()).getYears();
        if (years >= 10) {
            return 100; // Долгая история
        } else if (years >= 5) {
            return 50; // Средняя история
        } else {
            return 0; // Короткая история
        }
    }

    public int calculateNewCreditsScore() {
        // Новые кредиты: меньше запросов — выше балл
        if (creditInquiries <= 2) {
            return 100; // Мало запросов
        } else if (creditInquiries <= 5) {
            return 50; // Среднее количество
        } else {
            return 0; // Много запросов
        }
    }

    public int calculateCreditTypesScore() {
        // Типы кредитов: больше разнообразие — выше балл
        if (creditTypes == null || creditTypes.isEmpty()) {
            return 0;
        }
        String[] types = creditTypes.split(",");
        if (types.length >= 3) {
            return 100; // Большое разнообразие
        } else if (types.length == 2) {
            return 50; // Среднее разнообразие
        } else {
            return 0; // Мало разнообразия
        }
    }
}