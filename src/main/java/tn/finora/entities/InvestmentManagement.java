package tn.finora.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

public class InvestmentManagement {

    private int managementId;
    private int investmentId;
    private String investmentType;
    private BigDecimal amountInvested;
    private BigDecimal ownershipPercentage;
    private LocalDate startDate;
    private String status;

    public InvestmentManagement() {}

    // INSERT
    public InvestmentManagement(int investmentId, String investmentType,
                                BigDecimal amountInvested, BigDecimal ownershipPercentage,
                                LocalDate startDate, String status) {
        this.investmentId = investmentId;
        this.investmentType = investmentType;
        this.amountInvested = amountInvested;
        this.ownershipPercentage = ownershipPercentage;
        this.startDate = startDate;
        this.status = status;
    }

    // UPDATE / FULL
    public InvestmentManagement(int managementId, int investmentId, String investmentType,
                                BigDecimal amountInvested, BigDecimal ownershipPercentage,
                                LocalDate startDate, String status) {
        this.managementId = managementId;
        this.investmentId = investmentId;
        this.investmentType = investmentType;
        this.amountInvested = amountInvested;
        this.ownershipPercentage = ownershipPercentage;
        this.startDate = startDate;
        this.status = status;
    }

    public int getManagementId() { return managementId; }
    public void setManagementId(int managementId) { this.managementId = managementId; }

    public int getInvestmentId() { return investmentId; }
    public void setInvestmentId(int investmentId) { this.investmentId = investmentId; }

    public String getInvestmentType() { return investmentType; }
    public void setInvestmentType(String investmentType) { this.investmentType = investmentType; }

    public BigDecimal getAmountInvested() { return amountInvested; }
    public void setAmountInvested(BigDecimal amountInvested) { this.amountInvested = amountInvested; }

    public BigDecimal getOwnershipPercentage() { return ownershipPercentage; }
    public void setOwnershipPercentage(BigDecimal ownershipPercentage) { this.ownershipPercentage = ownershipPercentage; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "InvestmentManagement{" +
                "managementId=" + managementId +
                ", investmentId=" + investmentId +
                ", investmentType='" + investmentType + '\'' +
                ", amountInvested=" + amountInvested +
                ", ownershipPercentage=" + ownershipPercentage +
                ", startDate=" + startDate +
                ", status='" + status + '\'' +
                '}';
    }
}
