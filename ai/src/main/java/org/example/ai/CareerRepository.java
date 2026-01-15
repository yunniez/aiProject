package org.example.ai;

import org.example.ai.aiEntity.CareerResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CareerRepository extends JpaRepository<CareerResult, Long> {
    // 생성일 기준 내림차순(최신순)으로 모든 데이터를 가져오기
    List<CareerResult> findAllByOrderByCreatedAtDesc();
}