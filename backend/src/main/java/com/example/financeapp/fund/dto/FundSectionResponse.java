package com.example.financeapp.fund.dto;

import java.util.Collections;
import java.util.List;

public class FundSectionResponse {

    private String title;
    private String description;
    private int total;
    private List<FundCardResponse> funds;

    public FundSectionResponse() {
    }

    public FundSectionResponse(String title, String description, List<FundCardResponse> funds) {
        this.title = title;
        this.description = description;
        this.funds = funds != null ? funds : Collections.emptyList();
        this.total = this.funds.size();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<FundCardResponse> getFunds() {
        return funds;
    }

    public void setFunds(List<FundCardResponse> funds) {
        this.funds = funds;
    }
}

