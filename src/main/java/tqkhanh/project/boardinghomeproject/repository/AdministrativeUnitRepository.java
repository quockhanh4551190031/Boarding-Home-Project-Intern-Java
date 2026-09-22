package tqkhanh.project.boardinghomeproject.repository;

import tqkhanh.project.boardinghomeproject.entity.AdministrativeUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AdministrativeUnitRepository extends JpaRepository<AdministrativeUnit, Long> {

    @Query("SELECT DISTINCT a.province FROM AdministrativeUnit a ORDER BY a.province")
    List<String> findDistinctProvinces();

    @Query("SELECT a.name FROM AdministrativeUnit a WHERE a.province = :province ORDER BY a.name")
    List<String> findNamesByProvince(@Param("province") String province);

    boolean existsByNameAndProvince(String name, String province);
}