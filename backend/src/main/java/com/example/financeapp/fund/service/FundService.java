package com.example.financeapp.fund.service;

import com.example.financeapp.fund.dto.CreateFundRequest;
import com.example.financeapp.fund.dto.FundDashboardResponse;
import com.example.financeapp.fund.dto.FundDetailResponse;

public interface FundService {

    FundDetailResponse createFund(Long userId, CreateFundRequest request);

    FundDashboardResponse getFundDashboard(Long userId);

    FundDetailResponse getFundDetail(Long userId, Long fundId);
}

