package com._ya.neoliz.persistence.repository;

import com._ya.neoliz.domain.SequenceItems;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SequenceItemsRepository extends JpaRepository<SequenceItems, Long> {
    List<SequenceItems> findBySequenceIdOrderByOrderIndexAsc(Long sequenceId);
}
