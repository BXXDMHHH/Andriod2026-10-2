package com.chatflow.workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface WorkflowRepository extends JpaRepository<WorkflowEntity,Long>{
 List<WorkflowEntity> findByCreatedBy_UsernameAndStatusOrderByUpdatedAtDesc(String username,String status);
 Optional<WorkflowEntity> findByIdAndCreatedBy_Username(Long id,String username);
}
