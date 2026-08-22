package com.bcsdlab.bcsdinternalapiv2.member.model;

import com.bcsdlab.bcsdinternalapiv2.global.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    private static final short MAX_LOGIN_FAIL_COUNT = 5;
    private static final long LOCK_DURATION_MINUTES = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_number", nullable = false, updatable = false)
    private String studentNumber;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "track", nullable = false)
    private Track track;

    @Column(name = "generation", nullable = false)
    private String generation;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_type", nullable = false)
    private MemberType memberType;

    @Column(name = "university", nullable = false)
    private String university;

    @Column(name = "department", nullable = false)
    private String department;

    @Enumerated(EnumType.STRING)
    @Column(name = "academic_status", nullable = false)
    private AcademicStatus academicStatus;

    @Column(name = "is_active", nullable = false)
    private boolean clubActive;

    @Column(name = "position")
    private String position;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "dues_required", nullable = false)
    private boolean duesRequired;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "github_id")
    private String githubId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MemberStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private MemberRole role;

    @Column(name = "login_fail_count", nullable = false)
    private short loginFailCount;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "password_changed_at")
    private Instant passwordChangedAt;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "welcome_mail_sent_at")
    private Instant welcomeMailSentAt;

    @Builder
    private Member(String studentNumber, String password, String name, Track track, String generation,
                   MemberType memberType, String university, String department, AcademicStatus academicStatus,
                   Boolean clubActive, String position, LocalDate birthDate, Boolean duesRequired,
                   String email, String phoneNumber, String githubId,
                   MemberStatus status, MemberRole role, Instant passwordChangedAt) {
        this.studentNumber = studentNumber;
        this.password = password;
        this.name = name;
        this.track = track;
        this.generation = generation;
        this.memberType = memberType;
        this.university = university;
        this.department = department != null ? department : "";
        this.academicStatus = academicStatus != null ? academicStatus : AcademicStatus.ENROLLED;
        this.clubActive = clubActive != null ? clubActive : true;
        this.position = position;
        this.birthDate = birthDate;
        this.duesRequired = duesRequired != null ? duesRequired : false;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.githubId = githubId;
        this.status = status != null ? status : MemberStatus.PENDING_SETUP;
        this.role = role != null ? role : MemberRole.MEMBER;
        this.loginFailCount = 0;
        this.passwordChangedAt = truncateToMillis(passwordChangedAt);
    }

    public boolean isPendingSetup() {
        return this.status == MemberStatus.PENDING_SETUP;
    }

    public boolean isActive() {
        return this.status == MemberStatus.ACTIVE;
    }

    public boolean isWithdrawn() {
        return this.status == MemberStatus.WITHDRAWN;
    }

    public boolean isLocked(Instant now) {
        return this.lockedUntil != null && now.isBefore(this.lockedUntil);
    }

    public void recordSuccessfulLogin(Instant now) {
        this.loginFailCount = 0;
        this.lockedUntil = null;
        this.lastLoginAt = now;
    }

    public void recordFailedLogin(Instant now) {
        if (this.lockedUntil != null && !now.isBefore(this.lockedUntil)) {
            this.loginFailCount = 0;
            this.lockedUntil = null;
        }
        this.loginFailCount++;
        if (this.loginFailCount >= MAX_LOGIN_FAIL_COUNT) {
            this.lockedUntil = now.plus(LOCK_DURATION_MINUTES, ChronoUnit.MINUTES);
        }
    }

    public void completeInitialSetup(String encodedPassword, String phoneNumber, String email, String githubId,
                                      Instant now) {
        this.password = encodedPassword;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.githubId = githubId;
        this.passwordChangedAt = truncateToMillis(now);
        this.status = MemberStatus.ACTIVE;
    }

    public void resetPassword(String encodedPassword, Instant now) {
        this.password = encodedPassword;
        this.passwordChangedAt = truncateToMillis(now);
        this.loginFailCount = 0;
        this.lockedUntil = null;
    }

    public void changeAcademicStatus(AcademicStatus academicStatus) {
        this.academicStatus = academicStatus;
    }

    public void changeClubActive(boolean clubActive) {
        this.clubActive = clubActive;
    }

    public void updateProfile(String name, Track track, String generation, MemberType memberType,
                               String university, String department, String position, LocalDate birthDate,
                               boolean duesRequired, String email, String phoneNumber, String githubId) {
        this.name = name;
        this.track = track;
        this.generation = generation;
        this.memberType = memberType;
        this.university = university;
        this.department = department;
        this.position = position;
        this.birthDate = birthDate;
        this.duesRequired = duesRequired;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.githubId = githubId;
    }

    public void changeRole(MemberRole role) {
        this.role = role;
    }

    public void withdraw() {
        this.status = MemberStatus.WITHDRAWN;
    }

    public void restore() {
        this.status = MemberStatus.ACTIVE;
    }

    public void updatePhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public void reissueTemporaryPassword(String encodedPassword, Instant now) {
        this.password = encodedPassword;
        this.passwordChangedAt = truncateToMillis(now);
        this.welcomeMailSentAt = null;
    }

    private static Instant truncateToMillis(Instant instant) {
        return instant != null ? instant.truncatedTo(ChronoUnit.MILLIS) : null;
    }
}
