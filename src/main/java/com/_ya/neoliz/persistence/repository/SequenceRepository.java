package com._ya.neoliz.persistence.repository;

import com._ya.neoliz.domain.Sequence;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SequenceRepository extends JpaRepository<Sequence, Long> {
    Page<Sequence> findAllByUserId(Long id, Pageable pageable);

}
