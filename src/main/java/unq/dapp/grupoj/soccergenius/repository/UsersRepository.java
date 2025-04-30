package unq.dapp.grupoj.soccergenius.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import unq.dapp.grupoj.soccergenius.model.AppUser;

public interface UsersRepository extends JpaRepository<AppUser, Long> {
    AppUser findByEmail(String email);
    boolean existsByEmail(String email);
}