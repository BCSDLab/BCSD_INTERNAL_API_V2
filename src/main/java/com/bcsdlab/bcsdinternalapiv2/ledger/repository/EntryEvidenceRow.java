package com.bcsdlab.bcsdinternalapiv2.ledger.repository;

import com.bcsdlab.bcsdinternalapiv2.media.model.ImageAsset;

/** 장부 기록에 붙은 증빙 한 건. */
public record EntryEvidenceRow(Long entryId, ImageAsset asset) {
}
