-- rotated_from_id is a write-only audit trail (never read back), so the self-referential
-- FK only gets in the way of deleting rows (e.g. cleaning up expired/revoked tokens).
ALTER TABLE refresh_token DROP CONSTRAINT refresh_token_rotated_from_id_fkey;
