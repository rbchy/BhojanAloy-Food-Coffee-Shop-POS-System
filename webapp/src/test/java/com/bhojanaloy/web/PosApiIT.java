package com.bhojanaloy.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The POS REST API against a real MySQL (CI). Skipped when DB_URL is not set. */
@SpringBootTest
@AutoConfigureMockMvc
class PosApiIT {

    @Autowired
    MockMvc mvc;

    private final ObjectMapper json = new ObjectMapper();
    private MockHttpSession session;

    @BeforeAll
    static void needsADatabase() {
        String url = System.getenv("DB_URL");
        assumeTrue(url != null && !url.isEmpty(), "DB_URL not set - skipping API tests");
    }

    private ResultActions login(MockHttpSession s, String user, String pass) throws Exception {
        return mvc.perform(post("/api/auth/login").session(s).contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + user + "\",\"password\":\"" + pass + "\"}"));
    }

    @BeforeEach
    void logInAsCashier() throws Exception {
        session = new MockHttpSession();
        login(session, "sabbir", "admin123").andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("CASHIER"));
    }

    private JsonNode body(ResultActions r) throws Exception {
        return json.readTree(r.andReturn().getResponse().getContentAsString());
    }

    private JsonNode menuItem(String name) throws Exception {
        for (JsonNode n : body(mvc.perform(get("/api/menu").session(session)).andExpect(status().isOk()))) {
            if (n.get("itemName").asText().equals(name)) return n;
        }
        throw new AssertionError(name + " not on the menu");
    }

    private ResultActions checkout(String bodyJson) throws Exception {
        return mvc.perform(post("/api/orders/checkout").session(session)
                .contentType(MediaType.APPLICATION_JSON).content(bodyJson));
    }

    @Test
    void wrongPasswordIsRejectedAndLogoutEndsTheSession() throws Exception {
        login(new MockHttpSession(), "admin", "nope").andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/me").session(session)).andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("sabbir"));
        mvc.perform(post("/api/auth/logout").session(session)).andExpect(status().isOk());
        assertTrue(session.isInvalid());
    }

    @Test
    void checkoutRepricesFromTheDatabaseAndDeductsStock() throws Exception {
        JsonNode latte = menuItem("Latte");
        int id = latte.get("itemId").asInt();
        BigDecimal price = new BigDecimal(latte.get("price").asText());
        int stock = latte.get("stockQty").asInt();

        // The browser cannot set prices: the request only carries item ids and quantities.
        JsonNode order = body(checkout("{\"items\":[{\"itemId\":" + id + ",\"quantity\":3}],\"discount\":1.00,"
                + "\"paymentMethod\":\"CARD\",\"orderType\":\"PICKUP\",\"customerName\":\"API Test\"}")
                .andExpect(status().isOk()));

        BigDecimal subtotal = price.multiply(BigDecimal.valueOf(3)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal tax = subtotal.subtract(BigDecimal.ONE).multiply(new BigDecimal("0.08")).setScale(2, RoundingMode.HALF_UP);
        assertEquals(0, subtotal.compareTo(new BigDecimal(order.get("subtotal").asText())));
        assertEquals(0, tax.compareTo(new BigDecimal(order.get("taxAmount").asText())));
        assertEquals(0, subtotal.subtract(BigDecimal.ONE).add(tax).compareTo(new BigDecimal(order.get("totalAmount").asText())));
        assertEquals("PICKUP", order.get("orderType").asText());
        assertEquals(stock - 3, menuItem("Latte").get("stockQty").asInt());

        int orderId = order.get("orderId").asInt();
        mvc.perform(get("/api/orders/" + orderId).session(session)).andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(3));
        assertTrue(body(mvc.perform(get("/api/orders").session(session))).toString().contains(order.get("orderNumber").asText()));
    }

    @Test
    void badCartsAreRefusedWithAClearMessage() throws Exception {
        int id = menuItem("Espresso").get("itemId").asInt();
        checkout("{\"items\":[]}").andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Cart is empty"));
        checkout("{\"items\":[{\"itemId\":" + id + ",\"quantity\":0}]}").andExpect(status().isBadRequest());
        checkout("{\"items\":[{\"itemId\":" + id + ",\"quantity\":-2}]}").andExpect(status().isBadRequest());
        checkout("{\"items\":[{\"itemId\":999999,\"quantity\":1}]}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Menu item 999999 is not available"));
        checkout("{\"items\":[{\"itemId\":" + id + ",\"quantity\":1}],\"discount\":-5}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Discount cannot be negative"));
        checkout("{\"items\":[{\"itemId\":" + id + ",\"quantity\":1}],\"paymentMethod\":\"BITCOIN\"}")
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Unknown payment method: BITCOIN"));
        checkout("{\"items\":[{\"itemId\":" + id + ",\"quantity\":1}],\"orderType\":\"DRONE\"}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void ahugeDiscountGivesAFreeOrderNotANegativeOne() throws Exception {
        int id = menuItem("Americano").get("itemId").asInt();
        JsonNode order = body(checkout("{\"items\":[{\"itemId\":" + id + ",\"quantity\":1}],\"discount\":500}")
                .andExpect(status().isOk()));
        assertEquals(0, BigDecimal.ZERO.compareTo(new BigDecimal(order.get("totalAmount").asText())));
    }

    @Test
    void orderingMoreThanIsInStockIsAConflictAndChangesNothing() throws Exception {
        JsonNode mocha = menuItem("Mocha");
        int stock = mocha.get("stockQty").asInt();
        checkout("{\"items\":[{\"itemId\":" + mocha.get("itemId").asInt() + ",\"quantity\":" + (stock + 1) + "}]}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Insufficient stock for item: Mocha"));
        assertEquals(stock, menuItem("Mocha").get("stockQty").asInt());
    }

    @Test
    void aMissingOrderIs404() throws Exception {
        mvc.perform(get("/api/orders/99999999").session(session)).andExpect(status().isNotFound());
    }

    @Test
    void healthSeesTheDatabase() throws Exception {
        mvc.perform(get("/api/health")).andExpect(jsonPath("$.db").value("UP"));
    }
}
