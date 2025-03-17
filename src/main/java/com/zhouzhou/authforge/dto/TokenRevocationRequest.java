package com.zhouzhou.authforge.dto;

import lombok.Data;

@Data
public class TokenRevocationRequest {
    private String token;
    private String token_type_hint;
} 