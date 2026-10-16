package com.bcsdlab.bcsdinternalapiv2.auth.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 컨트롤러 파라미터에 로그인한 회원의 id(액세스 토큰의 subject)를 넣는다. 파라미터 타입은 {@code Long}이다.
 * {@code required = false}면 인증이 없을 때 null이다. 문서(Swagger)에는 노출되지 않는다.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginMemberId {

    boolean required() default true;
}
