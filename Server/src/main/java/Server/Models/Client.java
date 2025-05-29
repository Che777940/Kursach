package Server.Models;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Client {
    private int id;
    private String fullName;
    private LocalDate birthDate;
    private String gender;
    private String phone;
    private String email;
    private String address;
    private BigDecimal monthlyIncome;
    private String employmentStatus;
    private String workplace;
    private String assets;
    private String passportNumber;
    private LocalDate passportIssueDate;
    private LocalDate passportExpiryDate;
    private String passportIssuingCountry;

    public Client(String fullName, LocalDate birthDate, String gender, String phone,
                  String email, String address, BigDecimal monthlyIncome,
                  String employmentStatus, String workplace, String assets,
                  String passportNumber, LocalDate passportIssueDate,
                  LocalDate passportExpiryDate, String passportIssuingCountry) {
        this.fullName = fullName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.monthlyIncome = monthlyIncome;
        this.employmentStatus = employmentStatus;
        this.workplace = workplace;
        this.assets = assets;
        this.passportNumber = passportNumber;
        this.passportIssueDate = passportIssueDate;
        this.passportExpiryDate = passportExpiryDate;
        this.passportIssuingCountry = passportIssuingCountry;
    }

    public Client(int id, String fullName, LocalDate birthDate, String gender, String phone,
                  String email, String address, BigDecimal monthlyIncome,
                  String employmentStatus, String workplace, String assets,
                  String passportNumber, LocalDate passportIssueDate,
                  LocalDate passportExpiryDate, String passportIssuingCountry) {
        this(fullName, birthDate, gender, phone, email, address, monthlyIncome,
                employmentStatus, workplace, assets, passportNumber, passportIssueDate,
                passportExpiryDate, passportIssuingCountry);
        this.id = id;
    }

    public Client(int id, String fullName) {
        this.id = id;
        this.fullName = fullName;
    }

    public Client() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigDecimal getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(BigDecimal monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }

    public String getEmploymentStatus() {
        return employmentStatus;
    }

    public void setEmploymentStatus(String employmentStatus) {
        this.employmentStatus = employmentStatus;
    }

    public String getWorkplace() {
        return workplace;
    }

    public void setWorkplace(String workplace) {
        this.workplace = workplace;
    }

    public String getAssets() {
        return assets;
    }

    public void setAssets(String assets) {
        this.assets = assets;
    }

    public String getPassportNumber() {
        return passportNumber;
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
    }

    public LocalDate getPassportIssueDate() {
        return passportIssueDate;
    }

    public void setPassportIssueDate(LocalDate passportIssueDate) {
        this.passportIssueDate = passportIssueDate;
    }

    public LocalDate getPassportExpiryDate() {
        return passportExpiryDate;
    }

    public void setPassportExpiryDate(LocalDate passportExpiryDate) {
        this.passportExpiryDate = passportExpiryDate;
    }

    public String getPassportIssuingCountry() {
        return passportIssuingCountry;
    }

    public void setPassportIssuingCountry(String passportIssuingCountry) {
        this.passportIssuingCountry = passportIssuingCountry;
    }

    @Override
    public String toString() {
        return "Client{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", birthDate=" + birthDate +
                ", gender='" + gender + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", address='" + address + '\'' +
                ", monthlyIncome=" + monthlyIncome +
                ", employmentStatus='" + employmentStatus + '\'' +
                ", workplace='" + workplace + '\'' +
                ", assets='" + assets + '\'' +
                ", passportNumber='" + passportNumber + '\'' +
                ", passportIssueDate=" + passportIssueDate +
                ", passportExpiryDate=" + passportExpiryDate +
                ", passportIssuingCountry='" + passportIssuingCountry + '\'' +
                '}';
    }

    public boolean isEmployed() {
        return employmentStatus != null &&
                (employmentStatus.equalsIgnoreCase("employed") ||
                        employmentStatus.equalsIgnoreCase("self-employed"));
    }
}