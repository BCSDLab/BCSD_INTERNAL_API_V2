package com.bcsdlab.bcsdinternalapiv2.member.model;

import com.bcsdlab.bcsdinternalapiv2.global.BaseTimeEntity;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackMaster;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "track_id", nullable = false)
    private TrackMaster track;

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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "member_position",
            joinColumns = @JoinColumn(name = "member_id"),
            inverseJoinColumns = @JoinColumn(name = "position_id"))
    private Set<Position> positions = new LinkedHashSet<>();

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "dues_required", nullable = false)
    private boolean duesRequired;

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

    // 홈페이지 "함께 할 멤버들"에 노출된다(T-18). 관리자 업로드(S3 presigned URL) 또는
    // Slack 프로필 동기화를 통해서만 갱신된다 — 두 경로 모두 admin API를 경유한다.
    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Builder
    private Member(String studentNumber, String password, String name, TrackMaster track, String generation,
                   MemberType memberType, String university, String department, AcademicStatus academicStatus,
                   Boolean clubActive, Collection<Position> positions, LocalDate birthDate, Boolean duesRequired,
                   String email, String phoneNumber, String githubId, String profileImageUrl,
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
        if (positions != null) {
            this.positions.addAll(positions);
        }
        this.birthDate = birthDate;
        this.duesRequired = duesRequired != null ? duesRequired : false;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.githubId = githubId;
        this.profileImageUrl = profileImageUrl;
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

    public void updateContact(String phoneNumber, String email, String githubId) {
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.githubId = githubId;
    }

    public void changePassword(String encodedPassword, Instant now) {
        this.password = encodedPassword;
        this.passwordChangedAt = truncateToMillis(now);
    }

    public void resetPassword(String encodedPassword, Instant now) {
        this.password = encodedPassword;
        this.passwordChangedAt = truncateToMillis(now);
        this.loginFailCount = 0;
        this.lockedUntil = null;
    }

    public void reissueTemporaryPassword(String encodedPassword, Instant now) {
        this.password = encodedPassword;
        this.passwordChangedAt = truncateToMillis(now);
        this.welcomeMailSentAt = null;
    }

    public void changeAcademicStatus(AcademicStatus academicStatus) {
        this.academicStatus = academicStatus;
    }

    public void changeClubActive(boolean clubActive) {
        this.clubActive = clubActive;
    }

    public void updateProfile(String name, TrackMaster track, String generation, MemberType memberType,
                               String university, String department, Collection<Position> positions,
                               LocalDate birthDate, boolean duesRequired, String email, String phoneNumber,
                               String githubId) {
        this.name = name;
        this.track = track;
        this.generation = generation;
        this.memberType = memberType;
        this.university = university;
        this.department = department;
        this.positions.clear();
        if (positions != null) {
            this.positions.addAll(positions);
        }
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

    public void updateProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    private static Instant truncateToMillis(Instant instant) {
        return instant != null ? instant.truncatedTo(ChronoUnit.MILLIS) : null;
    }
}
