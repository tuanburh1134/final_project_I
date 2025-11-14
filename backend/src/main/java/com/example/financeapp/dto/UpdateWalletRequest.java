package com.example.financeapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO cho request cập nhật thông tin ví
 *
 * Cho phép sửa:
 * - walletName: Tên ví (bắt buộc)
 * - description: Mô tả (tùy chọn)
 * - balance: Số dư (CHỈ khi ví chưa có giao dịch nào)
 *
 * KHÔNG cho phép sửa:
 * - currencyCode: Loại tiền tệ (immutable)
 * - balance: Nếu ví đã có giao dịch (chỉ thay đổi qua transactions hoặc xóa ví)
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWalletRequest {

    @NotBlank(message = "Tên ví không được để trống")
    @Size(max = 100, message = "Tên ví không được vượt quá 100 ký tự")
    private String walletName;

    @Size(max = 255, message = "Mô tả không được vượt quá 255 ký tự")
    private String description;

    // currencyCode KHÔNG ĐƯỢC SỬA → nhưng vẫn cần để client gửi lên (nếu muốn kiểm tra)
    private String currencyCode;

    @DecimalMin(value = "0.0", message = "Số dư không được âm")
    private BigDecimal balance;

    // Đặt ví làm mặc định
    private Boolean setAsDefault;
}
