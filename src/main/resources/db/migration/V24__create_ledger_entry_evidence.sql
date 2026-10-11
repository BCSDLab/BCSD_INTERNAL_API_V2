-- 증빙 파일 자체는 기존 image_asset(purpose='LEDGER_EVIDENCE')에 저장한다. 이 테이블은 장부와의 연결만 둔다.
-- image_asset과는 FK를 걸지 않는다(ADR-009: 미디어 행과 콘텐츠를 FK로 묶지 않는다). 존재 여부는 서비스가 검사한다.
CREATE TABLE ledger_entry_evidence (
    image_asset_id  BIGINT       PRIMARY KEY,          -- 증빙 하나는 한 기록에만 붙는다
    entry_id        BIGINT       NOT NULL REFERENCES ledger_entry (id),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_ledger_entry_evidence_entry_id ON ledger_entry_evidence (entry_id);
