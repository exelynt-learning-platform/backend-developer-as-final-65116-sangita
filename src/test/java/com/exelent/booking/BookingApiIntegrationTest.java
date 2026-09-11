package com.exelent.booking;

import com.exelent.booking.dto.auth.LoginRequest;
import com.exelent.booking.dto.auth.LoginResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookingApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void loginSucceedsForSeedAdminAndUser() throws Exception {
        login("admin", "Admin@123");
        login("user", "User@123");
    }

    @Test
    void loginFailsWithInvalidPassword() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("admin", "wrong"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void protectedEndpointsRequireJwt() throws Exception {
        mockMvc.perform(get("/api/resources")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/reservations")).andExpect(status().isUnauthorized());
    }

    @Test
    void userHasReadOnlyAccessToResources() throws Exception {
        String userToken = login("user", "User@123");
        String adminToken = login("admin", "Admin@123");

        mockMvc.perform(get("/api/resources").header("Authorization", bearer(userToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(3))));

        mockMvc.perform(post("/api/resources")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Blocked Room",
                                  "type": "ROOM",
                                  "hourlyRate": 10.00,
                                  "available": true
                                }
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/resources")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Training Room",
                                  "description": "Admin created",
                                  "type": "ROOM",
                                  "location": "Floor 3",
                                  "hourlyRate": 40.00,
                                  "available": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Training Room"));
    }

    @Test
    void userSeesOnlyOwnReservationsWhileAdminSeesAll() throws Exception {
        String userToken = login("user", "User@123");
        String userTwoToken = login("user2", "User@123");
        String adminToken = login("admin", "Admin@123");

        mockMvc.perform(get("/api/reservations").header("Authorization", bearer(userToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.username != 'user')]").isEmpty());

        MvcResult userList = mockMvc.perform(get("/api/reservations").header("Authorization", bearer(userToken)))
                .andExpect(status().isOk())
                .andReturn();
        long userReservationId = objectMapper.readTree(userList.getResponse().getContentAsString())
                .get("content").get(0).get("id").asLong();

        mockMvc.perform(get("/api/reservations/" + userReservationId).header("Authorization", bearer(userTwoToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/reservations/" + userReservationId).header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userReservationId));

        mockMvc.perform(get("/api/reservations").header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(3)));
    }

    @Test
    void reservationOwnerIsTakenFromJwtNotRequestBody() throws Exception {
        String userToken = login("user", "User@123");
        LocalDateTime start = LocalDateTime.now().plusDays(12).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(1);

        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "resourceId": 3,
                                  "startTime": "%s",
                                  "endTime": "%s",
                                  "price": 15.50,
                                  "userId": 999
                                }
                                """.formatted(start, end)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("user"))
                .andExpect(jsonPath("$.price").value(15.50));
    }

    @Test
    void reservationsCanBeFilteredPaginatedAndSorted() throws Exception {
        String adminToken = login("admin", "Admin@123");

        mockMvc.perform(get("/api/reservations")
                        .header("Authorization", bearer(adminToken))
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.status != 'PENDING')]").isEmpty());

        mockMvc.perform(get("/api/reservations")
                        .header("Authorization", bearer(adminToken))
                        .param("minPrice", "100")
                        .param("maxPrice", "200")
                        .param("sort", "price,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sort").value("price,asc"));

        mockMvc.perform(get("/api/reservations")
                        .header("Authorization", bearer(adminToken))
                        .param("page", "0")
                        .param("size", "1")
                        .param("sort", "price,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalPages").value(greaterThanOrEqualTo(3)));
    }

    @Test
    void validationErrorsReturnBadRequest() throws Exception {
        String adminToken = login("admin", "Admin@123");
        String userToken = login("user", "User@123");

        mockMvc.perform(post("/api/resources")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "type": "ROOM",
                                  "hourlyRate": -5
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray());

        LocalDateTime start = LocalDateTime.now().plusDays(14).withHour(10).withMinute(0).withSecond(0).withNano(0);
        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "resourceId": 1,
                                  "startTime": "%s",
                                  "endTime": "%s",
                                  "price": -1
                                }
                                """.formatted(start, start.minusHours(1))))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/reservations")
                        .header("Authorization", bearer(adminToken))
                        .param("status", "UNKNOWN"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void userCanCancelOwnReservationButCannotDelete() throws Exception {
        String userToken = login("user", "User@123");
        LocalDateTime start = LocalDateTime.now().plusDays(15).withHour(11).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(2);

        MvcResult created = mockMvc.perform(post("/api/reservations")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "resourceId": 3,
                                  "startTime": "%s",
                                  "endTime": "%s",
                                  "price": 30.00
                                }
                                """.formatted(start, end)))
                .andExpect(status().isCreated())
                .andReturn();
        long id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/api/reservations/" + id + "/cancel").header("Authorization", bearer(userToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        mockMvc.perform(delete("/api/reservations/" + id).header("Authorization", bearer(userToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/reservations/" + id)
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "resourceId": 3,
                                  "startTime": "%s",
                                  "endTime": "%s",
                                  "price": 30.00,
                                  "status": "CONFIRMED"
                                }
                                """.formatted(start, end)))
                .andExpect(status().isForbidden());
    }

    @Test
    void missingResourceReturnsNotFound() throws Exception {
        String adminToken = login("admin", "Admin@123");
        mockMvc.perform(get("/api/resources/99999").header("Authorization", bearer(adminToken)))
                .andExpect(status().isNotFound());
    }

    private String login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(username, password))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), LoginResponse.class).accessToken();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
