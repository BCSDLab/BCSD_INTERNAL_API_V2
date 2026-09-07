-- expand -> migrate -> contract 중 expand+migrate 단계. contract(레거시 컬럼 완전 제거)는
-- 이번 마이그레이션 범위 밖이다 — 애플리케이션 코드 전환이 배포로 안정화된 뒤 별도 진행한다.

ALTER TABLE member ADD COLUMN track_id BIGINT;

UPDATE member m SET track_id = t.id FROM track t WHERE t.code = m.track;

ALTER TABLE member ALTER COLUMN track_id SET NOT NULL;
ALTER TABLE member ADD CONSTRAINT fk_member_track FOREIGN KEY (track_id) REFERENCES track (id);

-- 레거시 문자열 컬럼은 남겨 두되(하위 호환 read path), 새 엔티티는 이 컬럼을 매핑하지 않으므로
-- NOT NULL을 유지하면 새 부원 생성 INSERT가 "track" 컬럼 누락으로 실패한다. CHECK 제약은
-- NULL을 통과시키므로 ck_member_track과 충돌하지 않는다.
ALTER TABLE member ALTER COLUMN track DROP NOT NULL;
