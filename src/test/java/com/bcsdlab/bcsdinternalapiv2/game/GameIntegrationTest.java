package com.bcsdlab.bcsdinternalapiv2.game;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.IntegrationTestSupport;
import com.bcsdlab.bcsdinternalapiv2.auth.repository.RefreshTokenRepository;
import com.bcsdlab.bcsdinternalapiv2.game.model.Game;
import com.bcsdlab.bcsdinternalapiv2.game.model.GameBuild;
import com.bcsdlab.bcsdinternalapiv2.game.model.GameBuildStatus;
import com.bcsdlab.bcsdinternalapiv2.game.model.GameMember;
import com.bcsdlab.bcsdinternalapiv2.game.repository.GameBuildRepository;
import com.bcsdlab.bcsdinternalapiv2.game.repository.GameMemberRepository;
import com.bcsdlab.bcsdinternalapiv2.game.repository.GameRepository;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberStatus;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberType;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackMaster;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackMasterRepository;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

class GameIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GameBuildRepository gameBuildRepository;

    @Autowired
    private GameMemberRepository gameMemberRepository;

    @Autowired
    private TrackMasterRepository trackMasterRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private TrackMaster gameTrack;
    private TrackMaster backend;

    @BeforeEach
    void setUp() {
        gameRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        memberRepository.deleteAll();
        gameTrack = trackMasterRepository.findByCode("GAME").orElseThrow();
        backend = trackMasterRepository.findByCode("BACKEND").orElseThrow();
    }

    @Test
    @DisplayName("AC-9.3 공개 목록은 공개된 게임만 display_order 순으로 반환한다")
    void 공개_목록은_display_order_순이고_숨김_게임은_제외한다() throws Exception {
        gameRepository.save(game("pixel-farm", 1, true));
        gameRepository.save(game("neon-drift", 0, true));
        gameRepository.save(game("hidden-game", 2, false));

        mockMvc.perform(get("/v1/games"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].slug").value("neon-drift"))
                .andExpect(jsonPath("$[1].slug").value("pixel-farm"));
    }

    @Test
    @DisplayName("AC-9.3 숨김 게임은 slug를 알아도 404다 (존재하지 않는 slug와 구분되지 않는다)")
    void 숨김_게임은_404다() throws Exception {
        gameRepository.save(game("hidden-game", 0, false));

        mockMvc.perform(get("/v1/games/hidden-game")).andExpect(status().isNotFound());
        mockMvc.perform(get("/v1/games/no-such-slug")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("AC-9.10 활성 빌드가 없는 게임의 공개 응답은 activeBuild가 null이고 500이 아니다")
    void 활성_빌드가_없으면_activeBuild가_null이다() throws Exception {
        gameRepository.save(game("neon-drift", 0, true));

        mockMvc.perform(get("/v1/games/neon-drift"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeBuild").doesNotExist());
    }

    @Test
    @DisplayName("AC-9.10 활성 빌드가 있으면 공개 응답의 activeBuild에 버전과 상태가 담긴다")
    void 활성_빌드가_있으면_버전과_상태가_노출된다() throws Exception {
        Game game = gameRepository.save(game("neon-drift", 0, true));
        gameBuildRepository.save(GameBuild.builder()
                .game(game).version("1.1.0").status(GameBuildStatus.ARCHIVED).uploadedAt(Instant.now()).build());
        gameBuildRepository.save(GameBuild.builder()
                .game(game).version("1.2.0").status(GameBuildStatus.ACTIVE).uploadedAt(Instant.now()).build());

        mockMvc.perform(get("/v1/games/neon-drift"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeBuild.version").value("1.2.0"))
                .andExpect(jsonPath("$.activeBuild.status").value("ACTIVE"))
                .andExpect(jsonPath("$.activeBuild.buildFileUrl").doesNotExist());
    }

    @Test
    @DisplayName("AC-9.14 탈퇴한 참여 멤버는 공개 응답의 members에서 사라진다")
    void 탈퇴한_참여_멤버는_응답에서_제외된다() throws Exception {
        Game game = gameRepository.save(game("neon-drift", 0, true));
        Member active = memberRepository.save(newMember("20240001", "m1@bcsd.club", MemberStatus.ACTIVE));
        Member withdrawn = memberRepository.save(newMember("20240002", "m2@bcsd.club", MemberStatus.WITHDRAWN));

        gameMemberRepository.save(GameMember.builder().game(game).member(active).displayOrder(0).build());
        gameMemberRepository.save(GameMember.builder().game(game).member(withdrawn).displayOrder(1).build());

        mockMvc.perform(get("/v1/games/neon-drift"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members.length()").value(1))
                .andExpect(jsonPath("$.members[0].name").value(active.getName()));
    }

    @Test
    @DisplayName("INV-18 삭제되지 않은 게임의 slug는 유일하다")
    void slug는_유일하다() {
        gameRepository.saveAndFlush(game("neon-drift", 0, true));

        assertThatThrownBy(() -> gameRepository.saveAndFlush(game("neon-drift", 1, true)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("공개 게임 상세는 제작 트랙 이름을 함께 반환한다")
    void 공개_게임_상세는_트랙명을_반환한다() throws Exception {
        gameRepository.save(Game.builder()
                .slug("neon-drift").name("Neon Drift").oneLiner("밤의 도시를 달리는 드리프트 레이서")
                .track(gameTrack).teamLabel("1팀").displayOrder(0).published(true).build());

        mockMvc.perform(get("/v1/games/neon-drift"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trackName").value(gameTrack.getName()))
                .andExpect(jsonPath("$.teamLabel").value("1팀"));
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

    private Game game(String slug, int order, boolean published) {
        return Game.builder()
                .slug(slug)
                .name(slug)
                .oneLiner("한 줄 소개")
                .track(gameTrack)
                .displayOrder(order)
                .published(published)
                .build();
    }
}
