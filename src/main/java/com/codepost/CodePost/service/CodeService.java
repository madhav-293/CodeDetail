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
                Aggregation.match(Criteria.where("cName").is(cname)),
                Aggregation.sort(Sort.by(Sort.Direction.DESC,("version"))),
                Aggregation.limit(1)
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "codedetail", Document.class);
        Document document = results.getUniqueMappedResult();

        if (document == null) {
            return null;
        }

        CodeEntity codeEntity = new CodeEntity();
        codeEntity.setCName(document.getString("cName"));
        codeEntity.setVersion(document.getDouble("version"));
        codeEntity.setDescription(document.getString("description"));
        codeEntity.setStartDate(document.getString("startDate"));
        codeEntity.setEndDate(document.getString("endDate"));
        codeEntity.setActive(document.getBoolean("isActive"));

        return codeEntity;
    }


    public List<CodeEntity> getAllLatestCode(String status) {
            Aggregation aggregation;
            if (status.isBlank()) {
                aggregation = Aggregation.newAggregation(
                        Aggregation.sort(Sort.by(Sort.Direction.DESC, "version")),
                        Aggregation.group("cName")
                                .first("cName").as("cName")
                                .first("description").as("description")
                                .first("startDate").as("startDate")
                                .first("version").as("version")
                                .first("endDate").as("endDate")
                                .first("isActive").as("isActive")
                );
                System.out.println("blank Status");
            } else {

                aggregation = Aggregation.newAggregation(

                        Aggregation.sort(Sort.by(Sort.Direction.DESC, "version")),
                        Aggregation.group("cName")
                                .first("cName").as("cName")
                                .first("description").as("description")
                                .first("startDate").as("startDate")
                                .first("version").as("version")
                                .first("endDate").as("endDate")
                                .first("isActive").as("isActive"),
                        Aggregation.match(Criteria.where("isActive").is(Boolean.parseBoolean(status)))
                );
            }

            AggregationResults<CodeEntity> results = mongoTemplate.aggregate(aggregation, "codedetail", CodeEntity.class);
            return results.getMappedResults();
        }


    }



