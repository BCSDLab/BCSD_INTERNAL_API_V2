ALTER TABLE member
    ADD COLUMN academic_status VARCHAR(20) NOT NULL DEFAULT 'ENROLLED';

ALTER TABLE member
    ADD CONSTRAINT ck_member_academic_status
        CHECK (academic_status IN
            ('ENROLLED', 'LEAVE_OF_ABSENCE', 'MILITARY_LEAVE', 'INDUSTRY_PRACTICE', 'GRADUATED'));

ALTER TABLE member
    ADD COLUMN department VARCHAR(100) NOT NULL DEFAULT '';

ALTER TABLE member
    ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE member DROP CONSTRAINT ck_member_track;
ALTER TABLE member
    ADD CONSTRAINT ck_member_track CHECK (track IN
        ('FRONTEND', 'BACKEND', 'ANDROID', 'IOS', 'PM', 'DATA', 'DESIGN', 'DEVOPS', 'PS', 'GAME', 'SECURITY'));

CREATE INDEX idx_member_academic_status ON member (academic_status);
CREATE INDEX idx_member_is_active ON member (is_active);
CREATE INDEX idx_member_generation ON member (generation);
