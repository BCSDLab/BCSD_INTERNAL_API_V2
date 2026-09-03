package com.bcsdlab.bcsdinternalapiv2.game.repository;

import com.bcsdlab.bcsdinternalapiv2.game.model.GameMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameMemberRepository extends JpaRepository<GameMember, Long> {

    List<GameMember> findAllByGame_IdOrderByDisplayOrderAsc(Long gameId);

    boolean existsByGame_IdAndMember_Id(Long gameId, Long memberId);

    Optional<GameMember> findByGame_IdAndMember_Id(Long gameId, Long memberId);
}
