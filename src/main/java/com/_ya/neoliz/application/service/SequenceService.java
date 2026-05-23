package com._ya.neoliz.application.service;

import com._ya.neoliz.domain.Sequence;
import com._ya.neoliz.persistence.repository.SequenceRepository;
import com._ya.neoliz.presentation.dto.response.SequencePageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SequenceService {
    private final SequenceRepository sequenceRepository;
    public SequencePageResponse getSequenceList(Long id, Pageable pageable) {
        Page<Sequence> sequencePage = sequenceRepository.findAllByUserId(id, pageable);
        return SequencePageResponse.from(sequencePage);
    }
}
