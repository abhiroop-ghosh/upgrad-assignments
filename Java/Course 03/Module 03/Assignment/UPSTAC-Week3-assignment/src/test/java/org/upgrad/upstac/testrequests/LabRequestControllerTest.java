package org.upgrad.upstac.testrequests;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
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
    LabRequestController labRequestController;

    @Autowired
    TestRequestQueryService testRequestQueryService;

    @Test
    @WithUserDetails(value = "tester")
    public void calling_assignForLabTest_with_valid_test_request_id_should_update_the_request_status(){

        //Arrange
        //Obtain test requests which are in INITIATED status
        TestRequest testRequest = getTestRequestByStatus(RequestStatus.INITIATED);

        //Act
        //From the obtained test request assign the test request for lab test
        TestRequest testResponse = labRequestController.assignForLabTest(testRequest.getRequestId());

        //Assert

        // The response should not be NULL
        assertNotNull(testResponse.getLabResult());

        //The request and response should have the same request id
        assertEquals(testRequest.getRequestId(),testResponse.getRequestId());

        //The request status post assignment should be LAB_TEST_IN_PROGRESS
        assertEquals(testResponse.getStatus(), RequestStatus.LAB_TEST_IN_PROGRESS);

    }

    public TestRequest getTestRequestByStatus(RequestStatus status) {
        return testRequestQueryService.findBy(status).stream().findFirst().get();
    }

    @Test
    @WithUserDetails(value = "tester")
    public void calling_assignForLabTest_with_valid_test_request_id_should_throw_exception(){

        //Assign

        //Create an invalid request id
        Long InvalidRequestId= -34L;

        //Act
        AppException exception =  assertThrows(AppException.class,()->{
           labRequestController.assignForLabTest(InvalidRequestId);
        });

        //Assert

        //The exception message should contain a message "Invalid ID"
        assertThat(exception.getMessage(),containsString("Invalid ID"));

    }

    @Test
    @WithUserDetails(value = "tester")
    public void calling_updateLabTest_with_valid_test_request_id_should_update_the_request_status_and_update_test_request_details(){

        //Assign

        //Obtain test requests which are in LAB_TEST_IN_PROGRESS status
        TestRequest testRequest = getTestRequestByStatus(RequestStatus.LAB_TEST_IN_PROGRESS);

        //Create a lab request based on the test request
        CreateLabResult labResult = getCreateLabResult(testRequest);

        //Act

        //Update the lab result details
        TestRequest testResponse = labRequestController.updateLabTest(testRequest.getRequestId(), labResult);

        //Assert

        //The request id for the test request should match that of the test response
        assertEquals(testRequest.getRequestId(),testResponse.getRequestId());

        //The request status of the test should be updated to LAB_TEST_COMPLETED
        assertEquals(testResponse.getStatus(), RequestStatus.LAB_TEST_COMPLETED);

        //The test response should have the same lab result as created
        assertEquals(testResponse.getLabResult().getResult(),labResult.getResult());

    }


    @Test
    @WithUserDetails(value = "tester")
    public void calling_updateLabTest_with_invalid_test_request_id_should_throw_exception(){

        //Assign

        //Obtain test requests which are in LAB_TEST_IN_PROGRESS status
        TestRequest testRequest = getTestRequestByStatus(RequestStatus.LAB_TEST_IN_PROGRESS);

        //Create a lab request based on the test request
        CreateLabResult labResult = getCreateLabResult(testRequest);

        //Create an invalid test request id
        Long invalidRequestId = -21L;

        //Act
        UpgradResponseStatusException exception = assertThrows(UpgradResponseStatusException.class, ()->{
            labRequestController.updateLabTest(invalidRequestId,labResult);
        });

        //The exception message should contain message "Invalid ID"
        assertThat(exception.getMessage(),containsString("Invalid ID"));

    }

    @Test
    @WithUserDetails(value = "tester")
    public void calling_updateLabTest_with_invalid_empty_status_should_throw_exception(){

        //Assign

        //Obtain test requests which are in LAB_TEST_IN_PROGRESS status
        TestRequest testRequest = getTestRequestByStatus(RequestStatus.LAB_TEST_IN_PROGRESS);

        //Create a lab request based on the test request
        CreateLabResult labResult = getCreateLabResult(testRequest);

        //Update the result to NULL
        labResult.setResult(null);

        //Act
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, ()->{
            labRequestController.updateLabTest(testRequest.getRequestId(),labResult);
        });

        //Assert

        //The exception message should contain message "ConstraintViolationException"
        assertThat(exception.getMessage(),containsString("ConstraintViolationException"));

    }

    public CreateLabResult getCreateLabResult(TestRequest testRequest) {

       //Creating a lab request to capture vitals of the user
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