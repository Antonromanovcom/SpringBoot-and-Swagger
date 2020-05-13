package online.prostobank.clients.domain.repository;

import online.prostobank.clients.domain.LoggingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.NonUniqueResultException;

@Repository
public interface LoggingRecordRepository extends JpaRepository<LoggingRecord, Long> {

    LoggingRecord getAllByReqResPair(String reqResPair) throws NonUniqueResultException;

}