-- 홈페이지 "함께 할 멤버들" 섹션에 프로필 사진이 필요한데 현재 member 스키마에 없다(T-18).
-- 컬럼과 조회만 추가하고 쓰기 API는 만들지 않는다 — 명부 관리 책임은 그대로 auth/member
-- 담당자에게 있다(INV-13).
ALTER TABLE member ADD COLUMN profile_image_url VARCHAR(500);
