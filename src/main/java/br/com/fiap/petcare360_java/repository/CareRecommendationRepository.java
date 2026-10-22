package br.com.fiap.petcare360_java.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.fiap.petcare360_java.model.CareRecommendation;

public interface CareRecommendationRepository extends JpaRepository<CareRecommendation, Long> {
	@Query("""
			select count(recommendation) from CareRecommendation recommendation
			join recommendation.tutor tutor join recommendation.veterinarian veterinarian
			where lower(tutor.email) = lower(:email) or lower(veterinarian.email) = lower(:email)
			""")
	long countVisibleForUser(@Param("email") String email);

	@Query(value = """
			select recommendation from CareRecommendation recommendation
			join fetch recommendation.pet join fetch recommendation.tutor tutor
			join fetch recommendation.veterinarian veterinarian
			where lower(tutor.email) = lower(:email) or lower(veterinarian.email) = lower(:email)
			order by recommendation.createdAt desc, recommendation.id desc
			""", countQuery = """
			select count(recommendation) from CareRecommendation recommendation
			join recommendation.tutor tutor join recommendation.veterinarian veterinarian
			where lower(tutor.email) = lower(:email) or lower(veterinarian.email) = lower(:email)
			""")
	Page<CareRecommendation> pageVisibleForUser(@Param("email") String email, Pageable pageable);

	@Query(value = """
			select recommendation from CareRecommendation recommendation
			join fetch recommendation.pet join fetch recommendation.tutor join fetch recommendation.veterinarian
			order by recommendation.createdAt desc, recommendation.id desc
			""", countQuery = "select count(recommendation) from CareRecommendation recommendation")
	Page<CareRecommendation> pageAllVisible(Pageable pageable);
	List<CareRecommendation> findByTutorEmailOrVeterinarianEmailOrderByCreatedAtDesc(String tutorEmail, String veterinarianEmail);

	@Query("""
			select recommendation from CareRecommendation recommendation
			join fetch recommendation.pet pet
			join fetch recommendation.tutor tutor
			join fetch recommendation.veterinarian veterinarian
			where lower(tutor.email) = lower(:email)
				or lower(veterinarian.email) = lower(:email)
			order by recommendation.createdAt desc
			""")
	List<CareRecommendation> findVisibleForUser(@Param("email") String email);

	@Query("""
			select recommendation from CareRecommendation recommendation
			join fetch recommendation.pet pet
			join fetch recommendation.tutor tutor
			join fetch recommendation.veterinarian veterinarian
			order by recommendation.createdAt desc
			""")
	List<CareRecommendation> findAllVisible();
}
