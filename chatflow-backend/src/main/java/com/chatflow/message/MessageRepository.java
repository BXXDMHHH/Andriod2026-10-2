package com.chatflow.message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface MessageRepository extends JpaRepository<MessageEntity,Long>{
 List<MessageEntity> findByConversation_IdOrderByIdDesc(Long conversationId,Pageable pageable);
 List<MessageEntity> findByConversation_IdAndIdLessThanOrderByIdDesc(Long conversationId,Long beforeId,Pageable pageable);
}