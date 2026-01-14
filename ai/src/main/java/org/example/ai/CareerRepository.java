package org.example.ai;

import org.example.ai.aiEntity.CareerResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CareerRepository extends JpaRepository<CareerResult, Long> {
}