package com._ya.neoliz.persistence.repository;

import com._ya.neoliz.domain.SequenceItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SequenceItemRepository extends JpaRepository<SequenceItem, Long> {
    List<SequenceItem> findBySequenceIdOrderByOrderIndexAsc(Long sequenceId);
    void deleteBySequenceId(Long sequenceId);
}
