package com.bcsdlab.bcsdinternalapiv2.home.service;

import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.OrderRequest;
import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.PublishRequest;
import com.bcsdlab.bcsdinternalapiv2.global.event.ContentChangedPublisher;
import com.bcsdlab.bcsdinternalapiv2.global.util.DisplayOrders;
import com.bcsdlab.bcsdinternalapiv2.home.controller.dto.request.QnaCreateRequest;
import com.bcsdlab.bcsdinternalapiv2.home.controller.dto.request.QnaUpdateRequest;
import com.bcsdlab.bcsdinternalapiv2.home.controller.dto.response.AdminQnaResponse;
import com.bcsdlab.bcsdinternalapiv2.home.exception.HomeException;
import com.bcsdlab.bcsdinternalapiv2.home.exception.HomeExceptionType;
import com.bcsdlab.bcsdinternalapiv2.home.model.QnaItem;
import com.bcsdlab.bcsdinternalapiv2.home.repository.QnaItemRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminQnaService {

    private final QnaItemRepository qnaItemRepository;
    private final ContentChangedPublisher contentChangedPublisher;

    public List<AdminQnaResponse> getQnaItems() {
        return qnaItemRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(AdminQnaResponse::from)
                .toList();
    }

    @Transactional
    public AdminQnaResponse createQnaItem(QnaCreateRequest request) {
        QnaItem saved = qnaItemRepository.save(QnaItem.builder()
                .question(request.question())
                .answer(request.answer())
                .displayOrder((int) qnaItemRepository.count())
                .published(true)
                .build());
        contentChangedPublisher.homeChanged();
        return AdminQnaResponse.from(saved);
    }

    @Transactional
    public AdminQnaResponse updateQnaItem(Long id, QnaUpdateRequest request) {
        QnaItem item = findOrThrow(id);
        item.update(request.question(), request.answer());
        contentChangedPublisher.homeChanged();
        return AdminQnaResponse.from(item);
    }

    @Transactional
    public void deleteQnaItem(Long id) {
        QnaItem item = findOrThrow(id);
        qnaItemRepository.delete(item);
        contentChangedPublisher.homeChanged();
    }

    @Transactional
    public void publish(Long id, PublishRequest request) {
        QnaItem item = findOrThrow(id);
        item.updatePublished(request.isPublished());
        contentChangedPublisher.homeChanged();
    }

    @Transactional
    public void reorder(OrderRequest request) {
        List<QnaItem> items = qnaItemRepository.findAll();
        Map<Long, QnaItem> byId = items.stream().collect(Collectors.toMap(QnaItem::getId, item -> item));

        Map<Long, Integer> newOrders = DisplayOrders.reassign(request.ids(), byId.keySet());
        newOrders.forEach((id, order) -> byId.get(id).updateDisplayOrder(order));
        contentChangedPublisher.homeChanged();
    }

    private QnaItem findOrThrow(Long id) {
        return qnaItemRepository.findById(id)
                .orElseThrow(() -> new HomeException(HomeExceptionType.QNA_NOT_FOUND));
    }
}
