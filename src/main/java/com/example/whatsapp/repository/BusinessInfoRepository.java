package com.example.whatsapp.repository;

import com.example.whatsapp.model.BusinessInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface BusinessInfoRepository extends MongoRepository<BusinessInfo, String> {
    Optional<BusinessInfo> findByBusinessId(String businessId);
}
