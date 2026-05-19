package com._ya.neoliz.persistence.repository;

import com._ya.neoliz.domain.Sequence;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SequenceRepository extends JpaRepository<Sequence, Long> {

}
