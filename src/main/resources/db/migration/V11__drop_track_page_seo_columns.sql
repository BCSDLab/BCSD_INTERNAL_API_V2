-- track_page의 히어로/OG/meta description 컬럼 제거.
--
-- 팀 결정으로 인터널 화면에서 세 항목을 모두 뺐다(시안에도 없다). 설정할 수단이 없으니
-- 값은 영구히 NULL로 남는 죽은 컬럼이고, ddl-auto: validate가 엔티티와 계속 대조하므로
-- 컬럼째 정리한다.
--
-- 되돌릴 수 없는 변경이다. 아직 운영 콘텐츠가 들어가기 전(T-20 시드 이전)이라 실제로
-- 값이 있는 행은 없다. 나중에 OG를 다시 도입한다면 새 마이그레이션으로 추가한다.
--
-- activity_category.hero_image_url은 건드리지 않는다 — 활동 화면에서 실제로 쓰고 있다.
ALTER TABLE track_page
    DROP COLUMN hero_image_url,
    DROP COLUMN og_image_url,
    DROP COLUMN seo_description;
