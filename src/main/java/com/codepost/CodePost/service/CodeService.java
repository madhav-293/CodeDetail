package com.codepost.CodePost.service;

import com.codepost.CodePost.config.AppConstant;
import com.codepost.CodePost.dto.CodeEntityDTO;
import com.codepost.CodePost.entity.CodeEntity;
import com.codepost.CodePost.repository.CodeRepository;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CodeService {


    @Autowired
    CodeRepository codeRepository;

    @Autowired
    KafkaTemplate<String, CodeEntityDTO> kafkaTemplateDetail;

    @Autowired
    private MongoTemplate mongoTemplate;

    public CodeEntity addCode(CodeEntity codeEntity) {
        CodeEntity savedCode = null;
        try {
            savedCode = codeRepository.save(codeEntity);

            CodeEntityDTO codeEntityDTO = new CodeEntityDTO(savedCode.getCName(), savedCode.getVersion());

            Message<CodeEntityDTO> message = MessageBuilder
                    .withPayload(codeEntityDTO)
                    .setHeader(KafkaHeaders.TOPIC, AppConstant.DETAIL_TOPIC_NAME)
                    .build();

            kafkaTemplateDetail.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to add code and send message", e);
        }
        return savedCode;
    }


    public List<CodeEntity> getActive(String value) {
        if (value.isEmpty()) return codeRepository.findAll();
        return codeRepository.findByIsActive(Boolean.parseBoolean(value));

    }

    public CodeEntity getLatestVersion(String cname) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("cname").is(cname)),
                Aggregation.sort(Sort.by(Sort.Order.desc("version"))),
                Aggregation.limit(1)
        );

        AggregationResults<CodeEntity> results = mongoTemplate.aggregate(aggregation, "codedetail", CodeEntity.class);
        return results.getUniqueMappedResult();
    }


    public List<CodeEntity> getAllLatestCode(String status) {
            Aggregation aggregation;

            if (status.equals("")) {
                // Case 1: When status is an empty string
                aggregation = Aggregation.newAggregation(
                        // Stage 1: Sort documents by version in descending order
                        Aggregation.sort(Sort.by(Sort.Direction.DESC, "version")),

                        // Stage 2: Group by 'code' and get the latest version for each code
                        Aggregation.group("cName")
                                .first("cName").as("cName")
                                .first("description").as("description")
                                .first("startDate").as("startDate")
                                .first("version").as("version")
                                .first("endDate").as("endDate")
                                .first("isActive").as("isActive")
                );
            } else {
                // Case 2: When status is not an empty string
                aggregation = Aggregation.newAggregation(
                        // Stage 1: Match documents by 'isActive' status
                        Aggregation.sort(Sort.by(Sort.Direction.DESC, "version")),
                        Aggregation.match(Criteria.where("isActive").is(Boolean.parseBoolean(status))),

                        // Stage 2: Sort documents by version in descending order


                        // Stage 3: Group by 'code' and get the latest version for each code
                        Aggregation.group("cName")
                                .first("cName").as("cName")
                                .first("description").as("description")
                                .first("startDate").as("startDate")
                                .first("version").as("version")
                                .first("endDate").as("endDate")
                                .first("isActive").as("isActive")

                );
            }

            AggregationResults<CodeEntity> results = mongoTemplate.aggregate(aggregation, "codedetail", CodeEntity.class);
            return results.getMappedResults();
        }


    }
//
//        if(!value.isBlank()){
//            Aggregation aggregation = Aggregation.newAggregation(
//
//                    Aggregation.match(Criteria.where("isActive").is(Boolean.parseBoolean(value))),
//                    // Sort by cName and version descending
//                    Aggregation.sort(Sort.by(Sort.Direction.DESC,"version")),
//                    // Group by cName and get the first document in each group
//                    Aggregation.group("cName")
//                            .first("cName").as("cName")
//                            .first("version").as("version")
//                            .first("startDate").as("startDate")
//                            .first("endDate").as("endDate")
//                            .first("description").as("description")
//                            .first("isActive").as("isActive"),
//
//                    Aggregation.project("cName","version","startDate","endDate","description","isActive")
//            );
//
//            // Execute the Aggregation
//            AggregationResults<CodeEntity> results = mongoTemplate.aggregate(
//                    aggregation,
//                    "codedetail",
//                    CodeEntity.class
//            );
//
//            // Return the Results
//            return results.getMappedResults();
//        }
//
//        Aggregation aggregation = Aggregation.newAggregation(
//                // Sort by cName and version descending
//                Aggregation.sort(Sort.by(Sort.Order.desc("cName"), Sort.Order.desc("version"))),
//
//                // Group by cName and get the first document in each group
//                Aggregation.group("cName")
//                        .first("cName").as("cName")
//                        .first("version").as("version")
//                        .first("startDate").as("startDate")
//                        .first("endDate").as("endDate")
//                        .first("description").as("description")
//                        .first("isActive").as("isActive")
//        );
//
//        // Execute the Aggregation
//        AggregationResults<CodeEntity> results = mongoTemplate.aggregate(
//                aggregation,
//                "codedetail",
//                CodeEntity.class
//        );
//
//        // Return the Results
//        return results.getMappedResults();
//    }


