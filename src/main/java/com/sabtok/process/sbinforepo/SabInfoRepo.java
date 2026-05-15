package com.sabtok.process.sbinforepo;

import com.sabtok.process.domain.PageActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SabInfoRepo extends JpaRepository<PageActivity, String> {
}
