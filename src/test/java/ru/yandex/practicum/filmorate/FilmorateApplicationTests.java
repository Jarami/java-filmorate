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
import java.util.*;
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
				User user = createUser("bob@mail.ru;bob;Bob;2000-08-20").getBody();
				createUser("jack@mail.ru;jack;Jack;2010-08-20");

				ResponseEntity<User> resp = getUserById(user.getId());
				assertStatus(200, resp);

				User actualUser = resp.getBody();
				assertNotNull(actualUser);
				assertUserEquals(user, actualUser);
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

		@Nested
		class FriendTests {
			@Test
			void givenExistingUsers_whenAddFriends_gotFriendship() {
				User user1 = createUser("my1@email.com;login1;name1;2024-01-01").getBody();
				User user2 = createUser("my2@email.com;login2;name2;2024-02-01").getBody();

				addFriend(user1.getId(), user2.getId());

				ResponseEntity<User[]> respUser1friends = getFriends(user1);
				ResponseEntity<User[]> respUser2friends = getFriends(user2);

				assertStatus(200, respUser1friends);
				assertStatus(200, respUser2friends);

				List<User> user1friends = Arrays.asList(respUser1friends.getBody());
				List<User> user2friends = Arrays.asList(respUser2friends.getBody());
				assertTrue(user1friends.contains(user2));
				assertTrue(user2friends.contains(user1));
			}

			@Test
			void givenNonExistingUsers_whenAddFriends_gotNoFriendship() {
				User user1 = createUser("my1@email.com;login1;name1;2024-01-01").getBody();

				assertThrows(HttpClientErrorException.NotFound.class, () ->
						addFriend(user1.getId(), user1.getId() + 1));

				assertThrows(HttpClientErrorException.NotFound.class, () ->
						addFriend(user1.getId() + 1, user1.getId()));
			}

			@Test
			void givenExistingUsers_whenRemoveFriends_gotNoFriendshipAnyMore() {
				User user1 = createUser("my1@email.com;login1;name1;2024-01-01").getBody();
				User user2 = createUser("my2@email.com;login2;name2;2024-02-01").getBody();
				addFriend(user1, user2);

				removeFromFriends(user1, user2);

				ResponseEntity<User[]> respUser1friends = getFriends(user1);
				ResponseEntity<User[]> respUser2friends = getFriends(user2);

				assertStatus(200, respUser1friends);
				assertStatus(200, respUser2friends);

				List<User> user1friends = Arrays.asList(respUser1friends.getBody());
				List<User> user2friends = Arrays.asList(respUser2friends.getBody());
				assertFalse(user1friends.contains(user2));
				assertFalse(user2friends.contains(user1));
			}

			@Test
			void givenUsersWithCommonFriends_whenGetCommon_gotThem() {
				User user1 = createUser("my1@email.com;login1;name1;2024-01-01").getBody();
				User user2 = createUser("my2@email.com;login2;name2;2024-02-01").getBody();
				User user3 = createUser("my3@email.com;login3;name3;2024-03-01").getBody();

				addFriend(user1.getId(), user2.getId());
				addFriend(user1.getId(), user3.getId());
				addFriend(user2.getId(), user1.getId());
				addFriend(user2.getId(), user3.getId());

				ResponseEntity<User[]> respCommonFriends = getCommonFriends(user1, user2);
				user3 = getUserById(user3.getId()).getBody(); // обновляем список друзей

				assertStatus(200, respCommonFriends);

				List<User> commonFriends = Arrays.asList(respCommonFriends.getBody());

				assertEquals(1, commonFriends.size());
				assertUserEquals(user3, commonFriends.getFirst());
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

		private ResponseEntity<User> getUserById(long id) {
			return get("/users/" + id, User.class);
		}

		private ResponseEntity<User> updateUser(User user) {
			return put("/users", user, User.class);
		}

		private ResponseEntity<Integer> deleteAllUsers() {
			return delete("/users", Integer.class);
		}

		private ResponseEntity<User> addFriend(User user, User friend) {
			return put("/users/" + user.getId() + "/friends/" + friend.getId(), User.class);
		}

		private ResponseEntity<User> addFriend(Long userId, Long friendId) {
			return put("/users/" + userId + "/friends/" + friendId, User.class);
		}

		private ResponseEntity<User[]> getFriends(User user) {
			return get("/users/" + user.getId() + "/friends", User[].class);
		}

		private ResponseEntity<Void> removeFromFriends(User user, User friend) {
			return delete("/users/" + user.getId() + "/friends/" + friend.getId());
		}

		private ResponseEntity<User[]> getCommonFriends(User user1, User user2) {
			return get("/users/" + user1.getId() + "/friends/common/" + user2.getId(), User[].class);
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

	private <T> ResponseEntity<T> put(String uri, Class<T> clazz) {
		return client.put().uri(uri).retrieve().toEntity(clazz);
	}

	private <T> ResponseEntity<T> delete(String uri, Class<T> clazz) {
		return client.delete().uri(uri).retrieve().toEntity(clazz);
	}

	private ResponseEntity<Void> delete(String uri) {
		return client.delete().uri(uri).retrieve().toBodilessEntity();
	}

	private void assertStatus(int statusCode, ResponseEntity<?> resp) {
		assertEquals(HttpStatusCode.valueOf(statusCode), resp.getStatusCode());
	}

}
