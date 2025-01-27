package com.example.solidconnection.siteuser.repository;

import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.university.domain.LikedUniversity;
import com.example.solidconnection.university.domain.UniversityInfoForApply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikedUniversityRepository extends JpaRepository<LikedUniversity, Long> {

    List<LikedUniversity> findAllBySiteUser_Id(long id);

    int countBySiteUser_Id(long id);

    Optional<LikedUniversity> findBySiteUserAndUniversityInfoForApply(SiteUser siteUser, UniversityInfoForApply universityInfoForApply);
}
