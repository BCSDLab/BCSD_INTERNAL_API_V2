package com.bcsdlab.bcsdinternalapiv2.media.repository;

import com.bcsdlab.bcsdinternalapiv2.media.model.ImageAsset;
import com.bcsdlab.bcsdinternalapiv2.media.model.ImagePurpose;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageAssetRepository extends JpaRepository<ImageAsset, Long> {

    Page<ImageAsset> findAllByPurposeAndConfirmedTrueOrderByCreatedAtDesc(ImagePurpose purpose, Pageable pageable);
}
