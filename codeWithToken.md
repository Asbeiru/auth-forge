# 
1. 发送
```shell
curl --location 'http://localhost:9000/oauth2/token' \
--header 'Authorization: Basic bWVzc2FnaW5nLWNsaWVudDpzZWNyZXQ=' \
--form 'grant_type="authorization_code"' \
--form 'code="OMvQP-gBHFa1i9k7xTycX90V69LqLjIbaCcCzWuN3JVMDSzEtnxD-jnIcr2kI-iEt5GR2N5qnMN3GZrB5Fw3BFipmtTmTqqRvabeEGTGBfvDRc_06pCjS7DxHPttOwCj"' \
--form 'redirect_uri="http://127.0.0.1:8080/login/oauth2/code/messaging-client-oidc"'
```
# 处理流程
1. 转交给 OAuth2TokenEndpointFilter 处理
2. 校验基本参数：GRANT_TYPE 的合法性
```
String[] grantTypes = request.getParameterValues(OAuth2ParameterNames.GRANT_TYPE);
    if (grantTypes == null || grantTypes.length != 1) {
        throwError(OAuth2ErrorCodes.INVALID_REQUEST, OAuth2ParameterNames.GRANT_TYPE);
    }
```
3. 转交给 OAuth2AuthorizationCodeAuthenticationConverter 解析请求，解析成一个 OAuth2AuthorizationCodeAuthenticationToken 对象。
注意在这之前客户端已经被认证通过 OAuth2ClientCredentialsAuthenticationProvider 认证通过，变成authenticated 状态。
```shell
MultiValueMap<String, String> parameters = OAuth2EndpointUtils.getFormParameters(request);

		// grant_type (REQUIRED)
		String grantType = parameters.getFirst(OAuth2ParameterNames.GRANT_TYPE);
		if (!AuthorizationGrantType.AUTHORIZATION_CODE.getValue().equals(grantType)) {
			return null;
		}

		Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();

		// code (REQUIRED)
		String code = parameters.getFirst(OAuth2ParameterNames.CODE);
		if (!StringUtils.hasText(code) || parameters.get(OAuth2ParameterNames.CODE).size() != 1) {
			OAuth2EndpointUtils.throwError(OAuth2ErrorCodes.INVALID_REQUEST, OAuth2ParameterNames.CODE,
					OAuth2EndpointUtils.ACCESS_TOKEN_REQUEST_ERROR_URI);
		}

		// redirect_uri (REQUIRED)
		// Required only if the "redirect_uri" parameter was included in the authorization
		// request
		String redirectUri = parameters.getFirst(OAuth2ParameterNames.REDIRECT_URI);
		if (StringUtils.hasText(redirectUri) && parameters.get(OAuth2ParameterNames.REDIRECT_URI).size() != 1) {
			OAuth2EndpointUtils.throwError(OAuth2ErrorCodes.INVALID_REQUEST, OAuth2ParameterNames.REDIRECT_URI,
					OAuth2EndpointUtils.ACCESS_TOKEN_REQUEST_ERROR_URI);
		}

		Map<String, Object> additionalParameters = new HashMap<>();
		parameters.forEach((key, value) -> {
			if (!key.equals(OAuth2ParameterNames.GRANT_TYPE) && !key.equals(OAuth2ParameterNames.CLIENT_ID)
					&& !key.equals(OAuth2ParameterNames.CODE) && !key.equals(OAuth2ParameterNames.REDIRECT_URI)) {
				additionalParameters.put(key, (value.size() == 1) ? value.get(0) : value.toArray(new String[0]));
			}
		});

		return new OAuth2AuthorizationCodeAuthenticationToken(code, clientPrincipal, redirectUri, additionalParameters);
```