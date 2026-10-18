package com.bcsdlab.bcsdinternalapiv2.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.IntegrationTestSupport;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberRole;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberStatus;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberType;
import com.bcsdlab.bcsdinternalapiv2.member.model.Position;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import com.bcsdlab.bcsdinternalapiv2.member.repository.PositionRepository;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackMaster;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackMasterRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

/** 명부 목록은 회원 수와 무관하게 일정한 쿼리 수로 조회한다(회원마다 직책·트랙을 따로 읽지 않는다). */
@TestPropertySource(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
class MemberDirectoryQueryCountIntegrationTest extends IntegrationTestSupport {

    private static final int MEMBER_COUNT = 20;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private TrackMasterRepository trackMasterRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    void 명부_조회_쿼리_수는_회원_수에_비례하지_않는다() throws Exception {
        List<TrackMaster> tracks = trackMasterRepository.findAll();
        List<Position> positions = positionRepository.findAll();
        for (int i = 0; i < MEMBER_COUNT; i++) {
            memberRepository.save(Member.builder()
                    .studentNumber("2030%04d".formatted(i))
                    .name("명부" + i)
                    .track(tracks.get(i % tracks.size()))
                    .generation("24-하")
                    .memberType(MemberType.REGULAR)
                    .university("한국기술교육대학교")
                    .department("컴퓨터공학부")
                    .positions(positions.isEmpty() ? List.of() : List.of(positions.get(i % positions.size())))
                    .email("dir" + i + "@bcsd.club")
                    .phoneNumber("0100000%04d".formatted(i))
                    .status(MemberStatus.ACTIVE)
                    .role(MemberRole.MEMBER)
                    .build());
        }
        String token = adminToken();

        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();
        mockMvc.perform(get("/v1/admin/members").param("size", "50")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members.length()").value(MEMBER_COUNT + 1));

        assertThat(statistics.getPrepareStatementCount()).isLessThan(15);
    }

    private String adminToken() throws Exception {
        memberRepository.save(Member.builder()
                .studentNumber("20309999")
                .password(passwordEncoder.encode("Temp1234"))
                .name("관리자")
                .track(trackMasterRepository.findByCode("BACKEND").orElseThrow())
                .generation("16")
                .memberType(MemberType.MENTOR)
                .university("한국기술교육대학교")
                .department("컴퓨터공학부")
                .email("admin@bcsd.club")
                .phoneNumber("01099999999")
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.ADMIN)
                .build());
        String body = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentNumber\":\"20309999\",\"password\":\"Temp1234\",\"rememberMe\":false}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return new ObjectMapper().readTree(body).get("accessToken").asText();
    }
}
