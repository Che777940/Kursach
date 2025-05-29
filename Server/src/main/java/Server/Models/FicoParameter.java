package Server.Models;

public class FicoParameter {
    private int id;
    private String parameterName;
    private int weightPercentage;
    private String calculationFormula;
    private Integer calculatedValue;
    private String description;

    public FicoParameter() {}

    public FicoParameter(String parameterName, int weightPercentage,
                         String calculationFormula, String description) {
        this.parameterName = parameterName;
        this.weightPercentage = weightPercentage;
        this.calculationFormula = calculationFormula;
        this.description = description;
    }

    // Геттеры и сеттеры
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public int getWeightPercentage() {
        return weightPercentage;
    }

    public void setWeightPercentage(int weightPercentage) {
        this.weightPercentage = weightPercentage;
    }

    public String getCalculationFormula() {
        return calculationFormula;
    }

    public void setCalculationFormula(String calculationFormula) {
        this.calculationFormula = calculationFormula;
    }

    public Integer getCalculatedValue() {
        return calculatedValue;
    }

    public void setCalculatedValue(Integer calculatedValue) {
        this.calculatedValue = calculatedValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "FicoParameter{" +
                "id=" + id +
                ", parameterName='" + parameterName + '\'' +
                ", weightPercentage=" + weightPercentage +
                ", calculationFormula='" + calculationFormula + '\'' +
                ", calculatedValue=" + calculatedValue +
                ", description='" + description + '\'' +
                '}';
    }
}