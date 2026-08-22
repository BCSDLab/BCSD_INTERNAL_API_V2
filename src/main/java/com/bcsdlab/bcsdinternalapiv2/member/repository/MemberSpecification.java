package com.bcsdlab.bcsdinternalapiv2.member.repository;

import com.bcsdlab.bcsdinternalapiv2.member.model.AcademicStatus;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberType;
import com.bcsdlab.bcsdinternalapiv2.member.model.Track;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class MemberSpecification {

    private MemberSpecification() {
    }

    public static Specification<Member> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        String pattern = "%" + keyword.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), pattern),
                cb.like(cb.lower(root.get("studentNumber")), pattern),
                cb.like(cb.lower(root.get("email")), pattern),
                cb.like(cb.lower(root.get("githubId")), pattern)
        );
    }

    public static Specification<Member> hasActive(Boolean active) {
        return active == null ? null : (root, query, cb) -> cb.equal(root.get("clubActive"), active);
    }

    public static Specification<Member> hasAcademicStatusIn(List<AcademicStatus> statuses) {
        return (statuses == null || statuses.isEmpty()) ? null
                : (root, query, cb) -> root.get("academicStatus").in(statuses);
    }

    public static Specification<Member> hasTrackIn(List<Track> tracks) {
        return (tracks == null || tracks.isEmpty()) ? null
                : (root, query, cb) -> root.get("track").in(tracks);
    }

    public static Specification<Member> hasMemberTypeIn(List<MemberType> memberTypes) {
        return (memberTypes == null || memberTypes.isEmpty()) ? null
                : (root, query, cb) -> root.get("memberType").in(memberTypes);
    }

    public static Specification<Member> combine(List<Specification<Member>> specs) {
        List<Specification<Member>> nonNull = new ArrayList<>();
        for (Specification<Member> spec : specs) {
            if (spec != null) {
                nonNull.add(spec);
            }
        }
        return Specification.allOf(nonNull);
    }
}
