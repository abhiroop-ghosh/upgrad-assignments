package org.upgrad.upstac.testrequests;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.upgrad.upstac.config.security.UserLoggedInService;
import org.upgrad.upstac.exception.UpgradResponseStatusException;
import org.upgrad.upstac.testrequests.consultation.ConsultationController;
import org.upgrad.upstac.testrequests.consultation.CreateConsultationRequest;
import org.upgrad.upstac.testrequests.consultation.DoctorSuggestion;
import org.upgrad.upstac.testrequests.lab.TestStatus;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class ConsultationControllerTest {


    @Autowired
    ConsultationController consultationController;

    @Autowired
    UserLoggedInService userLoggedInService;

    @Autowired
    TestRequestQueryService testRequestQueryService;

    @Autowired
    TestRequestUpdateService testRequestUpdateService;

    @Test
    @WithUserDetails(value = "doctor")
    public void calling_assignForConsultation_with_valid_test_request_id_should_update_the_request_status(){

        //Arrange

        //Obtain test requests which are in LAB_TEST_COMPLETED status
        TestRequest testRequest = getTestRequestByStatus(RequestStatus.LAB_TEST_COMPLETED);

       //Act

       //Pass the test with obtained id for consultation
       TestRequest testResponse = consultationController.assignForConsultation(testRequest.getRequestId());

        //Assert

        //The system has a response
        assertNotNull(testResponse);
        //The request id of the request is same as that of the response.
        assertEquals(testRequest.getRequestId(),testResponse.getRequestId());
        //The request status of the response is DIAGNOSIS_IN_PROCESS
        assertEquals(testResponse.getStatus(), RequestStatus.DIAGNOSIS_IN_PROCESS);

    }

    public TestRequest getTestRequestByStatus(RequestStatus status) {
        return testRequestQueryService.findBy(status).stream().findFirst().get();
    }

    @Test
    @WithUserDetails(value = "doctor")
    public void calling_assignForConsultation_with_valid_test_request_id_should_throw_exception(){

        //Arrange
        Long InvalidRequestId= -34L;

        //Act

        //Pass the invalid id for consultation
        UpgradResponseStatusException exception = assertThrows(UpgradResponseStatusException.class,()->{
            consultationController.assignForConsultation(InvalidRequestId);
        });

        //Assert

        //The exception message should contain a message "Invalid ID"
        assertThat(exception.getMessage(), containsString("Invalid ID"));

    }

    @Test
    @WithUserDetails(value = "doctor")
    public void calling_updateConsultation_with_valid_test_request_id_should_update_the_request_status_and_update_consultation_details(){

        //Arrange

        //Obtain test requests which are in DIAGNOSIS_IN_PROCESS status
        TestRequest testRequest = getTestRequestByStatus(RequestStatus.DIAGNOSIS_IN_PROCESS);

        //Act

        //Create a consultation request based on the test request
        CreateConsultationRequest consultationRequest = getCreateConsultationRequest(testRequest);
        //Update the consultation remarks created to the test request
        TestRequest testResponse = consultationController.updateConsultation(testRequest.getRequestId(),consultationRequest);

        //Assert

        //The test request and the test response have same id
        assertEquals(testResponse.getRequestId(),testRequest.getRequestId());
        //The test response should have the request status as COMPLETED
        assertEquals(testResponse.getStatus(), RequestStatus.COMPLETED);
        //The consultation remarks on the test response is same as the formulated suggestion
        assertEquals(consultationRequest.getSuggestion(),testResponse.getConsultation().getSuggestion());

    }

    @Test
    @WithUserDetails(value = "doctor")
    public void calling_updateConsultation_with_invalid_test_request_id_should_throw_exception(){

        //Arrange

        //Obtain test requests which are in DIAGNOSIS_IN_PROCESS status
        TestRequest testRequest = getTestRequestByStatus(RequestStatus.DIAGNOSIS_IN_PROCESS);

        //Create a consultation request based on the test request
        CreateConsultationRequest consultationRequest = getCreateConsultationRequest(testRequest);

        //set up an invalid request id
        Long invalidRequestId = -21L;

        //Act
        UpgradResponseStatusException exception = assertThrows(UpgradResponseStatusException.class, ()->{
            consultationController.updateConsultation(invalidRequestId,consultationRequest);
        });

        //Assert

        //The exception message should contain a message "Invalid ID"
        assertThat(exception.getMessage(), containsString("Invalid ID"));
   }

    @Test
    @WithUserDetails(value = "doctor")
    public void calling_updateConsultation_with_invalid_empty_status_should_throw_exception(){

        //Arrange

        //Obtain test requests which are in DIAGNOSIS_IN_PROCESS status
        TestRequest testRequest = getTestRequestByStatus(RequestStatus.DIAGNOSIS_IN_PROCESS);

        //Act

        //Create a consultation request based on the test request
        CreateConsultationRequest consultationRequest = getCreateConsultationRequest(testRequest);

        //update the suggestion to null
        consultationRequest.setSuggestion(null);

        //Act & Assert

        //An exception is thrown by the system
        UpgradResponseStatusException exception = assertThrows(UpgradResponseStatusException.class, ()->{
            consultationController.updateConsultation(testRequest.getRequestId(),consultationRequest);
        });
    }

    public CreateConsultationRequest getCreateConsultationRequest(TestRequest testRequest) {

        //Create a new consultation request
        CreateConsultationRequest consultationRequest = new CreateConsultationRequest();

        //If the lab result is positive
        if (testRequest.getLabResult().getResult() == TestStatus.POSITIVE) {
            //Doctors comment = "Take Rest"
            consultationRequest.setComments("Take Rest");
            //Doctors suggestion is HOME_QUARANTINE
            consultationRequest.setSuggestion(DoctorSuggestion.HOME_QUARANTINE);
        }
        //If the lab result is negative
        else if (testRequest.getLabResult().getResult() == TestStatus.NEGATIVE) {
            //Doctors comment = "Ok"
            consultationRequest.setComments("Ok");
            //Doctors suggestion is NO_ISSUES
            consultationRequest.setSuggestion(DoctorSuggestion.NO_ISSUES);
        }
        return consultationRequest;

    }

}