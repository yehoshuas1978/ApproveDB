package com.susswein.approvedb.repository;

import com.susswein.approvedb.model.ChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChangeRequestRepository extends JpaRepository<ChangeRequest, String> {
    List<ChangeRequest> findByStateOrderByReceivedAtDesc(String state);
}
