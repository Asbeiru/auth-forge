package com.zhouzhou.authforge.specification;

/**
 * 组合规范（AND）
 */
public class AndSpecification<T> implements TokenRequestSpecification<T> {
    private final TokenRequestSpecification<T> first;
    private final TokenRequestSpecification<T> second;
    private TokenRequestSpecification<T> failed;

    public AndSpecification(TokenRequestSpecification<T> first, TokenRequestSpecification<T> second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean isSatisfiedBy(T t) {
        if (!first.isSatisfiedBy(t)) {
            failed = first;
            return false;
        }
        if (!second.isSatisfiedBy(t)) {
            failed = second;
            return false;
        }
        return true;
    }

    @Override
    public String getErrorCode() {
        return failed != null ? failed.getErrorCode() : null;
    }

    @Override
    public String getErrorDescription() {
        return failed != null ? failed.getErrorDescription() : null;
    }
}