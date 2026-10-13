-- 인명부를 이 DB 하나로 합친다. 졸업생·멘토처럼 로그인 계정 없이 명부에만 있는 회원을 받기 위해 비밀번호를 비워 둘 수 있게 한다.
-- password가 NULL이면 "계정 없음"이다. 상태는 PENDING_SETUP이고 환영 메일도 보내지 않는다(welcome_mail_sent_at NULL).
-- 계정은 관리자가 환영 메일 재발송(임시 비밀번호) 또는 Slack 비밀번호 설정 링크로 만들어 준다. 셀프 비밀번호 재설정은 막는다.
ALTER TABLE member ALTER COLUMN password DROP NOT NULL;

-- 직장. 관리자 인명부에서만 보이고 수정한다.
ALTER TABLE member ADD COLUMN company VARCHAR(100);
