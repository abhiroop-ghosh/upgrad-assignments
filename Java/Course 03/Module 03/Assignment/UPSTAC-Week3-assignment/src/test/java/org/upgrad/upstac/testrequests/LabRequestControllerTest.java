package org.upgrad.upstac.testrequests;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.web.server.ResponseStatusException;
import org.upgrad.upstac.exception.AppException;
import org.upgrad.upstac.exception.UpgradResponseStatusException;
import org.upgrad.upstac.testrequests.lab.CreateLabResult;
import org.upgrad.upstac.testrequests.lab.LabRequestController;
import org.upgrad.upstac.testrequests.lab.TestStatus;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class LabRequestControllerTest {

    @Autowired
    @InjectMocks
    LabRequestController labRequestController;

    @Autowired
    @Mock
    TestRequestQueryService testRequestQueryService;

    @Test
    @WithUserDetails(value = "tester")
    public void calling_assignForLabTest_with_valid_test_request_id_should_update_the_request_status(){

        //Arrange
        TestRequest testRequest = getTestRequestByStatus(RequestStatus.INITIATED);

        //Act
        TestRequest testResponse = labRequestController.assignForLabTest(testRequest.getRequestId());

        //Assert
        assertNotNull(testResponse.getLabResult());
        assertEquals(testRequest.getRequestId(),testResponse.getRequestId());
        assertEquals(testResponse.getStatus(), RequestStatus.LAB_TEST_IN_PROGRESS);

    }

    public TestRequest getTestRequestByStatus(RequestStatus status) {
        return testRequestQueryService.findBy(status).stream().findFirst().get();
    }

    @Test
    @WithUserDetails(value = "tester")
    public void calling_assignForLabTest_with_valid_test_request_id_should_throw_exception(){

        //Assign
        Long InvalidRequestId= -34L;

        //Act
        AppException exception =  assertThrows(AppException.class,()->{
           labRequestController.assignForLabTest(InvalidRequestId);
        });

        //Assert
        assertThat(exception.getMessage(),containsString("Invalid ID"));

    }

    @Test
    @WithUserDetails(value = "tester")
    public void calling_updateLabTest_with_valid_test_request_id_should_update_the_request_status_and_update_test_request_details(){

        //Assign

        TestRequest testRequest = getTestRequestByStatus(RequestStatus.LAB_TEST_IN_PROGRESS);
        CreateLabResult labResult = getCreateLabResult(testRequest);

        //Act

        TestRequest testResponse = labRequestController.updateLabTest(testRequest.getRequestId(), labResult);

        //Assert

        assertEquals(testRequest.getRequestId(),testResponse.getRequestId());
        assertEquals(testResponse.getStatus(), RequestStatus.LAB_TEST_COMPLETED);
        assertEquals(testResponse.getLabResult().getResult(),labResult.getResult());

    }


    @Test
    @WithUserDetails(value = "tester")
    public void calling_updateLabTest_with_invalid_test_request_id_should_throw_exception(){

        //Assign
        TestRequest testRequest = getTestRequestByStatus(RequestStatus.LAB_TEST_IN_PROGRESS);
        CreateLabResult labResult = getCreateLabResult(testRequest);
        Long invalidRequestId = -21L;

        //Act
        UpgradResponseStatusException exception = assertThrows(UpgradResponseStatusException.class, ()->{
            labRequestController.updateLabTest(invalidRequestId,labResult);
        });

        assertThat(exception.getMessage(),containsString("Invalid ID"));

    }

    @Test
    @WithUserDetails(value = "tester")
    public void calling_updateLabTest_with_invalid_empty_status_should_throw_exception(){

        //Assign
        TestRequest testRequest = getTestRequestByStatus(RequestStatus.LAB_TEST_IN_PROGRESS);
        CreateLabResult labResult = getCreateLabResult(testRequest);
        labResult.setResult(null);

        //Act
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, ()->{
            labRequestController.updateLabTest(testRequest.getRequestId(),labResult);
        });

        //Assert
        assertThat(exception.getMessage(),containsString("ConstraintViolationException"));

    }

    public CreateLabResult getCreateLabResult(TestRequest testRequest) {

       CreateLabResult labResult = new CreateLabResult();
       labResult.setComments("Taken all relevant data");
       labResult.setBloodPressure("170");
       labResult.setHeartBeat("90");
       labResult.setTemperature("100");
       labResult.setOxygenLevel("99");
       labResult.setResult(TestStatus.NEGATIVE);
       return labResult;

    }

}