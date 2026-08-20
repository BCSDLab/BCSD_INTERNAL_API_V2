package com.bcsdlab.bcsdinternalapiv2.track;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.IntegrationTestSupport;
import com.bcsdlab.bcsdinternalapiv2.auth.repository.RefreshTokenRepository;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberStatus;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberType;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackMaster;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackPage;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackPageMember;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackMasterRepository;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackPageMemberRepository;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackPageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TrackIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private TrackPageRepository trackPageRepository;

    @Autowired
    private TrackMasterRepository trackMasterRepository;

    @Autowired
    private TrackPageMemberRepository trackPageMemberRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private TrackMaster frontend;
    private TrackMaster backend;
    private TrackMaster pm;

    @BeforeEach
    void setUp() {
        trackPageRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        memberRepository.deleteAll();
        frontend = trackMasterRepository.findByCode("FRONTEND").orElseThrow();
        backend = trackMasterRepository.findByCode("BACKEND").orElseThrow();
        pm = trackMasterRepository.findByCode("PM").orElseThrow();
    }

    @Test
    @DisplayName("AC-1.3 공개 목록은 공개된 트랙만 display_order 순으로 반환한다")
    void 공개_목록은_display_order_순이고_숨김_트랙은_제외한다() throws Exception {
        // uq_track_page_track은 track_id 단위 partial unique index라, 활성 상태(미삭제)인
        // track_page는 트랙마다 하나뿐이어야 한다 — 숨김 케이스는 별도 트랙 마스터(pm)를 쓴다.
        trackPageRepository.save(trackPage(backend, "backend", 1, true));
        trackPageRepository.save(trackPage(frontend, "frontend", 0, true));
        trackPageRepository.save(trackPage(pm, "hidden", 2, false));

        mockMvc.perform(get("/v1/tracks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].slug").value("frontend"))
                .andExpect(jsonPath("$[1].slug").value("backend"));
    }

    @Test
    @DisplayName("공개 트랙 상세는 인증 없이 200과 헤더 필드를 반환한다")
    void 공개_트랙_상세는_인증_없이_조회된다() throws Exception {
        trackPageRepository.save(TrackPage.builder()
                .track(frontend)
                .slug("frontend")
                .displayName("Frontend")
                .tagline("사용자 인터페이스를 구축합니다.")
                .heroImageUrl("https://image.bcsdlab.com/hero.png")
                .displayOrder(0)
                .published(true)
                .build());

        mockMvc.perform(get("/v1/tracks/frontend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("frontend"))
                .andExpect(jsonPath("$.name").value("Frontend"))
                .andExpect(jsonPath("$.tagline").value("사용자 인터페이스를 구축합니다."))
                .andExpect(jsonPath("$.heroImageUrl").value("https://image.bcsdlab.com/hero.png"));
    }

    @Test
    @DisplayName("INV-11 숨김 트랙은 slug를 알아도 404다 (존재하지 않는 slug와 구분되지 않는다)")
    void 숨김_트랙은_404다() throws Exception {
        trackPageRepository.save(trackPage(frontend, "hidden-track", 0, false));

        mockMvc.perform(get("/v1/tracks/hidden-track")).andExpect(status().isNotFound());
        mockMvc.perform(get("/v1/tracks/no-such-slug")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("공개 트랙 상세는 숨긴 부원과 비활성 부원을 제외하고 순서대로 반환한다")
    void 공개_상세는_숨김과_비활성_부원을_제외한다() throws Exception {
        TrackPage trackPage = trackPageRepository.save(TrackPage.builder()
                .track(frontend).slug("frontend").displayName("Frontend").tagline("tagline")
                .displayOrder(0).published(true).build());

        Member visible = memberRepository.save(newMember("20240001", "m1@bcsd.club", MemberStatus.ACTIVE));
        Member hidden = memberRepository.save(newMember("20240002", "m2@bcsd.club", MemberStatus.ACTIVE));
        Member withdrawn = memberRepository.save(newMember("20240003", "m3@bcsd.club", MemberStatus.WITHDRAWN));

        trackPageMemberRepository.save(TrackPageMember.builder()
                .trackPage(trackPage).member(hidden).displayOrder(0).visible(false).build());
        trackPageMemberRepository.save(TrackPageMember.builder()
                .trackPage(trackPage).member(visible).displayOrder(1).visible(true).build());
        trackPageMemberRepository.save(TrackPageMember.builder()
                .trackPage(trackPage).member(withdrawn).displayOrder(2).visible(true).build());

        mockMvc.perform(get("/v1/tracks/frontend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members.length()").value(1))
                .andExpect(jsonPath("$.members[0].name").value(visible.getName()));
    }

    private Member newMember(String studentNumber, String email, MemberStatus status) {
        return Member.builder()
                .studentNumber(studentNumber)
                .password("{noop}password")
                .name("테스트" + studentNumber)
                .track(backend)
                .generation("24-하")
                .memberType(MemberType.REGULAR)
                .university("OO대학교")
                .email(email)
                .status(status)
                .build();
    }

    private TrackPage trackPage(TrackMaster track, String slug, int order, boolean published) {
        return TrackPage.builder()
                .track(track)
                .slug(slug)
                .displayName(slug)
                .tagline("tagline")
                .displayOrder(order)
                .published(published)
                .build();
    }
}
