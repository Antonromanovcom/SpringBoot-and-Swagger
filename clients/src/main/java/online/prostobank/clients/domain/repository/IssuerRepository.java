/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package online.prostobank.clients.domain.repository;

import online.prostobank.clients.domain.Issuer;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Репозиторий подразделений
 * @author yv
 */
public interface IssuerRepository  extends JpaRepository<Issuer, Long>{
    Issuer findByCode(String code);
}
