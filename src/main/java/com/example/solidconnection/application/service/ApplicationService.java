package com.example.solidconnection.application.service;

import com.example.solidconnection.application.dto.*;
import com.example.solidconnection.application.repository.ApplicationRepository;
import com.example.solidconnection.constants.NicknameForApplyWords;
import com.example.solidconnection.custom.exception.CustomException;
import com.example.solidconnection.entity.Application;
import com.example.solidconnection.entity.SiteUser;
import com.example.solidconnection.entity.University;
import com.example.solidconnection.entity.UniversityInfoForApply;
import com.example.solidconnection.siteuser.service.SiteUserValidator;
import com.example.solidconnection.type.ApplicationStatusResponse;
import com.example.solidconnection.type.CountryCode;
import com.example.solidconnection.type.RegionCode;
import com.example.solidconnection.type.VerifyStatus;
import com.example.solidconnection.university.repository.UniversityInfoForApplyRepository;
import com.example.solidconnection.university.repository.custom.UniversityRepositoryForFilterImpl;
import com.example.solidconnection.university.service.UniversityValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

import static com.example.solidconnection.constants.Constants.APPLICATION_UPDATE_COUNT_LIMIT;
import static com.example.solidconnection.constants.Constants.TERM;
import static com.example.solidconnection.custom.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final UniversityInfoForApplyRepository universityInfoForApplyRepository;
    private final UniversityValidator universityValidator;
    private final SiteUserValidator siteUserValidator;
    private final ApplicationValidator applicationValidator;
    private final UniversityRepositoryForFilterImpl universityRepositoryForFilter;

    public boolean submitScore(String email, ScoreRequestDto scoreRequestDto) {
        SiteUser siteUser = siteUserValidator.getValidatedSiteUserByEmail(email);

        // 수정
        if (applicationRepository.existsBySiteUser_Email(email)) {
            Application application = applicationValidator.getValidatedApplicationBySiteUser_Email(email);
            application.setGpa(scoreRequestDto.getGpa());
            application.setGpaCriteria(scoreRequestDto.getGpaCriteria());
            application.setGpaReportUrl(scoreRequestDto.getGpaReportUrl());
            application.setLanguageTestScore(scoreRequestDto.getLanguageTestScore());
            application.setLanguageTestType(scoreRequestDto.getLanguageTestType());
            application.setLanguageTestReportUrl(scoreRequestDto.getLanguageTestReportUrl());
            application.setVerifyStatus(VerifyStatus.PENDING);
            return true;
        }

        // 최초 등록
        Application application = Application.saveScore(siteUser, scoreRequestDto);
        applicationRepository.save(application);
        return true;
    }

    public boolean submitUniversityChoice(String email, UniversityRequestDto universityRequestDto) {
        // 수정 횟수 초과 에러 처리
        Application application = applicationValidator.getValidatedApplicationBySiteUser_Email(email);
        if (application.getUpdateCount() > APPLICATION_UPDATE_COUNT_LIMIT) {
            throw new CustomException(APPLY_UPDATE_LIMIT_EXCEED);
        }

        // 저장에 필요한 엔티티 불러오기 or 생성
        UniversityInfoForApply firstChoiceUniversity = universityValidator.getValidatedUniversityInfoForApplyById(universityRequestDto.getFirstChoiceUniversityId());
        UniversityInfoForApply secondChoiceUniversity;
        try {
            secondChoiceUniversity = universityValidator.getValidatedUniversityInfoForApplyById(universityRequestDto.getSecondChoiceUniversityId());
        } catch (Exception e) {
            secondChoiceUniversity = null;
        }

        // 1,2 동일한 대학교 지망 에러 처리
        if (secondChoiceUniversity != null && Objects.equals(secondChoiceUniversity.getId(), firstChoiceUniversity.getId())) {
            throw new CustomException(CANT_APPLY_FOR_SAME_UNIVERSITY);
        }


        // 수정이면 update count 1 증가
        if (application.getFirstChoiceUniversity() != null) {
            application.setUpdateCount(application.getUpdateCount() + 1);
        }

        // 수정
        application.setFirstChoiceUniversity(firstChoiceUniversity);
        application.setSecondChoiceUniversity(secondChoiceUniversity);

        // 새로운 닉네임 부여
        String randomNickname = makeRandomNickname();
        while (applicationRepository.existsByNicknameForApply(randomNickname)) {
            randomNickname = makeRandomNickname();
        }
        application.setNicknameForApply(randomNickname);

        return true;
    }

    private String makeRandomNickname() {
        Random random = new Random();
        int randomIndex1 = random.nextInt(NicknameForApplyWords.adjectives.size());
        String randomAdjective = NicknameForApplyWords.adjectives.get(randomIndex1);
        int randomIndex2 = random.nextInt(NicknameForApplyWords.nouns.size());
        String randomNoun = NicknameForApplyWords.nouns.get(randomIndex2);
        return randomAdjective + " " + randomNoun;
    }

    public ApplicationsDto getApplicants(String email, String region, String keyword) {
        // 유저 검증
        SiteUser siteUser = siteUserValidator.getValidatedSiteUserByEmail(email);
        // 지원했는지 검증
        Application application = applicationValidator.getValidatedApplicationBySiteUser_Email(email);
        // 승인되었는지 확인
        validateApproved(application);

        RegionCode regionCode = null;
        if (region != null && !region.isBlank()) {
            regionCode = RegionCode.getRegionCodeByKoreanName(region);
        }
        List<CountryCode> countryCodes = null;
        if (keyword != null && !keyword.isBlank()) {
            countryCodes = CountryCode.getCountryCodeMatchesToKeyword(keyword);
        }

        List<University> universities = universityRepositoryForFilter.findByRegionAndCountryAndKeyword(regionCode, countryCodes, keyword);
        List<UniversityApplicantsDto> firstChoiceApplicants = getFirstChoiceApplicants(universities, siteUser);
        List<UniversityApplicantsDto> secondChoiceApplicants = getSecondChoiceApplicants(universities, siteUser);
        return ApplicationsDto.builder()
                .firstChoice(firstChoiceApplicants)
                .secondChoice(secondChoiceApplicants)
                .build();
    }

    private void validateApproved(Application application) {
        if (application.getVerifyStatus() != VerifyStatus.APPROVED) {
            throw new CustomException(APPLICATION_NOT_APPROVED);
        }
    }

    private List<UniversityApplicantsDto> getFirstChoiceApplicants(List<University> universities, SiteUser siteUser) {
        return universities.stream()
                .filter(university -> universityInfoForApplyRepository.existsByUniversityAndTerm(university, TERM))
                .map(university -> {
                    UniversityInfoForApply universityInfoForApply = universityValidator.getValidatedUniversityInfoForApplyByUniversity(university);
                    List<Application> firstChoiceApplication = applicationRepository.findAllByFirstChoiceUniversityAndVerifyStatus(universityInfoForApply, VerifyStatus.APPROVED);
                    List<ApplicantDto> firstChoiceApplicant = firstChoiceApplication.stream()
                            .map(ap -> ApplicantDto.fromEntity(ap, Objects.equals(siteUser.getId(), ap.getSiteUser().getId())))
                            .toList();
                    return UniversityApplicantsDto.builder()
                            .koreanName(university.getKoreanName())
                            .studentCapacity(universityInfoForApply.getStudentCapacity())
                            .region(university.getRegion().getCode().getKoreanName())
                            .country(university.getCountry().getCode().getKoreanName())
                            .applicants(firstChoiceApplicant)
                            .build();
                })
                .toList();
    }

    private List<UniversityApplicantsDto> getSecondChoiceApplicants(List<University> universities, SiteUser siteUser) {
        return universities.stream()
                .filter(university -> universityInfoForApplyRepository.existsByUniversityAndTerm(university, TERM))
                .map(university -> {
                    UniversityInfoForApply universityInfoForApply = universityValidator.getValidatedUniversityInfoForApplyByUniversity(university);
                    List<Application> secondChoiceApplication = applicationRepository.findAllBySecondChoiceUniversityAndVerifyStatus(universityInfoForApply, VerifyStatus.APPROVED);
                    List<ApplicantDto> secondChoiceApplicant = secondChoiceApplication.stream()
                            .map(ap -> ApplicantDto.fromEntity(ap, Objects.equals(siteUser.getId(), ap.getSiteUser().getId())))
                            .toList();
                    return UniversityApplicantsDto.builder()
                            .koreanName(university.getKoreanName())
                            .studentCapacity(universityInfoForApply.getStudentCapacity())
                            .region(university.getRegion().getCode().getKoreanName())
                            .country(university.getCountry().getCode().getKoreanName())
                            .applicants(secondChoiceApplicant)
                            .build();
                })
                .toList();
    }

    public VerifyStatusDto getVerifyStatus(String email) {
        SiteUser siteUser = siteUserValidator.getValidatedSiteUserByEmail(email);
        Optional<Application> application = applicationRepository.findBySiteUser_Email(siteUser.getEmail());

        if (application.isEmpty()) {
            return new VerifyStatusDto(ApplicationStatusResponse.NOT_SUBMITTED.name());
        }

        int updateCount = application.get().getUpdateCount();
        if (application.get().getVerifyStatus() == VerifyStatus.PENDING) {
            return new VerifyStatusDto(ApplicationStatusResponse.SUBMITTED_PENDING.name(), updateCount);
        }
        if (application.get().getVerifyStatus() == VerifyStatus.REJECTED) {
            return new VerifyStatusDto(ApplicationStatusResponse.SUBMITTED_REJECTED.name(), updateCount);
        }
        return new VerifyStatusDto(ApplicationStatusResponse.SUBMITTED_APPROVED.name(), updateCount);
    }
}
