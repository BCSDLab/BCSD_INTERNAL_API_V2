-- 회비 Slack 알림 수신자. 회원 이메일로 Slack users.lookupByEmail을 조회해 채우거나 관리자가 입력한다.
-- 비어 있는 회원이 많으므로 값이 있는 행만 유일성을 검사한다.
ALTER TABLE member ADD COLUMN slack_id VARCHAR(20);

CREATE UNIQUE INDEX uq_member_slack_id ON member (slack_id) WHERE slack_id IS NOT NULL;
