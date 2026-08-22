package com.bcsdlab.bcsdinternalapiv2.member.service;

import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.request.AdminMemberProfileUpdateRequest;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.request.MemberDirectoryQuery;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.request.PhotoPresignedUrlRequest;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.response.MemberDirectoryResponse;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.response.PhotoPresignedUrlResponse;
import com.bcsdlab.bcsdinternalapiv2.member.exception.MemberException;
import com.bcsdlab.bcsdinternalapiv2.member.exception.MemberExceptionType;
import com.bcsdlab.bcsdinternalapiv2.member.model.AcademicStatus;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberRole;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberSpecification;
import com.bcsdlab.bcsdinternalapiv2.member.util.GithubIdNormalizer;
import com.bcsdlab.bcsdinternalapiv2.member.util.PhoneNumberNormalizer;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberDirectoryService {

    private final MemberRepository memberRepository;
    private final PhotoStorageService photoStorageService;

    @Transactional(readOnly = true)
    public MemberDirectoryResponse getDirectory(MemberDirectoryQuery query, Pageable pageable) {
        Specification<Member> spec = MemberSpecification.combine(Arrays.asList(
                MemberSpecification.search(query.keyword()),
                MemberSpecification.hasActive(query.active()),
                MemberSpecification.hasAcademicStatusIn(query.academicStatus()),
                MemberSpecification.hasTrackIn(query.track()),
                MemberSpecification.hasMemberTypeIn(query.memberType())
        ));

        Page<Member> page = memberRepository.findAll(spec, pageable);
        return MemberDirectoryResponse.of(page, buildCounts());
    }

    @Transactional
    public void changeAcademicStatus(Long memberId, AcademicStatus academicStatus) {
        Member member = memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new MemberException(MemberExceptionType.MEMBER_NOT_FOUND));
        member.changeAcademicStatus(academicStatus);
    }

    @Transactional
    public void changeActive(Long memberId, boolean active) {
        Member member = memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new MemberException(MemberExceptionType.MEMBER_NOT_FOUND));
        member.changeClubActive(active);
    }

    @Transactional
    public void updateProfile(Long memberId, AdminMemberProfileUpdateRequest request) {
        Member member = memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new MemberException(MemberExceptionType.MEMBER_NOT_FOUND));

        String normalizedEmail = request.email().trim().toLowerCase();
        if (memberRepository.existsByEmailAndIdNot(normalizedEmail, memberId)) {
            throw new MemberException(MemberExceptionType.EMAIL_DUPLICATED);
        }

        String normalizedPhone = isBlank(request.phoneNumber()) ? null
                : PhoneNumberNormalizer.normalize(request.phoneNumber());
        String normalizedGithubId = GithubIdNormalizer.normalize(request.githubId());

        member.updateProfile(request.name(), request.track(), request.generation(), request.memberType(),
                request.university(), request.department(), request.position(), request.birthDate(),
                request.duesRequired(), normalizedEmail, normalizedPhone, normalizedGithubId);
    }

    @Transactional
    public void changeRole(Long memberId, MemberRole role) {
        Member member = memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new MemberException(MemberExceptionType.MEMBER_NOT_FOUND));
        member.changeRole(role);
    }

    @Transactional
    public void changeWithdrawal(Long memberId, boolean withdrawn) {
        Member member = memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new MemberException(MemberExceptionType.MEMBER_NOT_FOUND));
        if (withdrawn) {
            member.withdraw();
        } else {
            member.restore();
        }
    }

    @Transactional(readOnly = true)
    public PhotoPresignedUrlResponse issuePhotoPresignedUrl(Long memberId, PhotoPresignedUrlRequest request) {
        if (!memberRepository.existsById(memberId)) {
            throw new MemberException(MemberExceptionType.MEMBER_NOT_FOUND);
        }
        return photoStorageService.issuePresignedUrl(memberId, request);
    }

    @Transactional
    public void updatePhotoUrl(Long memberId, String photoUrl) {
        Member member = memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new MemberException(MemberExceptionType.MEMBER_NOT_FOUND));
        member.updatePhotoUrl(photoUrl);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private MemberDirectoryResponse.Counts buildCounts() {
        long total = memberRepository.count();
        long active = memberRepository.countByClubActive(true);
        long inactive = total - active;

        Map<String, Long> byAcademicStatus = groupCount(memberRepository.countGroupByAcademicStatus());
        Map<String, Long> byTrack = groupCount(memberRepository.countGroupByTrack());
        Map<String, Long> byMemberType = groupCount(memberRepository.countGroupByMemberType());

        return new MemberDirectoryResponse.Counts(total, active, inactive, byAcademicStatus, byTrack, byMemberType);
    }

    private Map<String, Long> groupCount(List<Object[]> rows) {
        Map<String, Long> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String key = ((Enum<?>) row[0]).name();
            Long value = (Long) row[1];
            result.put(key, value);
        }
        return result;
    }
}
