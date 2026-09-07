-- 트래픽이 적어 SSR로 충분하다고 판단, 홈페이지 revalidate 웹훅(ADR-010)을 API 쪽에서 제거한다.
DROP TABLE homepage_sync_status;
