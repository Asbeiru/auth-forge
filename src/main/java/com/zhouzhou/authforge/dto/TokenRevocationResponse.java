package com.zhouzhou.authforge.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenRevocationResponse {
    private String error;
    private String error_description;
} 