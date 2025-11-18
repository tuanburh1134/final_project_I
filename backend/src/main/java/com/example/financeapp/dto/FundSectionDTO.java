package com.example.financeapp.dto;

import java.util.ArrayList;
import java.util.List;

public class FundSectionDTO {
    private String title;
    private String description;
    private int total;
    private List<FundListItemDTO> funds = new ArrayList<>();

    public FundSectionDTO() {
    }

    public FundSectionDTO(String title, String description) {
        this.title = title;
        this.description = description;
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

    public List<FundListItemDTO> getFunds() {
        return funds;
    }

    public void setFunds(List<FundListItemDTO> funds) {
        this.funds = funds;
    }
}

