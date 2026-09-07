package com.bcsdlab.bcsdinternalapiv2.media.controller;

import com.bcsdlab.bcsdinternalapiv2.media.controller.dto.request.PresignedUrlRequest;
import com.bcsdlab.bcsdinternalapiv2.media.controller.dto.response.ImageAssetResponse;
import com.bcsdlab.bcsdinternalapiv2.media.controller.dto.response.ImageCompleteResponse;
import com.bcsdlab.bcsdinternalapiv2.media.controller.dto.response.PresignedUrlResponse;
import com.bcsdlab.bcsdinternalapiv2.media.model.ImagePurpose;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Tag(name = "관리자 미디어 API")
public interface AdminImageApi {

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", description = "5MB 초과(AC-4.1) 또는 허용 외 확장자(AC-4.2)"),
    })
    @Operation(summary = "presigned URL 발급", description = "브라우저가 반환된 uploadUrl로 S3에 직접 PUT한다.")
    PresignedUrlResponse issuePresignedUrl(@Valid PresignedUrlRequest request);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 이미지"),
    })
    @Operation(summary = "업로드 완료 등록", description = "S3 PUT이 끝난 뒤 호출해야 라이브러리 목록에 노출된다(AC-4.3, AC-4.4).")
    ImageCompleteResponse complete(Long id);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
    })
    @Operation(summary = "미디어 라이브러리 목록", description = "complete된 이미지만 반환한다(AC-4.4).")
    Page<ImageAssetResponse> getImages(ImagePurpose purpose, Pageable pageable);
}
