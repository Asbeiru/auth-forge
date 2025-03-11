package com.zhouzhou.authforge.util;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class OAuth2Utils {
    
    public static Set<String> parseScopes(String scope) {
        if (!StringUtils.hasText(scope)) {
            return Collections.emptySet();
        }
        return Arrays.stream(scope.split(" "))
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
    }

    public static Set<String> parseRedirectUris(String redirectUris) {
        if (!StringUtils.hasText(redirectUris)) {
            return Collections.emptySet();
        }
        return Arrays.stream(redirectUris.split(","))
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
    }
} 