package ru.yandex.practicum.filmorate;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.practicum.filmorate.util.TestUtil.assertEmpty;
import static ru.yandex.practicum.filmorate.util.TestUtil.assertUserEquals;

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

	@Nested
	class UserTests {

		@AfterEach
		void shutdown() {
			deleteAllUsers();
		}

		@Nested
		class CreateTests {
			@Test
			void givenValidUser_whenCreate_gotSuccess() {
				ResponseEntity<User> resp = createUser("mail@mail.ru;dolore;Nick Name;1946-08-20");
				User user = resp.getBody();

				assertStatus(201, resp);
				assertNotNull(user);
				assertNotNull(user.getId());
				assertEquals("mail@mail.ru", user.getEmail());
				assertEquals("dolore", user.getLogin());
				assertEquals("Nick Name", user.getName());
				assertEquals(LocalDate.parse("1946-08-20"), user.getBirthday());
			}

			@Test
			void givenUserWithoutName_whenCreate_gotUserWithLoginInsteadOfName() {
				ResponseEntity<User> resp = createUser("my@email.com;login;NULL;2024-01-01");
				User user = resp.getBody();

				assertStatus(201, resp);
				assertNotNull(user);
				assertEquals("login", user.getName());
			}

			@Test
			void givenNoLogin_whenSave_gotFail() {
				assertThrows(HttpClientErrorException.BadRequest.class, () ->
					createUser("hello@main.ru;NULL;Nick Name;1946-08-20"));
			}

			@Test
			void givenUserWithSpaceInLogin_whenCreate_gotError() {
				assertThrows(HttpClientErrorException.BadRequest.class, () ->
					createUser("mail@main.ru;dolore ullamco;name;1946-08-20"));
			}

			@Test
			void givenLoginWithWrongEmail_whenCreate_gotFail() {
				assertThrows(HttpClientErrorException.BadRequest.class, () ->
					createUser("@main.ru;dolore;Nick Name;1946-08-20"));
			}

			@Test
			void givenLoginWithWrongBirthday_whenCreate_gotFail() {
				assertThrows(HttpClientErrorException.BadRequest.class, () ->
					createUser("mail@main.ru;dolore;Nick Name;2946-08-20"));
			}
		}

		@Nested
		class ReadTests {

			@Test
			void givenNoUsersYet_whenRequest_gotEmptyArray() {
				ResponseEntity<User[]> resp = getAllUsers();
				User[] users = resp.getBody();

				assertStatus(200, resp);
				assertNotNull(users);
				assertEmpty(users);
			}

			@Test
			void givenUsers_whenGetAll_gotAll() {
				createUser("bob@mail.ru;bob;Bob;2000-08-20");
				createUser("jack@mail.ru;jack;Jack;2010-08-20");

				ResponseEntity<User[]> resp = getAllUsers();
				User[] users = resp.getBody();
				Set<String> actualNames = Arrays.stream(users).map(User::getLogin).collect(Collectors.toSet());
				Set<String> expectedNames = Set.of("bob", "jack");

				assertStatus(200, resp);
				assertNotNull(users);
				assertEquals(expectedNames, actualNames);
			}

			@Test
			void givenExistingUserId_whenGetById_gotUser() {
				createUser("bob@mail.ru;bob;Bob;2000-08-20");
			}
		}

		@Nested
		class UpdateTests {

			@Test
			void givenExistingUser_whenUpdate_gotUpdated() {
				ResponseEntity<User> resp1 = createUser("my1@email.com;login1;name1;2024-01-01");
				ResponseEntity<User> resp2 = createUser("my2@email.com;login2;name2;2024-02-01");
				User user1 = resp1.getBody();
				User user2 = resp2.getBody();

				User updatedUser = new User(user1.getId(), "my-new@email.com", "new-login", "new-name",
						LocalDate.parse("2024-02-02"), List.of(user2));

				ResponseEntity<User> resp3 = updateUser(updatedUser);
				User actualUser = resp3.getBody();
				assertUserEquals(updatedUser, actualUser);
			}

			@Test
			void givenNonExistingUser_whenUpdate_gotError() {
				User user = new User(1L, "my@mail.ru", "login", "name",
						LocalDate.parse("2024-01-01"), new ArrayList<>());

				assertThrows(HttpClientErrorException.NotFound.class, () -> updateUser(user));
			}
		}

		@Nested
		class DeleteTests {
			@Test
			void givenUsers_whenDeleteAll_gotDeleted() {
				createUser("my1@email.com;login1;name1;2024-01-01");
				createUser("my2@email.com;login2;name2;2024-02-01");

				ResponseEntity<Integer> resp = deleteAllUsers();

				assertStatus(200, resp);
				assertEquals(2, resp.getBody());

				User[] users = getAllUsers().getBody();
				assertEmpty(users);
			}
		}

		private User parseUser(String userString) {
			String[] chunks = userString.split(";");
			return new User(
					chunks[0],
					chunks[1].equals("NULL") ? null : chunks[1],
					chunks[2].equals("NULL") ? null : chunks[2],
					LocalDate.parse(chunks[3])
			);
		}

		private ResponseEntity<User> createUser(String userString) {
			return post("/users", parseUser(userString), User.class);
		}

		private ResponseEntity<User[]> getAllUsers() {
			return get("/users", User[].class);
		}

		private ResponseEntity<User> updateUser(User user) {
			return put("/users", user, User.class);
		}

		private ResponseEntity<Integer> deleteAllUsers() {
			return delete("/users");
		}
	}



	private <T> ResponseEntity<T> get(String uri, Class<T> clazz) {
		return client.get().uri(uri).retrieve().toEntity(clazz);
	}

	private <T> ResponseEntity<T> post(String uri, Object body, Class<T> clazz) {
		return client.post().uri(uri).body(body).retrieve().toEntity(clazz);
	}

	private <T> ResponseEntity<T> put(String uri, Object body, Class<T> clazz) {
		return client.put().uri(uri).body(body).retrieve().toEntity(clazz);
	}

	private ResponseEntity<Integer> delete(String uri) {
		return client.delete().uri(uri).retrieve().toEntity(Integer.class);
	}

	private void assertStatus(int statusCode, ResponseEntity<?> resp) {
		assertEquals(HttpStatusCode.valueOf(statusCode), resp.getStatusCode());
	}

}
