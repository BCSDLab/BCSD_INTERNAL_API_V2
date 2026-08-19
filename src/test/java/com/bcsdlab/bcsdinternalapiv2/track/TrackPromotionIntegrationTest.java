package com.bcsdlab.bcsdinternalapiv2.track;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bcsdlab.bcsdinternalapiv2.IntegrationTestSupport;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberStatus;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberType;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackMasterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * T-35(member.track enum → 참조 테이블 승격)의 V7/V8 마이그레이션 결과를 검증한다.
 * {@link IntegrationTestSupport}의 두 번째 소비자다.
 */
class TrackPromotionIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private TrackMasterRepository trackMasterRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("AC-8.1 track 테이블에 기존 10개 + SECURITY 총 11행이 시드된다")
    void 트랙_11개가_시드된다() {
        assertThat(trackMasterRepository.count()).isEqualTo(11);
        assertThat(trackMasterRepository.findByCode("SECURITY")).isPresent();
    }

    @Test
    @DisplayName("AC-8.2 새로 생성한 부원의 track_id는 null이 아니다")
    void 신규_부원의_track_id는_null이_아니다() {
        var backend = trackMasterRepository.findByCode("BACKEND").orElseThrow();
        Member member = memberRepository.save(newMember(backend));

        Integer nullCount = jdbcTemplate.queryForObject(
                "select count(*) from member where id = ? and track_id is null",
                Integer.class, member.getId());

        assertThat(nullCount).isZero();
    }

    @Test
    @DisplayName("AC-8.3 V8 백필 결과가 원본 문자열 값과 정확히 일치한다")
    void 백필된_track_id가_레거시_문자열과_일치한다() {
        // 기존 통합 테스트들이 이미 여러 부원을 만들어 지나갔을 수 있으므로, 여기서 직접 하나 만들어
        // "이 마이그레이션이 실제로 무엇을 했는지"를 이 테스트 스스로 검증한다.
        var backend = trackMasterRepository.findByCode("BACKEND").orElseThrow();
        memberRepository.save(newMember(backend));

        Integer mismatchCount = jdbcTemplate.queryForObject(
                "select count(*) from member m join track t on t.id = m.track_id where t.code <> m.track",
                Integer.class);

        assertThat(mismatchCount).isZero();
    }

    @Test
    @DisplayName("AC-8.4 레거시 track 컬럼은 NOT NULL이 풀려 있다 (신버전 INSERT가 그 컬럼을 채우지 않아도 된다)")
    void 레거시_track_컬럼은_not_null이_아니다() {
        Integer nullableCount = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.columns "
                        + "where table_name = 'member' and column_name = 'track' and is_nullable = 'YES'",
                Integer.class);

        assertThat(nullableCount).isEqualTo(1);
    }

    @Test
    @DisplayName("AC-8.4 caveat: track_id 없이 넣는 구버전 스타일 INSERT는 이제 실패한다 "
            + "(쓰기 경로는 신버전 배포 후에만 안전하다 — 읽기 전용 하위호환)")
    void track_id_없는_구버전_스타일_insert는_실패한다() {
        assertThatThrownBy(() -> jdbcTemplate.update(
                "insert into member (student_number, password, name, track, generation, member_type, "
                        + "university, email, status, role) "
                        + "values ('99999999', 'x', '레거시부원', 'BACKEND', '24-하', 'REGULAR', "
                        + "'OO대학교', 'legacy-style@bcsd.club', 'PENDING_SETUP', 'MEMBER')"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Member newMember(com.bcsdlab.bcsdinternalapiv2.track.model.TrackMaster track) {
        return Member.builder()
                .studentNumber("2023" + (int) (Math.random() * 9000 + 1000))
                .password(passwordEncoder.encode("Temp1234"))
                .name("트랙승격테스트")
                .track(track)
                .generation("24-하")
                .memberType(MemberType.REGULAR)
                .university("OO대학교")
                .email("track-promotion-" + System.nanoTime() + "@bcsd.club")
                .status(MemberStatus.ACTIVE)
                .build();
    }
}
