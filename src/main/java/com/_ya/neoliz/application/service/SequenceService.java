package com._ya.neoliz.application.service;

import com._ya.neoliz.domain.Sequence;
import com._ya.neoliz.domain.SequenceItem;
import com._ya.neoliz.global.exception.ForbiddenException;
import com._ya.neoliz.global.exception.SequenceNotFoundException;
import com._ya.neoliz.persistence.repository.SequenceItemRepository;
import com._ya.neoliz.persistence.repository.SequenceRepository;
import com._ya.neoliz.presentation.dto.response.SequenceDetailResponse;
import com._ya.neoliz.presentation.dto.response.SequenceItemResponse;
import com._ya.neoliz.presentation.dto.response.SequencePageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SequenceService {
    private final SequenceRepository sequenceRepository;
    private final SequenceItemRepository sequenceItemRepository;
    public SequencePageResponse getSequenceList(Long id, Pageable pageable) {
        Page<Sequence> sequencePage = sequenceRepository.findAllByUserId(id, pageable);
        return SequencePageResponse.from(sequencePage);
    }

    public SequenceDetailResponse getSequenceDetail(Long userId, Long sequenceId) {
        Sequence sequence = sequenceRepository.findById(sequenceId)
                .orElseThrow(() -> new SequenceNotFoundException("조회 실패"));
        if (!sequence.getUserId().equals(userId)) {
            throw new ForbiddenException("권한 불일치");
        }
        List<SequenceItem> items = sequenceItemRepository.findBySequenceIdOrderByOrderIndexAsc(sequenceId);
        List<SequenceItemResponse> itemsResponses = items.stream()
                .map(item -> {
                    String soundUrl = "임시 Url";
                    String imageUrl = "임시 Url";
                    return SequenceItemResponse.from(item, soundUrl, imageUrl);
                })
                .toList();
        return SequenceDetailResponse.from(sequence, itemsResponses);
    }

    // 3. 시퀀스 저장
    @Transactional
    public SaveSequenceResponse saveSequence(Long userId, SaveSequenceRequest request) {
        // multiplier 유효성 검증
        List<BigDecimal> validMultipliers = List.of(
                new BigDecimal("0.5"), new BigDecimal("1.0"),
                new BigDecimal("1.5"), new BigDecimal("2.0")
        );
        for (SaveSequenceRequest.ItemRequest item : request.getItems()) {
            if (!validMultipliers.contains(item.getMultiplier())) {
                throw new InvalidMultiplierException("유효하지 않은 배율입니다.");
            }
            if (!emojiRepository.existsById(item.getEmojiId())) {
                throw new EmojiNotFoundException("존재하지 않는 이모지입니다.");
            }
        }

        Sequence sequence = sequenceRepository.save(Sequence.create(userId, request.getTitle()));

        for (int i = 0; i < request.getItems().size(); i++) {
            SaveSequenceRequest.ItemRequest item = request.getItems().get(i);
            sequenceItemRepository.save(SequenceItem.create(sequence.getId(), item.getEmojiId(), i, item.getMultiplier()));
        }

        return SaveSequenceResponse.from(sequence);
    }

    // 4. 시퀀스 삭제
    @Transactional
    public void deleteSequence(Long userId, Long sequenceId) {
        Sequence sequence = sequenceRepository.findById(sequenceId)
                .orElseThrow(() -> new SequenceNotFoundException("조회 실패")); // 404
        if (!sequence.getUserId().equals(userId)) {
            throw new ForbiddenException("권한 불일치"); // 403
        }
        sequenceItemRepository.deleteBySequenceId(sequenceId);
        sequenceRepository.delete(sequence);
    }

}
