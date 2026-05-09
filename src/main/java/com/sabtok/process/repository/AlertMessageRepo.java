package com.sabtok.process.repository;

import com.sabtok.process.entity.AlertMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertMessageRepo extends JpaRepository<AlertMessage, Long> {
}
