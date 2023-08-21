//Obs: tirado da task OD-353 do Edilson
package com.nexti.operationdesk.controller;
import helper.IntegrationHelper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
class LoadControllerIT extends IntegrationHelper {
    private final static String PATH_URI = "/load/";
    @Test
    void WHEN_Get_Workplaces_MUST_Return_List_Workplaces_Whitch_Sucess() throws Exception {
        var token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJ1c2VyX2FjY291bnRfaWQiOjE1NzM0LCJ1c2VyX25hbWUiOiJzaXN0ZW1hLm5leHRpIiwic2NvcGUiOlsib3BlbmlkIl0sImxhbmd1YWdlIjoicHQiLCJhdXRob3JpdGllcyI6WyJ0ZW5hbnR-MSJdLCJqdGkiOiI5YjkzYzI5Yi0xZjM3LTRlNzMtYWQwMS1hMTQxZjA2ZGM5MzMiLCJjbGllbnRfaWQiOiJmcm9udGVuZCJ9.-UnnHUY6kXqQC38Ep2b0NY0YvaScltEUKJS4NZw4HTA";
        mockMvc
                .perform(get(PATH_URI+"workplaces")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(exists("content"));
    }
    @Test
    void WHEN_do_Get_with_Invalid_token_it_SHOULD_Return_Empty_List() throws Exception {
        var token = "";
        mockMvc
                .perform(get(PATH_URI+"workplaces")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(exists("[]"));
    }
}





package helper;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
public class IntegrationHelper extends TestHelper{
    @Autowired
    protected MockMvc mockMvc;
}
