package com.bcsdlab.bcsdinternalapiv2.member.controller.dto.request;

import com.bcsdlab.bcsdinternalapiv2.member.model.AcademicStatus;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberType;
import com.bcsdlab.bcsdinternalapiv2.member.model.Track;
import java.util.List;

public record MemberDirectoryQuery(
        String keyword,
        Boolean active,
        List<AcademicStatus> academicStatus,
        List<Track> track,
        List<MemberType> memberType
) {
}
