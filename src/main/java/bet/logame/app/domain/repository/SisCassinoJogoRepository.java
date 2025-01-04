package bet.logame.app.domain.repository;

import bet.logame.app.domain.SisCassinoJogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SisCassinoJogoRepository extends JpaRepository<SisCassinoJogo, Integer> {
    Optional<SisCassinoJogo> findByGameidAndProvedor(String gameid, String provedor);
}
