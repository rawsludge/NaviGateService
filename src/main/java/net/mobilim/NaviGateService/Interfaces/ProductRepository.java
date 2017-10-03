package net.mobilim.NaviGateService.Interfaces;

import net.mobilim.NaviGateData.Entities.Product;
import org.hibernate.annotations.Parameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;

@Transactional
@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    @Query("SELECT p FROM Product p WHERE p.sailingDate BETWEEN :fromDate AND :toDate")
    Product findBySailingDate(Date fromDate, Date toDate);
}
