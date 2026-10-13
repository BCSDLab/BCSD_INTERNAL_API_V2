-- 지금 비밀번호가 관리자가 만든 임시 비밀번호인지. 초기 설정에서 새 비밀번호를 받아야 하는지를 이것으로 정한다.
-- 임시 비밀번호(가입 메일)로 들어온 회원만 다시 정한다. 비밀번호 설정 링크로 직접 정한 회원은 초기 설정에서 묻지 않는다.
ALTER TABLE member ADD COLUMN password_temporary BOOLEAN NOT NULL DEFAULT FALSE;

-- 초기 설정 전이면서 비밀번호가 있는 회원은 지금까지 모두 임시 비밀번호로 취급했다(초기 설정에서 다시 입력). 그대로 이어 간다.
UPDATE member SET password_temporary = TRUE WHERE status = 'PENDING_SETUP' AND password IS NOT NULL;
