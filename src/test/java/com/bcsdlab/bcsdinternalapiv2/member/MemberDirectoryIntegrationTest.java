package com.bcsdlab.bcsdinternalapiv2.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.auth.repository.RefreshTokenRepository;
import com.bcsdlab.bcsdinternalapiv2.member.model.AcademicStatus;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberRole;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberStatus;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberType;
import com.bcsdlab.bcsdinternalapiv2.member.model.Track;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class MemberDirectoryIntegrationTest {

    private static final String RAW_PASSWORD = "Temp1234";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void jwtSecret(DynamicPropertyRegistry registry) {
        registry.add("app.jwt.secret", () -> "test-only-secret-key-not-for-production-32bytes-min");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private S3Presigner s3Presigner;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    void 트랙과_구분으로_필터링하면_조건에_맞는_회원만_반환된다() throws Exception {
        String adminToken = createAdminAndLogin("20230001");
        saveMember("20240001", "백엔드멘토", Track.BACKEND, MemberType.MENTOR);
        saveMember("20240002", "프론트레귤러", Track.FRONTEND, MemberType.REGULAR);
        saveMember("20240003", "게임비기너", Track.GAME, MemberType.BEGINNER);

        mockMvc.perform(get("/v1/admin/members")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("track", "BACKEND")
                        .param("memberType", "MENTOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members.length()").value(1))
                .andExpect(jsonPath("$.members[0].studentNumber").value("20240001"));
    }

    @Test
    void 이름_학번_이메일_깃허브아이디로_통합검색이_동작한다() throws Exception {
        String adminToken = createAdminAndLogin("20230002");
        saveMember("20240011", "김검색", Track.BACKEND, MemberType.REGULAR);
        saveMember("20240012", "박다름", Track.BACKEND, MemberType.REGULAR);

        mockMvc.perform(get("/v1/admin/members")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("keyword", "20240011"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members.length()").value(1))
                .andExpect(jsonPath("$.members[0].name").value("김검색"));
    }

    @Test
    void 기수_오름차순_정렬이_적용된다() throws Exception {
        String adminToken = createAdminAndLogin("20230003");
        saveMemberWithGeneration("20240021", "24-하");
        saveMemberWithGeneration("20240022", "20-상");
        saveMemberWithGeneration("20240023", "22-하");

        mockMvc.perform(get("/v1/admin/members")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("keyword", "2024002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members[0].generation").value("20-상"))
                .andExpect(jsonPath("$.members[1].generation").value("22-하"))
                .andExpect(jsonPath("$.members[2].generation").value("24-하"));
    }

    @Test
    void 페이지네이션_경계값에서_올바른_페이지수와_총원이_반환된다() throws Exception {
        String adminToken = createAdminAndLogin("20230004");
        for (int i = 0; i < 9; i++) {
            saveMemberWithGeneration("2024010" + i, "24-하");
        }

        mockMvc.perform(get("/v1/admin/members")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("keyword", "202401")
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members.length()").value(8))
                .andExpect(jsonPath("$.page.totalElements").value(9))
                .andExpect(jsonPath("$.page.totalPages").value(2));

        mockMvc.perform(get("/v1/admin/members")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("keyword", "202401")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members.length()").value(1));
    }

    @Test
    void 활동_여부와_학적_상태_필터가_동시에_적용된다() throws Exception {
        String adminToken = createAdminAndLogin("20230005");
        saveMemberWithStatus("20240031", AcademicStatus.GRADUATED, false);
        saveMemberWithStatus("20240032", AcademicStatus.GRADUATED, true);
        saveMemberWithStatus("20240033", AcademicStatus.ENROLLED, false);

        mockMvc.perform(get("/v1/admin/members")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("keyword", "2024003")
                        .param("active", "false")
                        .param("academicStatus", "GRADUATED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members.length()").value(1))
                .andExpect(jsonPath("$.members[0].studentNumber").value("20240031"));
    }

    @Test
    void 사이드바_카운트는_필터와_무관하게_전체_기준으로_집계된다() throws Exception {
        String adminToken = createAdminAndLogin("20230006");
        saveMember("20240041", "백엔드1", Track.BACKEND, MemberType.REGULAR);
        saveMember("20240042", "프론트1", Track.FRONTEND, MemberType.REGULAR);
        saveMember("20240043", "게임1", Track.GAME, MemberType.REGULAR);

        mockMvc.perform(get("/v1/admin/members")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("track", "BACKEND"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members.length()").value(1))
                .andExpect(jsonPath("$.counts.total").value(4))
                .andExpect(jsonPath("$.counts.active").value(4));
    }

    @Test
    void 관리자는_학적_상태와_활동_여부를_변경할_수_있다() throws Exception {
        String adminToken = createAdminAndLogin("20230007");
        Member target = saveMember("20240051", "상태변경대상", Track.BACKEND, MemberType.REGULAR);

        mockMvc.perform(patch("/v1/admin/members/{memberId}/academic-status", target.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"academicStatus":"GRADUATED"}
                                """))
                .andExpect(status().isNoContent());

        mockMvc.perform(patch("/v1/admin/members/{memberId}/active", target.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"active":false}
                                """))
                .andExpect(status().isNoContent());

        Member updated = memberRepository.findById(target.getId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(updated.getAcademicStatus()).isEqualTo(AcademicStatus.GRADUATED);
        org.assertj.core.api.Assertions.assertThat(updated.isClubActive()).isFalse();
    }

    @Test
    void 일반_회원_권한으로는_학적_상태_및_활동여부를_변경할_수_없다() throws Exception {
        String memberToken = createMemberAndLogin("20230008");
        Member target = saveMember("20240061", "권한없음대상", Track.BACKEND, MemberType.REGULAR);

        mockMvc.perform(patch("/v1/admin/members/{memberId}/academic-status", target.getId())
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"academicStatus":"GRADUATED"}
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/v1/admin/members/{memberId}/active", target.getId())
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"active":false}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void 일반_회원도_인명부_조회_API로_전화번호_학번을_포함한_전체_정보를_조회할_수_있다() throws Exception {
        String memberToken = createMemberAndLogin("20230009");
        saveMember("20240071", "조회대상", Track.BACKEND, MemberType.REGULAR);

        mockMvc.perform(get("/v1/members/directory")
                        .header("Authorization", "Bearer " + memberToken)
                        .param("keyword", "20240071"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members[0].studentNumber").value("20240071"))
                .andExpect(jsonPath("$.members[0].phoneNumber").value("010240071"));
    }

    @Test
    void 비로그인_요청은_401이_반환된다() throws Exception {
        mockMvc.perform(get("/v1/admin/members"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/v1/members/directory"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 관리자는_회원_프로필을_수정할_수_있다() throws Exception {
        String adminToken = createAdminAndLogin("20230010");
        Member target = saveMember("20240081", "수정전이름", Track.BACKEND, MemberType.REGULAR);

        mockMvc.perform(patch("/v1/admin/members/{memberId}", target.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"수정후이름","track":"FRONTEND","memberType":"MENTOR",
                                 "generation":"24-하","university":"한국기술교육대학교","department":"전자공학부",
                                 "position":"회장","birthDate":"2000-01-01","duesRequired":true,
                                 "email":"updated-%s@bcsd.club","phoneNumber":"010-9999-9999","githubId":"updated-id"}
                                """.formatted(target.getId())))
                .andExpect(status().isNoContent());

        Member updated = memberRepository.findById(target.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("수정후이름");
        assertThat(updated.getTrack()).isEqualTo(Track.FRONTEND);
        assertThat(updated.getMemberType()).isEqualTo(MemberType.MENTOR);
        assertThat(updated.getDepartment()).isEqualTo("전자공학부");
        assertThat(updated.getPosition()).isEqualTo("회장");
        assertThat(updated.isDuesRequired()).isTrue();
        assertThat(updated.getPhoneNumber()).isEqualTo("01099999999");
        assertThat(updated.getGithubId()).isEqualTo("updated-id");
    }

    @Test
    void 관리자는_회원_권한을_변경할_수_있다() throws Exception {
        String adminToken = createAdminAndLogin("20230011");
        Member target = saveMember("20240082", "권한변경대상", Track.BACKEND, MemberType.REGULAR);

        mockMvc.perform(patch("/v1/admin/members/{memberId}/role", target.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"role":"ADMIN"}
                                """))
                .andExpect(status().isNoContent());

        assertThat(memberRepository.findById(target.getId()).orElseThrow().getRole()).isEqualTo(MemberRole.ADMIN);
    }

    @Test
    void 관리자는_회원을_탈퇴_처리하고_복구할_수_있다() throws Exception {
        String adminToken = createAdminAndLogin("20230012");
        Member target = saveMember("20240083", "탈퇴대상", Track.BACKEND, MemberType.REGULAR);

        mockMvc.perform(patch("/v1/admin/members/{memberId}/withdrawal", target.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"withdrawn":true}
                                """))
                .andExpect(status().isNoContent());
        assertThat(memberRepository.findById(target.getId()).orElseThrow().isWithdrawn()).isTrue();

        mockMvc.perform(patch("/v1/admin/members/{memberId}/withdrawal", target.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"withdrawn":false}
                                """))
                .andExpect(status().isNoContent());
        assertThat(memberRepository.findById(target.getId()).orElseThrow().isActive()).isTrue();
    }

    @Test
    void 일반_회원_권한으로는_프로필_수정_권한변경_탈퇴처리를_할_수_없다() throws Exception {
        String memberToken = createMemberAndLogin("20230013");
        Member target = saveMember("20240084", "권한없음대상2", Track.BACKEND, MemberType.REGULAR);

        mockMvc.perform(patch("/v1/admin/members/{memberId}", target.getId())
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"이름","track":"BACKEND","memberType":"REGULAR","generation":"24-하",
                                 "university":"한국기술교육대학교","department":"컴퓨터공학부","duesRequired":false,
                                 "email":"x-%s@bcsd.club"}
                                """.formatted(target.getId())))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/v1/admin/members/{memberId}/role", target.getId())
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"role":"ADMIN"}
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/v1/admin/members/{memberId}/withdrawal", target.getId())
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"withdrawn":true}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void 관리자는_사진_업로드용_presigned_URL을_발급받을_수_있다() throws Exception {
        PresignedPutObjectRequest presignedRequest = mock(PresignedPutObjectRequest.class);
        when(presignedRequest.url())
                .thenReturn(URI.create("https://bcsd-internal.s3.us-west-2.amazonaws.com/member-photos/test").toURL());
        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presignedRequest);

        String adminToken = createAdminAndLogin("20230014");
        Member target = saveMember("20240085", "사진대상", Track.BACKEND, MemberType.REGULAR);

        mockMvc.perform(post("/v1/admin/members/{memberId}/photo/presigned-url", target.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fileName":"photo.png","contentType":"image/png","byteSize":12345}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uploadUrl").isNotEmpty())
                .andExpect(jsonPath("$.publicUrl").isNotEmpty());
    }

    @Test
    void 관리자는_업로드된_사진_URL을_회원_프로필에_등록할_수_있다() throws Exception {
        String adminToken = createAdminAndLogin("20230015");
        Member target = saveMember("20240086", "사진등록대상", Track.BACKEND, MemberType.REGULAR);

        mockMvc.perform(patch("/v1/admin/members/{memberId}/photo", target.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"photoUrl":"https://image.bcsdlab.com/member-photos/1/abc.png"}
                                """))
                .andExpect(status().isNoContent());

        assertThat(memberRepository.findById(target.getId()).orElseThrow().getPhotoUrl())
                .isEqualTo("https://image.bcsdlab.com/member-photos/1/abc.png");
    }

    private String createAdminAndLogin(String studentNumber) throws Exception {
        Member admin = Member.builder()
                .studentNumber(studentNumber)
                .password(passwordEncoder.encode(RAW_PASSWORD))
                .name("관리자")
                .track(Track.PM)
                .generation("16")
                .memberType(MemberType.MENTOR)
                .university("한국기술교육대학교")
                .department("컴퓨터공학부")
                .email(studentNumber + "@bcsd.club")
                .phoneNumber("010" + studentNumber.substring(2))
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.ADMIN)
                .build();
        memberRepository.save(admin);
        return login(studentNumber);
    }

    private String createMemberAndLogin(String studentNumber) throws Exception {
        Member member = Member.builder()
                .studentNumber(studentNumber)
                .password(passwordEncoder.encode(RAW_PASSWORD))
                .name("일반회원")
                .track(Track.BACKEND)
                .generation("16")
                .memberType(MemberType.REGULAR)
                .university("한국기술교육대학교")
                .department("컴퓨터공학부")
                .email(studentNumber + "@bcsd.club")
                .phoneNumber("010" + studentNumber.substring(2))
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.MEMBER)
                .build();
        memberRepository.save(member);
        return login(studentNumber);
    }

    private String login(String studentNumber) throws Exception {
        String loginBody = """
                {"studentNumber":"%s","password":"%s","rememberMe":false}
                """.formatted(studentNumber, RAW_PASSWORD);

        String responseBody = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(responseBody).get("accessToken").asText();
    }

    private Member saveMember(String studentNumber, String name, Track track, MemberType memberType) {
        return saveMemberFull(studentNumber, name, track, memberType, "24-하",
                AcademicStatus.ENROLLED, true);
    }

    private Member saveMemberWithGeneration(String studentNumber, String generation) {
        return saveMemberFull(studentNumber, "기수테스트", Track.BACKEND, MemberType.REGULAR, generation,
                AcademicStatus.ENROLLED, true);
    }

    private Member saveMemberWithStatus(String studentNumber, AcademicStatus academicStatus, boolean active) {
        return saveMemberFull(studentNumber, "상태테스트", Track.BACKEND, MemberType.REGULAR, "24-하",
                academicStatus, active);
    }

    private Member saveMemberFull(String studentNumber, String name, Track track, MemberType memberType,
                                   String generation, AcademicStatus academicStatus, boolean active) {
        Member member = Member.builder()
                .studentNumber(studentNumber)
                .password(passwordEncoder.encode(RAW_PASSWORD))
                .name(name)
                .track(track)
                .generation(generation)
                .memberType(memberType)
                .university("한국기술교육대학교")
                .department("컴퓨터공학부")
                .academicStatus(academicStatus)
                .clubActive(active)
                .email(studentNumber + "@bcsd.club")
                .phoneNumber("010" + studentNumber.substring(2))
                .status(MemberStatus.ACTIVE)
                .build();
        return memberRepository.save(member);
    }
}
