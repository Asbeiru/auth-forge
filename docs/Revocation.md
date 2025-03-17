# 撤销访问令牌
curl -X POST "http://localhost:9000/oauth2/revoke" \
-H "Authorization: Basic base64(client_id:client_secret)" \
-H "Content-Type: application/x-www-form-urlencoded" \
-d "token=access_token_value&token_type_hint=access_token"

# 撤销刷新令牌
curl -X POST "http://localhost:9000/oauth2/revoke" \
-H "Authorization: Basic base64(client_id:client_secret)" \
-H "Content-Type: application/x-www-form-urlencoded" \
-d "token=refresh_token_value&token_type_hint=refresh_token"