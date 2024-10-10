package ru.yandex.practicum.filmorate;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FilmorateApplicationTests {

	@Autowired
	private ServerProperties serverProperties;

	@Autowired
	private ServletWebServerApplicationContext webServerAppCtxt;

	private RestClient client;

	@BeforeEach
	void init() {
		client = RestClient.create("http://localhost:" + webServerAppCtxt.getWebServer().getPort());
	}

	@Test
	void contextLoads() {
	}

	@Test
	void givenNoUsersYet_whenRequest_gotEmptyArray() {
		ResponseEntity<User[]> resp = get("/users", User[].class);

		assertEquals(HttpStatusCode.valueOf(200), resp.getStatusCode());

		User[] users = (User[])resp.getBody();
		assertEquals(0, users.length);
	}

	@Test
	void givenValidUser_whenCreate_gotSuccess() {
		User userToCreate = new User(null, "mail@mail.ru", "dolore", "Nick Name",
				LocalDate.parse("1946-08-20"));

		ResponseEntity<User> resp = post("/users", userToCreate, User.class);
		assertStatus(201, resp);

		User user = resp.getBody();
		assertNotNull(user.getId());
		assertEquals("mail@mail.ru", user.getEmail());
		assertEquals("dolore", user.getLogin());
		assertEquals("Nick Name", user.getName());
		assertEquals(LocalDate.parse("1946-08-20"), user.getBirthday());
	}

	@Test
	void givenUserWithSpaceInLogin_whenCreate_gotError() {
		User userToCreate = new User(null, "mail@mail.ru", "dolore ullamco", "name",
				LocalDate.parse("1946-08-20"));

		try {
			ResponseEntity<User> resp = post("/users", userToCreate, User.class);
			assertStatus(201, resp);
		} catch (Exception e) {
			log.error("опаньки", e);
		}
	}

	private <T> ResponseEntity<T> get(String uri, Class<T> clazz) {
		return client.get().uri(uri).retrieve().toEntity(clazz);
	}

	private <T> ResponseEntity<T> post(String uri, Object body, Class<T> clazz) {
		return client.post().uri(uri).body(body).retrieve()
				.onStatus(
						HttpStatusCode::is5xxServerError,
						(request, response) -> {
							throw new RuntimeException(response.getStatusCode().toString());
						}
				)
				.toEntity(clazz);
	}

	private void assertStatus(int statusCode, ResponseEntity<?> resp) {
		assertEquals(HttpStatusCode.valueOf(statusCode), resp.getStatusCode());
	}

}
