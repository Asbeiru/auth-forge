package com.zhouzhou.authforge.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@ControllerAdvice
public class OAuth2ExceptionHandler {

    @ExceptionHandler(OAuth2AuthorizationException.class)
    public String handleOAuth2Exception(OAuth2AuthorizationException ex) {
        if (ex.getRedirectUri() != null) {
            return "redirect:" + UriComponentsBuilder.fromUriString(ex.getRedirectUri())
                    .queryParam("error", ex.getError())
                    .queryParamIfPresent("state", Optional.ofNullable(ex.getState()))
                    .build()
                    .toUriString();
        }
        return "error";
    }
} 