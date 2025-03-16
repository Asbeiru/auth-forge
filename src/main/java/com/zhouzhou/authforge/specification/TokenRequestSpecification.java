package com.zhouzhou.authforge.specification;

/**
 * 令牌请求规范接口
 * 
 * @param <T> 被验证的对象类型
 */
public interface TokenRequestSpecification<T> {
    
    /**
     * 检查对象是否满足规范
     *
     * @param t 要检查的对象
     * @return 是否满足规范
     */
    boolean isSatisfiedBy(T t);

    /**
     * 获取不满足规范时的错误代码
     */
    String getErrorCode();

    /**
     * 获取不满足规范时的错误描述
     */
    String getErrorDescription();

    /**
     * 与其他规范组合（AND）
     */
    default TokenRequestSpecification<T> and(TokenRequestSpecification<T> other) {
        return new AndSpecification<>(this, other);
    }
}