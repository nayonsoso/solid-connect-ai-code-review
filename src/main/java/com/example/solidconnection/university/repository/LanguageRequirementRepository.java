package com.example.solidconnection.university.repository;

import com.example.solidconnection.entity.LanguageRequirement;
import com.example.solidconnection.entity.UniversityInfoForApply;
import com.example.solidconnection.type.LanguageTestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LanguageRequirementRepository extends JpaRepository<LanguageRequirement, Long> {
    List<LanguageRequirement> findAllByUniversityInfoForApply_Id(Long id);

    @Query("SELECT lr FROM LanguageRequirement lr WHERE lr.minScore <= :myScore AND lr.languageTestType = :testType AND lr.universityInfoForApply = :universityInfoForApply ORDER BY lr.minScore ASC")
    Optional<LanguageRequirement> findByUniversityInfoForApplyAndLanguageTestTypeAndLessThanMyScore(@Param("universityInfoForApply") UniversityInfoForApply universityInfoForApply, @Param("testType") LanguageTestType testType, @Param("myScore") String myScore);
}
