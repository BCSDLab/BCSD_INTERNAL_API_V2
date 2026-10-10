package com.bcsdlab.bcsdinternalapiv2.ledger.controller;

import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request.RosterAddRequest;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request.RosterUpdateRequest;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.RosterCandidateListResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.RosterMemberResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.SemesterRosterResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "관리자 - 회비 명단 API")
public interface AdminDuesRosterApi {

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "학기 명단 조회", description = "학기 명단을 이름·학번 순으로 돌려줍니다. applicable이 false면 납부 비대상입니다.")
    @SecurityRequirement(name = "JWT")
    @GetMapping
    ResponseEntity<SemesterRosterResponse> getRoster(@PathVariable String semesterId);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "명단 추가 후보 조회", description = "이 학기 명단에 없는 모든 회원입니다(회원 상태·납부 여부와 무관). "
            + "검색은 클라이언트에서 합니다.")
    @SecurityRequirement(name = "JWT")
    @GetMapping("/candidates")
    ResponseEntity<RosterCandidateListResponse> getCandidates(@PathVariable String semesterId);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "명단에 회원 추가", description = "학기 중 가입자 등을 명단에 추가합니다. 지난 학기도 추가할 수 있습니다(마감 없음). "
            + "이미 명단에 있으면 409입니다.")
    @SecurityRequirement(name = "JWT")
    @PostMapping
    ResponseEntity<RosterMemberResponse> addRosterMember(@PathVariable String semesterId,
                                                         @RequestBody @Valid RosterAddRequest request);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "납부 대상 여부 정정", description = "명단 회원의 납부 대상 여부를 바꿉니다. 지난 학기도 정정할 수 있습니다. "
            + "명단에서 빼는 기능은 없으며, 잘못 들어간 회원은 납부 비대상(applicable=false)으로 둡니다.")
    @SecurityRequirement(name = "JWT")
    @PatchMapping("/{memberId}")
    ResponseEntity<RosterMemberResponse> updateRosterMember(@PathVariable String semesterId,
                                                            @PathVariable Long memberId,
                                                            @RequestBody @Valid RosterUpdateRequest request);
}
