package com.codepost.CodePost;

import com.codepost.CodePost.config.AppConstant;
import com.codepost.CodePost.dto.CodeEntityDTO;
import com.codepost.CodePost.entity.CodeEntity;
import com.codepost.CodePost.repository.CodeRepository;
import com.codepost.CodePost.service.CodeService;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.Code;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CodeServiceTest {

    @Mock
    private CodeRepository codeRepository;

    @Mock
    private KafkaTemplate<String, CodeEntityDTO> kafkaTemplateDetail;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private CodeService codeService;

    private CodeEntity codeEntity;

    @BeforeEach
    void setUp() {
        codeEntity = new CodeEntity();
        codeEntity.setCName("TestName");
        codeEntity.setVersion(1.0);
        codeEntity.setActive(true);
        codeEntity.setDescription("Test description");
        codeEntity.setStartDate("29/02/23");
        codeEntity.setEndDate("29/03/23");
    }


    @Test
    void testGetActive() {
        when(codeRepository.findByIsActive(true)).thenReturn(List.of(codeEntity));

        List<CodeEntity> result = codeService.getActive("true");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(codeEntity.getCName(), result.get(0).getCName());
    }

    @Test
    void testGetLatestVersion() throws InstantiationException, IllegalAccessException {
        // Create a mock aggregation result
        Document document = new Document("cName", codeEntity.getCName())
                .append("version", codeEntity.getVersion())
                .append("description", codeEntity.getDescription())
                .append("startDate", codeEntity.getStartDate())
                .append("endDate", codeEntity.getEndDate())
                .append("isActive", codeEntity.isActive());

        // Create AggregationResults with Document objects
        AggregationResults<Document> aggregationResults = new AggregationResults<>(List.of(document), Document.class.newInstance());

        // Mock the MongoTemplate to return the AggregationResults
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("codedetail"), eq(Document.class)))
                .thenReturn(aggregationResults);

        // Call the service method
        CodeEntity result = codeService.getLatestVersion(codeEntity.getCName());

        // Assert the result
        assertNotNull(result);
        assertEquals(codeEntity.getCName(), result.getCName());
        assertEquals(codeEntity.getVersion(), result.getVersion());
    }

    @Test
    void testGetAllLatestCodeWithoutStatus() throws InstantiationException, IllegalAccessException {
        // Create a mock Document representing the CodeEntity
        Document document = new Document("cName", codeEntity.getCName())
                .append("version", codeEntity.getVersion())
                .append("description", codeEntity.getDescription())
                .append("startDate", codeEntity.getStartDate())
                .append("endDate", codeEntity.getEndDate())
                .append("isActive", codeEntity.isActive());

        // Wrap the document in AggregationResults
        AggregationResults<Document> aggregationResults = new AggregationResults<>(List.of(document), Document.class.newInstance());

        // Mock MongoTemplate to return AggregationResults<Document>
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("codedetail"), eq(Document.class)))
                .thenReturn(aggregationResults);

        // Call the service method
        List<CodeEntity> result = codeService.getAllLatestCode("");

        // Assert the result
        assertNotNull(result);
        assertEquals(1, result.size());
        CodeEntity returnedCodeEntity = result.get(0);
        assertEquals(codeEntity.getCName(), returnedCodeEntity.getCName());
        assertEquals(codeEntity.getVersion(), returnedCodeEntity.getVersion());
        assertEquals(codeEntity.getDescription(), returnedCodeEntity.getDescription());
        assertEquals(codeEntity.getStartDate(), returnedCodeEntity.getStartDate());
        assertEquals(codeEntity.getEndDate(), returnedCodeEntity.getEndDate());
        assertEquals(codeEntity.isActive(), returnedCodeEntity.isActive());
    }

//
//    @Test
//    void testGetAllLatestCodeWithStatus() {
//        Aggregation aggregation = Aggregation.newAggregation(
//                Aggregation.sort(Sort.by(Sort.Direction.DESC, "version")),
//                Aggregation.group("cName")
//                        .first("cName").as("cName")
//                        .first("description").as("description")
//                        .first("startDate").as("startDate")
//                        .first("version").as("version")
//                        .first("endDate").as("endDate")
//                        .first("isActive").as("isActive"),
//                Aggregation.match(Criteria.where("isActive").is(true))
//        );
//
//        AggregationResults<CodeEntity> results = new AggregationResults<>(List.of(codeEntity), CodeEntity.class);
//        when(mongoTemplate.aggregate(aggregation, "codedetail", CodeEntity.class)).thenReturn(results);
//
//        List<CodeEntity> result = codeService.getAllLatestCode("true");
//
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        assertEquals(codeEntity.getCName(), result.get(0).getCName());
//    }
}
