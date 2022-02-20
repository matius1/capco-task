package com.skocz.capco.controller;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
class FeatureFlagsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Order(1)
    void givenInitialData_whenGetEnabledFeatures_thenReturn() throws Exception {
        mockMvc.perform(get("/features/user/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("[\"global-1\",\"user-1\",\"global-2\"]"));
    }

    @Test
    @Order(2)
    void givenNewFeatureFlagName_whenCreate_thenReturnDisabledFlag() throws Exception {
        mockMvc.perform(post("/features/create")
                        .content("{\"name\": \"new-1\"}")
                        .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("\"name\":\"new-1\",\"enabled\":false")));
    }

    @Test
    @Order(3)
    void givenName_whenEnable_thenReturn() throws Exception {
        mockMvc.perform(put("/features/user/1/feature/new-1/on"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("\"name\":\"new-1\",\"enabled\":true")));
    }

    @Test
    @Order(4)
    void whenGetEnabledFeatures_thenReturnWithNewFeature() throws Exception {
        mockMvc.perform(get("/features/user/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("[\"global-1\",\"user-1\",\"new-1\",\"global-2\"]"));
    }

    @Test
    @Order(5)
    void givenNotExistingName_whenEnable_thenReturnBadRequest() throws Exception {
        mockMvc.perform(put("/features/user/1/feature/notexisting-1/on"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Feature flag with name: notexisting-1 do not exists"));
    }

    @Test
    @Order(6)
    void givenNotExistingUser_whenGetEnabledFeatures_thenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/features/user/9"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User with id: 9 not found"));
    }

}