package com.citi.training.portfolioManager.repo;

import com.citi.training.portfolioManager.entities.Future;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BondRepository extends JpaRepository<Future,String> {
}
