package online.prostobank.clients.domain.fns.repository;

import online.prostobank.clients.domain.fns.DocumentFns;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface DocumentFnsRepository extends JpaRepository<DocumentFns, Long> {

    @Query("select d from DocumentFns d where d.infoNpFnsDto.innUl = ?1")
    DocumentFns findByInnUl(String innUl);
}
