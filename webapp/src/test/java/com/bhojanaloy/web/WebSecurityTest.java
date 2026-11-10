package com.bhojanaloy.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** No database needed: the auth guard, input checks before any DAO call, and the static pages. */
@SpringBootTest
@AutoConfigureMockMvc
class WebSecurityTest {

    @Autowired
    MockMvc mvc;

    @Test
    void everyApiExceptLoginAndHealthNeedsASession() throws Exception {
        for (String path : new String[]{"/api/menu", "/api/orders", "/api/orders/1", "/api/auth/me"}) {
            mvc.perform(get(path)).andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Not logged in"));
        }
        mvc.perform(post("/api/orders/checkout").contentType(MediaType.APPLICATION_JSON).content("{\"items\":[]}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void healthIsPublicAndReportsTheApp() throws Exception {
        mvc.perform(get("/api/health")).andExpect(status().isOk()).andExpect(jsonPath("$.app").value("UP"));
    }

    @Test
    void loginWithoutAPasswordIsABadRequest() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"username\":\"admin\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username and password are required"));
    }

    @Test
    void thePagesAreServedWithTheStableIdsTheUiTestsUse() throws Exception {
        mvc.perform(get("/login.html")).andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"username\"")))
                .andExpect(content().string(containsString("id=\"login-btn\"")));
        mvc.perform(get("/pos.html")).andExpect(status().isOk())
                .andExpect(content().string(containsString("checkout-btn")));
        mvc.perform(get("/orders.html")).andExpect(status().isOk());
    }
}
