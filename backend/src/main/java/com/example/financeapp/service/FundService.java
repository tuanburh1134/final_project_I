package com.example.financeapp.service;

import com.example.financeapp.dto.*;
import com.example.financeapp.entity.Fund;
import com.example.financeapp.entity.FundMember;
import jakarta.transaction.Transactional;

public interface FundService {

    FundOverviewResponse getMyFundsOverview(Long userId);

    FundDetailResponse getFundDetail(Long userId, Long fundId);

    Fund createFund(Long userId, CreateFundRequest request);

    Fund updateFund(Long userId, Long fundId, UpdateFundRequest request);

    @Transactional
    void closeFund(Long userId, Long fundId);

    @Transactional
    void deleteFund(Long userId, Long fundId);

    FundMember addMember(Long userId, Long fundId, FundMemberInviteRequest request);

    void removeMember(Long userId, Long fundId, Long memberId);
}

