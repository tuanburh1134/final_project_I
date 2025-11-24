package com.example.financeapp.fund.dto;

public class FundDashboardResponse {

    private FundSectionResponse personalFixed;
    private FundSectionResponse personalOpen;
    private FundSectionResponse groupFixed;
    private FundSectionResponse groupOpen;

    public FundSectionResponse getPersonalFixed() {
        return personalFixed;
    }

    public void setPersonalFixed(FundSectionResponse personalFixed) {
        this.personalFixed = personalFixed;
    }

    public FundSectionResponse getPersonalOpen() {
        return personalOpen;
    }

    public void setPersonalOpen(FundSectionResponse personalOpen) {
        this.personalOpen = personalOpen;
    }

    public FundSectionResponse getGroupFixed() {
        return groupFixed;
    }

    public void setGroupFixed(FundSectionResponse groupFixed) {
        this.groupFixed = groupFixed;
    }

    public FundSectionResponse getGroupOpen() {
        return groupOpen;
    }

    public void setGroupOpen(FundSectionResponse groupOpen) {
        this.groupOpen = groupOpen;
    }
}

