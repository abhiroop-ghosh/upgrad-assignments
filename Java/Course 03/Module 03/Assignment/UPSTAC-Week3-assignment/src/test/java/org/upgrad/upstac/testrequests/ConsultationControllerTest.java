package org.upgrad.upstac.testrequests;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
    @InjectMocks
    ConsultationController consultationController;

    @Autowired
    @Mock
    UserLoggedInService userLoggedInService;

    @Autowired
    @Mock
    TestRequestQueryService testRequestQueryService;

    @Autowired
    @Mock
    TestRequestUpdateService testRequestUpdateService;

    @Test
    @WithUserDetails(value = "doctor")
    public void calling_assignForConsultation_with_valid_test_request_id_should_update_the_request_status(){

        //Arrange
        TestRequest testRequest = getTestRequestByStatus(RequestStatus.LAB_TEST_COMPLETED);

       //Act
        TestRequest testResponse = consultationController.assignForConsultation(testRequest.getRequestId());

        //Assert
        assertNotNull(testResponse);
        assertEquals(testRequest.getRequestId(),testResponse.getRequestId());
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
        UpgradResponseStatusException exception = assertThrows(UpgradResponseStatusException.class,()->{
            consultationController.assignForConsultation(InvalidRequestId);
        });

        //Assert
        assertThat(exception.getMessage(), containsString("Invalid ID"));

    }

    @Test
    @WithUserDetails(value = "doctor")
    public void calling_updateConsultation_with_valid_test_request_id_should_update_the_request_status_and_update_consultation_details(){

        //Arrange
        TestRequest testRequest = getTestRequestByStatus(RequestStatus.DIAGNOSIS_IN_PROCESS);

        //Act
        CreateConsultationRequest consultationRequest = getCreateConsultationRequest(testRequest);
        TestRequest testResponse = consultationController.updateConsultation(testRequest.getRequestId(),consultationRequest);

        //Assert
        assertEquals(testResponse.getRequestId(),testRequest.getRequestId());
        assertEquals(testResponse.getStatus(), RequestStatus.COMPLETED);
        assertEquals(consultationRequest.getSuggestion(),testResponse.getConsultation().getSuggestion());

    }

    @Test
    @WithUserDetails(value = "doctor")
    public void calling_updateConsultation_with_invalid_test_request_id_should_throw_exception(){

        //Arrange
        TestRequest testRequest = getTestRequestByStatus(RequestStatus.DIAGNOSIS_IN_PROCESS);
        CreateConsultationRequest consultationRequest = getCreateConsultationRequest(testRequest);
        Long invalidRequestId = -21L;

        //Act
        UpgradResponseStatusException exception = assertThrows(UpgradResponseStatusException.class, ()->{
            consultationController.updateConsultation(invalidRequestId,consultationRequest);
        });

        //Assert
        assertThat(exception.getMessage(), containsString("Invalid ID"));
   }

    @Test
    @WithUserDetails(value = "doctor")
    public void calling_updateConsultation_with_invalid_empty_status_should_throw_exception(){

        //Arrange
        TestRequest testRequest = getTestRequestByStatus(RequestStatus.DIAGNOSIS_IN_PROCESS);

        //Act
        CreateConsultationRequest consultationRequest = getCreateConsultationRequest(testRequest);
        consultationRequest.setSuggestion(null);

        //Act & Assert
        UpgradResponseStatusException exception = assertThrows(UpgradResponseStatusException.class, ()->{
            consultationController.updateConsultation(testRequest.getRequestId(),consultationRequest);
        });
    }

    public CreateConsultationRequest getCreateConsultationRequest(TestRequest testRequest) {

        CreateConsultationRequest consultationRequest = new CreateConsultationRequest();
        if (testRequest.getLabResult().getResult() == TestStatus.POSITIVE) {
            consultationRequest.setComments("Take Rest");
            consultationRequest.setSuggestion(DoctorSuggestion.HOME_QUARANTINE);
        }
        else if (testRequest.getLabResult().getResult() == TestStatus.NEGATIVE) {
            consultationRequest.setComments("Ok");
            consultationRequest.setSuggestion(DoctorSuggestion.NO_ISSUES);
        }
        return consultationRequest;

    }

}