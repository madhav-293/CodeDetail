package com.codepost.CodePost.repository;


import com.codepost.CodePost.entity.CodeEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodeRepository extends MongoRepository<CodeEntity,String> {
    List<CodeEntity> findByIsActive(boolean isActive);
}
