package com.codepost.CodePost;

import com.codepost.CodePost.dto.CodeEntityDTO;
import com.codepost.CodePost.entity.CodeEntity;
import com.codepost.CodePost.repository.CodeRepository;
import com.codepost.CodePost.service.CodeService;
import org.bson.Document;
import org.bson.types.Code;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import java.util.Collections;
import java.util.List;
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
        MockitoAnnotations.openMocks(this);
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
    public void getAllLatestCodeTestWithoutStatus(){

        List<CodeEntity> codeEntities = Collections.singletonList(codeEntity);

        AggregationResults<CodeEntity> codeEntities1 = new AggregationResults<>(codeEntities,new Document());

        when(mongoTemplate.aggregate(any(Aggregation.class),eq("codedetail"),eq(CodeEntity.class))).thenReturn( codeEntities1);

        assertEquals(codeEntities,codeService.getAllLatestCode(""));


    }

    @Test
    public void getAllLatestCodeTestWithStatus(){

        List<CodeEntity> codeEntities = Collections.singletonList(codeEntity);

        AggregationResults<CodeEntity> codeEntities1 = new AggregationResults<>(codeEntities,new Document());

        when(mongoTemplate.aggregate(any(Aggregation.class),eq("codedetail"),eq(CodeEntity.class))).thenReturn( codeEntities1);

        assertEquals(codeEntities,codeService.getAllLatestCode("true"));


    }



}
