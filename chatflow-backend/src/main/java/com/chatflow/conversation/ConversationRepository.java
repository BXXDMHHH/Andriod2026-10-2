package com.chatflow.conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface ConversationRepository extends JpaRepository<ConversationEntity,Long>{
 List<ConversationEntity> findByOwner_UsernameOrderByUpdatedAtDesc(String username);
 Optional<ConversationEntity> findByIdAndOwner_Username(Long id,String username);
}