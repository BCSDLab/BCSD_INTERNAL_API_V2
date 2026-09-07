package com.bcsdlab.bcsdinternalapiv2.global.event;

import java.util.List;

/**
 * 홈페이지 노출 콘텐츠가 바뀌었음을 알린다(T-21, ADR-010). 트랜잭션 커밋 후에만
 * {@code HomepageRevalidationListener}가 이 이벤트를 받아 웹훅을 호출한다.
 */
public record ContentChangedEvent(List<String> tags) {
}
