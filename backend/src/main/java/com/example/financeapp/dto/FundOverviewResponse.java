package com.example.financeapp.dto;

public class FundOverviewResponse {

    private OverviewGroup personal;
    private OverviewGroup group;

    public FundOverviewResponse() {
        this.personal = new OverviewGroup();
        this.group = new OverviewGroup();
    }

    public OverviewGroup getPersonal() {
        return personal;
    }

    public void setPersonal(OverviewGroup personal) {
        this.personal = personal;
    }

    public OverviewGroup getGroup() {
        return group;
    }

    public void setGroup(OverviewGroup group) {
        this.group = group;
    }

    public static class OverviewGroup {
        private String description;
        private FundSectionDTO fixedTerm;
        private FundSectionDTO flexible;

        public OverviewGroup() {
            this.fixedTerm = new FundSectionDTO();
            this.flexible = new FundSectionDTO();
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public FundSectionDTO getFixedTerm() {
            return fixedTerm;
        }

        public void setFixedTerm(FundSectionDTO fixedTerm) {
            this.fixedTerm = fixedTerm;
        }

        public FundSectionDTO getFlexible() {
            return flexible;
        }

        public void setFlexible(FundSectionDTO flexible) {
            this.flexible = flexible;
        }
    }
}

