package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.mapper.FilmGenreMapper;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.mapper.FilmMpaMapper;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.util.TestUtil;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.practicum.filmorate.util.TestUtil.*;

@Slf4j
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FilmorateApplicationTests {

	private final ServletWebServerApplicationContext webServerAppCtxt;

	private RestClient client;

	private List<FilmGenre> allGenres;
	private List<FilmMpa> allMpa;
	private Map<String, FilmGenre> genreByName;
	private Map<String, FilmMpa> mpaByName;

	@BeforeEach
	void init() {
		client = RestClient.create("http://localhost:" + webServerAppCtxt.getWebServer().getPort());
		allGenres = getAllGenres();
		allMpa = getAllMpa();

		genreByName = new HashMap<>();
		allGenres.forEach(g -> genreByName.put(g.getName(), g));

		mpaByName = new HashMap<>();
		allMpa.forEach(m -> mpaByName.put(m.getName(), m));
	}

	@AfterEach
	void shutdown() {
		deleteAllReviews();
		deleteAllFilms();
		deleteAllUsers();
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
			void givenValidUser_whenCreate_getSuccess() {
				User user = createUser("mail@mail.ru;dolore;Nick Name;1946-08-20");

				assertNotNull(user);
				assertNotNull(user.getId());
				assertEquals("mail@mail.ru", user.getEmail());
				assertEquals("dolore", user.getLogin());
				assertEquals("Nick Name", user.getName());
				assertEquals("1946-08-20", user.getBirthday().toString());
			}

			@Test
			void givenUserWithoutName_whenCreate_getUserWithLoginInsteadOfName() {
				User user = createUser("my@email.com;login;NULL;2024-01-01");

				assertNotNull(user);
				assertEquals("login", user.getName());
			}

			@Test
			void givenNoLogin_whenSave_getBadRequest() {
				assertThrows(HttpClientErrorException.BadRequest.class, () ->
					createUser("hello@main.ru;NULL;Nick Name;1946-08-20"));
			}

			@Test
			void givenUserWithSpaceInLogin_whenCreate_getBadRequest() {
				assertThrows(HttpClientErrorException.BadRequest.class, () ->
					createUser("mail@main.ru;dolore ullamco;name;1946-08-20"));
			}

			@Test
			void givenLoginWithWrongEmail_whenCreate_getBadRequest() {
				assertThrows(HttpClientErrorException.BadRequest.class, () ->
					createUser("@main.ru;dolore;Nick Name;1946-08-20"));
			}

			@Test
			void givenLoginWithWrongBirthday_whenCreate_getBadRequest() {
				assertThrows(HttpClientErrorException.BadRequest.class, () ->
					createUser("mail@main.ru;dolore;Nick Name;2946-08-20"));
			}
		}

		@Nested
		class ReadTests {

			@Test
			void givenNoUsersYet_whenRequest_getEmptyArray() {
				User[] users = getAllUsers();

				assertNotNull(users);
				assertEmpty(users);
			}

			@Test
			void givenUsers_whenGetAll_getAll() {
				createUser("bob@mail.ru;bob;Bob;2000-08-20");
				createUser("jack@mail.ru;jack;Jack;2010-08-20");

				User[] users = getAllUsers();

				Set<String> actualLogins = Arrays.stream(users).map(User::getLogin).collect(Collectors.toSet());
				Set<String> expectedLogins = Set.of("bob", "jack");

				assertNotNull(users);
				assertEquals(expectedLogins, actualLogins);
			}

			@Test
			void givenExistingUserId_whenGetById_getUser() {
				User user = createUser("bob@mail.ru;bob;Bob;2000-08-20");
				createUser("jack@mail.ru;jack;Jack;2010-08-20");

				User actualUser = getUserById(user.getId());

				assertNotNull(actualUser);
				assertUserEquals(user, actualUser);
			}

			@Test
			void givenNonExistingUserId_whenGetById_getNotFound() {
				User user = User.builder()
						.id(1L)
						.email("my@mail.ru")
						.login("login")
						.name("name")
						.birthday(LocalDate.parse("2024-01-01"))
						.build();

				assertThrows(HttpClientErrorException.NotFound.class,
						() -> getUserById(user.getId()));
			}
		}

		@Nested
		class UpdateTests {

			@Test
			void givenExistingUser_whenUpdate_getUpdated() {
				User user1 = createUser("my1@email.com;login1;name1;2024-01-01");
				User user2 = createUser("my2@email.com;login2;name2;2024-02-01");

				UpdateUserRequest updatedUser = UpdateUserRequest.builder()
						.id(user1.getId())
						.email("my-new@email.com")
						.login("new-login")
						.name("new-name")
						.birthday(LocalDate.parse("2024-02-02"))
						.build();

				User actualUser = updateUser(updatedUser);
				assertUserEquals(updatedUser, actualUser);
			}

			@Test
			void givenNonExistingUser_whenUpdate_getNotFound() {
				UpdateUserRequest updateUserRequest = UpdateUserRequest.builder()
						.id(1L)
						.email("my@mail.ru")
						.login("login")
						.name("name")
						.birthday(LocalDate.parse("2024-01-01"))
						.build();

				assertThrows(HttpClientErrorException.NotFound.class,
						() -> updateUser(updateUserRequest));
			}

			@Test
			void givenNoLogin_whenSave_getBadRequest() {
				User user = createUser("my@email.com;login;name;2024-01-01");

				UpdateUserRequest updateUserRequest = UpdateUserRequest.from(user)
						.login(null)
						.build();

				assertThrows(HttpClientErrorException.BadRequest.class,
						() -> updateUser(updateUserRequest));
			}

			@Test
			void givenUserWithSpaceInLogin_whenCreate_getBadRequest() {
				User user = createUser("my@email.com;login;name;2024-01-01");

				UpdateUserRequest updateUserRequest = UpdateUserRequest.from(user)
						.login("space in login")
						.build();

				assertThrows(HttpClientErrorException.BadRequest.class,
						() -> updateUser(updateUserRequest));
			}

			@Test
			void givenLoginWithWrongEmail_whenCreate_getBadRequest() {
				User user = createUser("my@email.com;login;name;2024-01-01");

				UpdateUserRequest updateUserRequest = UpdateUserRequest.from(user)
						.email("@email.com")
						.build();

				assertThrows(HttpClientErrorException.BadRequest.class,
						() -> updateUser(updateUserRequest));
			}

			@Test
			void givenLoginWithWrongBirthday_whenCreate_getBadRequest() {
				User user = createUser("my@email.com;login;name;2024-01-01");
				user.setBirthday(LocalDate.parse("2946-08-20"));

				UpdateUserRequest updateUserRequest = UpdateUserRequest.from(user)
						.birthday(LocalDate.parse("2946-08-20"))
						.build();

				assertThrows(HttpClientErrorException.BadRequest.class,
						() -> updateUser(updateUserRequest));
			}
		}

		@Nested
		class DeleteTests {
			@Test
			void givenUsers_whenDeleteAll_getDeleted() {
				createUser("my1@email.com;login1;name1;2024-01-01");
				createUser("my2@email.com;login2;name2;2024-02-01");

				Integer deletedUsers = deleteAllUsers();

				assertEquals(2, deletedUsers);

				User[] users = getAllUsers();
				assertEmpty(users);
			}

			@Test
			void givenFilm_whenDelete_getDeleted() {
				User user1 = createUser("my1@email.com;login1;name1;2024-01-01");
				User user2 = createUser("my2@email.com;login2;name2;2024-02-01");

				deleteUser(user1.getId());

				User[] actualUsers = getAllUsers();
				assertEquals(1, actualUsers.length);
				assertEquals("name2", actualUsers[0].getName());
			}

			@Test
			void givenNonExistingUser_whenDelete_getNotFound() {
				User user = createUser("my1@email.com;login1;name1;2024-01-01");

				assertThrows(HttpClientErrorException.NotFound.class,
						() -> deleteUser(user.getId() + 1));
			}
		}

		@Nested
		class FriendTests {
			@Test
			void givenExistingUsers_whenAddFriends_getFriendship() {
				User user1 = createUser("my1@email.com;login1;name1;2024-01-01");
				User user2 = createUser("my2@email.com;login2;name2;2024-02-01");

				addFriend(user1.getId(), user2.getId());

				List<User> user1friends = Arrays.asList(getFriends(user1));
				List<User> user2friends = Arrays.asList(getFriends(user2));

				assertTrue(user1friends.contains(user2));
				assertFalse(user2friends.contains(user1));
			}

			@Test
			void givenNonExistingUsers_whenAddFriends_getNoFriendship() {
				User user1 = createUser("my1@email.com;login1;name1;2024-01-01");

				assertThrows(HttpClientErrorException.NotFound.class, () ->
						addFriend(user1.getId(), user1.getId() + 1));

				assertThrows(HttpClientErrorException.NotFound.class, () ->
						addFriend(user1.getId() + 1, user1.getId()));
			}

			@Test
			void givenExistingUsers_whenRemoveFriends_getNoFriendshipAnyMore() {
				User user1 = createUser("my1@email.com;login1;name1;2024-01-01");
				User user2 = createUser("my2@email.com;login2;name2;2024-02-01");
				addFriend(user1, user2);

				removeFromFriends(user1, user2);

				List<User> user1friends = Arrays.asList(getFriends(user1));

				assertFalse(user1friends.contains(user2));
			}

			@Test
			void givenUsersWithCommonFriends_whenGetCommon_getThem() {
				User user1 = createUser("my1@email.com;login1;name1;2024-01-01");
				User user2 = createUser("my2@email.com;login2;name2;2024-02-01");
				User user3 = createUser("my3@email.com;login3;name3;2024-03-01");

				addFriend(user1.getId(), user2.getId());
				addFriend(user1.getId(), user3.getId());
				addFriend(user2.getId(), user1.getId());
				addFriend(user2.getId(), user3.getId());

				List<User> commonFriends = Arrays.asList(getCommonFriends(user1, user2));

				assertEquals(1, commonFriends.size());
				assertEquals(user3.getId(), commonFriends.get(0).getId());
			}
		}

		private User[] getAllUsers() {
			return get("/users", User[].class).getBody();
		}

		private User getUserById(long id) {
			return get("/users/" + id, User.class).getBody();
		}

		private User updateUser(UpdateUserRequest updateUserRequest) {
			return put("/users", updateUserRequest, User.class).getBody();
		}

		private Void deleteUser(long id) {
			return delete("/users/" + id).getBody();
		}

		private User addFriend(User user, User friend) {
			return put("/users/" + user.getId() + "/friends/" + friend.getId(), User.class).getBody();
		}

		private User addFriend(Long userId, Long friendId) {
			return put("/users/" + userId + "/friends/" + friendId, User.class).getBody();
		}

		private User[] getFriends(User user) {
			return get("/users/" + user.getId() + "/friends", User[].class).getBody();
		}

		private Void removeFromFriends(User user, User friend) {
			return delete("/users/" + user.getId() + "/friends/" + friend.getId()).getBody();
		}

		private User[] getCommonFriends(User user1, User user2) {
			return get("/users/" + user1.getId() + "/friends/common/" + user2.getId(), User[].class).getBody();
		}
	}

	@Nested
	class FilmTests {

		@AfterEach
		void shutdown() {
			deleteAllFilms();
		}

		@Nested
		class CreateTests {

			@Test
			void givenValidUser_whenCreate_getSuccess() {

				Film film = createFilm("name;desc;2024-01-01;120;G;Комедия,Драма");

				assertNotNull(film);
				assertNotNull(film.getId());
				assertEquals("name", film.getName());
				assertEquals("desc", film.getDescription());
				assertEquals("2024-01-01", film.getReleaseDate().toString());
				assertEquals("G", film.getMpa().getName());
				assertEquals(Set.of("Комедия","Драма"), getGenreNames(film));
				assertEquals(120, film.getDuration());
			}

			@Test
			void givenFilmWithoutName_whenSave_getBadRequest() {
				assertThrows(HttpClientErrorException.BadRequest.class, () ->
						createFilm("NULL;desc;2024-01-01;120;G;Комедия,Драма"));
			}

			@Test
			void givenFilmWithEmptyName_whenSave_getBadRequest() {
				assertThrows(HttpClientErrorException.BadRequest.class, () ->
						createFilm(";desc;2024-01-01;120;G;Комедия,Драма"));
			}

			@Test
			void givenFilmWithTooLongDesc_whenSave_getBadRequest() {

				String tooLongDesc = "d".repeat(201);

				assertThrows(HttpClientErrorException.BadRequest.class, () ->
						createFilm("name;" + tooLongDesc + ";2024-01-01;120;G;Комедия,Драма"));
			}

			@Test
			void givenFilmWithNegativeDuration_whenSave_getBadRequest() {
				assertThrows(HttpClientErrorException.BadRequest.class, () ->
						createFilm("name;desc;2024-01-01;-1;G;Комедия,Драма"));
			}
		}

		@Nested
		class ReadTests {

			@Test
			void givenNoFilms_whenRequest_getEmptyArray() {
				Film[] films = getAllFilms();

				assertNotNull(films);
				assertEmpty(films);
			}

			@Test
			void givenFilms_whenGetAll_getAll() {
				createFilm("name1;desc1;2024-01-01;120;G;Комедия,Драма");
				createFilm("name2;desc2;2024-02-01;180;G;Комедия,Драма");

				Film[] films = getAllFilms();
				Set<String> actualNames = Arrays.stream(films).map(Film::getName).collect(Collectors.toSet());
				Set<String> expectedNames = Set.of("name1", "name2");

				assertNotNull(films);
				assertEquals(expectedNames, actualNames);
			}

			@Test
			void givenExistingFilmId_whenGetById_getIt() {
				Film film1 = createFilm("name;desc;2024-01-01;120;G;Комедия,Драма");
				Film film2 = createFilm("name;desc;2024-01-01;120;G;Комедия,Драма");

				Film actualFilm = getFilmById(film1.getId());

				assertNotNull(actualFilm);
				assertFilmEquals(film1, actualFilm);
			}

			@Test
			void givenNonExistingFilm_whenGetById_getNotFound() {
				Film film = createFilm("name;desc;2024-01-01;120;G;Комедия,Драма");

				assertThrows(HttpClientErrorException.NotFound.class,
						() -> getFilmById(film.getId() + 1));
			}
		}

		@Nested
		class UpdateTests {

			@Test
			void givenExistingFilm_whenUpdate_getUpdated() {
				Film film1 = createFilm("name1;desc1;2024-01-01;120;G;Комедия,Драма");
				Film film2 = createFilm("name2;desc2;2024-02-01;180;G;Комедия,Драма");

				UpdateFilmRequest updateFilmRequest = UpdateFilmRequest.builder()
						.id(film1.getId())
						.name("name")
						.description("desc")
						.mpa(mpaDto("PG"))
						.genres(genreDto("Комедия,Триллер"))
						.releaseDate(LocalDate.parse("2024-05-01"))
						.build();

				updateFilm(updateFilmRequest);

				Film updatedFilm = getFilmById(film1.getId());

				assertEquals("name", updatedFilm.getName());
				assertEquals("desc", updatedFilm.getDescription());
				assertEquals("PG", updatedFilm.getMpa().getName());
				assertEquals(Set.of("Комедия","Триллер"), getGenreNames(updatedFilm));
			}

			@Test
			void givenNonExistingUser_whenUpdate_getNotFound() {

				UpdateFilmRequest updateFilmRequest = UpdateFilmRequest.builder()
						.id(1L)
						.name("name")
						.description("desc")
						.releaseDate(LocalDate.parse("2024-02-02"))
						.duration(180)
						.mpa(mpaDto("G"))
						.genres(genreDto("Драма,Документальный"))
						.build();

				assertThrows(HttpClientErrorException.NotFound.class,
						() -> updateFilm(updateFilmRequest));
			}

			@Test
			void givenFilmWithoutName_whenUpdate_getBadRequest() {
				Film film = createFilm("name1;desc1;2024-01-01;120;G;Комедия,Драма");

				UpdateFilmRequest updateFilmRequest = UpdateFilmRequest.from(film)
					.name(null)
					.build();

				assertThrows(HttpClientErrorException.BadRequest.class,
						() -> updateFilm(updateFilmRequest));
			}

			@Test
			void givenFilmWithEmptyName_whenSave_getBadRequest() {
				Film film = createFilm("name1;desc1;2024-01-01;120;G;Комедия,Драма");

				UpdateFilmRequest updateFilmRequest = UpdateFilmRequest.from(film)
						.name("")
						.build();

				assertThrows(HttpClientErrorException.BadRequest.class,
						() -> updateFilm(updateFilmRequest));
			}

			@Test
			void givenFilmWithTooLongDesc_whenSave_getBadRequest() {
				Film film = createFilm("name1;desc1;2024-01-01;120;G;Комедия,Драма");

				UpdateFilmRequest updateFilmRequest = UpdateFilmRequest.from(film)
						.description("d".repeat(201))
						.build();

				assertThrows(HttpClientErrorException.BadRequest.class,
						() -> updateFilm(updateFilmRequest));
			}

			@Test
			void givenFilmWithNegativeDuration_whenSave_getBadRequest() {
				Film film = createFilm("name1;desc1;2024-01-01;120;G;Комедия,Драма");

				UpdateFilmRequest updateFilmRequest = UpdateFilmRequest.from(film)
						.duration(-1)
						.build();

				assertThrows(HttpClientErrorException.BadRequest.class,
						() -> updateFilm(updateFilmRequest));
			}
		}

		@Nested
		class DeleteTests {
			@Test
			void givenFilms_whenDeleteAll_getDeleted() {
				createFilm("name1;desc1;2024-01-01;120;G;Комедия,Драма");
				createFilm("name2;desc2;2024-02-01;180;G;Комедия,Драма");

				Integer deletedFilms = deleteAllFilms();

				assertEquals(2, deletedFilms);

				Film[] films = getAllFilms();
				assertEmpty(films);
			}

			@Test
			void givenFilm_whenDelete_getDeleted() {
				Film film1 = createFilm("name1;desc1;2024-01-01;120;G;Комедия,Драма");
				Film film2 = createFilm("name2;desc2;2024-02-01;180;G;Комедия,Драма");

				deleteFilm(film1.getId());

				Film[] actualFilms = getAllFilms();
				assertEquals(1, actualFilms.length);
				assertEquals("name2", actualFilms[0].getName());
			}

			@Test
			void givenNonExistingFilm_whenDelete_getNotFound() {
				Film film = createFilm("name1;desc1;2024-01-01;120;G;Комедия,Драма");

				assertThrows(HttpClientErrorException.NotFound.class,
						() -> deleteFilm(film.getId() + 1));
			}
		}

		private Film[] getAllFilms() {
			return get("/films", Film[].class).getBody();
		}

		private ResponseEntity<Film> updateFilm(UpdateFilmRequest updateFilmRequest) {
			return put("/films", updateFilmRequest, Film.class);
		}

		private ResponseEntity<Void> deleteFilm(long id) {
			return delete("/films/" + id);
		}
	}

	@Nested
	class LikeTests {

		List<User> users;
		List<Film> films;

		@BeforeEach
		void setup() {

			users = new ArrayList<>();
			for (int i = 1; i <= 11; i++) {
				NewUserRequest newUserRequest = NewUserRequest.builder()
						.name("name" + i)
						.login("login" + i)
						.email("mail" + i + "@mail.com")
						.birthday(LocalDate.parse("2000-01-01"))
						.build();

				users.add(createUser(newUserRequest));
			}

			films = new ArrayList<>();



			for (int i = 1; i <= 11; i++) {
				String genre = "Комедия";
				int year = 2010;
				if (i % 2 == 0) {
					year = 2020;
					genre = "Боевик";
				}
				NewFilmRequest newFilmRequest = NewFilmRequest.builder()
						.name("film name " + i)
						.description("film desc " + i)
						.releaseDate(LocalDate.of(year, 1, 1))
						.duration(10 * i + 10)
						.mpa(mpaDto("G"))
						.genres(genreDto(genre))
						.build();

				films.add(createFilm(newFilmRequest));
			}
		}

		@AfterEach
		void shutdown() {
			deleteAllUsers();
			deleteAllFilms();
		}

		@Test
		void givenUserHasNotLikedFilmYet_whenGetLikeCount_gotZero() {
			for (Film film : films) {
				assertEquals(0, film.getRate());
			}
		}

		@Test
		void givenUserHasNotLikedFilmYet_whenLike_gotLikeCountIncrease() {

			ResponseDto responseDto = like(films.get(7), users.get(3));
			Film film = getFilmById(films.get(7).getId());

			assertTrue(responseDto.isSuccess());
			assertEquals(films.get(7).getName(), film.getName());
			assertEquals(1, film.getRate());
		}

		@Test
		void givenUserHasAlreadyLikedFilm_whenLike_gotLikeCountNoChange() {
			like(films.get(7), users.get(3));
			like(films.get(7), users.get(3));

			Film film = getFilmById(films.get(7).getId());
			assertEquals(1, film.getRate());
		}

		@Test
		void givenUserHasNotLikedFilmYet_whenDislike_gotLikeCountNoChange() {
			like(films.get(7), users.get(3));
			ResponseDto resp = dislike(films.get(7), users.get(8));
			assertFalse(resp.isSuccess());

			Film film = getFilmById(films.get(7).getId());

			assertNotNull(film);
			assertEquals(films.get(7).getName(), film.getName());
			assertEquals(1, film.getRate());
		}

		@Test
		void givenUserHasAlreadyLikedFilm_whenDislike_gotLikeCountDecrease() {
			like(films.get(7), users.get(3));
			dislike(films.get(7), users.get(3));

			Film film = getFilmById(films.get(7).getId());
			assertNotNull(film);
			assertEquals(films.get(7).getName(), film.getName());
			assertEquals(0, film.getRate());
		}

		@Test
		void givenNoLikesAtAll_whenAskDefaultPopular_getTenRandomFilms() {
			Film[] films = getPopularFilms();

			assertNotNull(films);
			assertEquals(10, films.length);
		}

		@Test
		void givenNoLikesAtAll_whenAskPopularFive_getFiveRandomFilms() {
			Film[] films = getPopularFilms(5);

			assertNotNull(films);
			assertEquals(5, films.length);
		}

		@Test
		void givenNineFilmsLiked_whenAskPopular10_getPopular() {
			// 9-ый фильм лайкаем 9 раз, 8-ой - 8 раз и т.д.
			for (int j = 9; j > 0; j--) {
				for (int i = 0; i < j; i++) {
					like(films.get(j), users.get(i));
				}
			}

			Film[] films = getPopularFilms(10);
			assertEquals(9, films[0].getRate());
			assertEquals(8, films[1].getRate());
			assertEquals(7, films[2].getRate());
			assertEquals(6, films[3].getRate());
			assertEquals(5, films[4].getRate());
			assertEquals(4, films[5].getRate());
			assertEquals(3, films[6].getRate());
			assertEquals(2, films[7].getRate());
			assertEquals(1, films[8].getRate());
			assertEquals(0, films[9].getRate());
		}

		@Test
		void givenPopularFilmsByYearOrGenre_getPopularByGenreOrYear() {
			// комедия (жарн 1) год 2010 (четные) 0 2 4 6 8 10
			// боевик (жанр 6) год 2020 (нечетные) 1 3 5 7 9
			// Лайкаем только фильмы 2010 года с жарном 1 (четные)
			for (int j = 9; j > 0; j--) {
				for (int i = 0; i <= j; i++) {
					if (j % 2 == 0) {
						like(films.get(j), users.get(i));
					}
				}
			}

			Film[] filmsByYear = getPopularFilmsByYear(10, 2010);
			Set<Integer> filmYears = new HashSet<>();
			Arrays.stream(filmsByYear).forEach(film -> {
				filmYears.add(film.getReleaseDate().getYear());
			});

			assertEquals(1, filmYears.size(), "В выборку попали фильмы другого года");
			assertEquals(2010, filmYears.iterator().next()); // и только 2010
			assertEquals(9, filmsByYear[0].getRate());

			//Берем ту же самую выборку фильмов, но по Жанрам
			Film[] filmsByGenres = getPopularFilmsByGenre(10, 1);
			assertEquals(filmsByYear.length, filmsByGenres.length, "Выборка по году отличается от " +
					"выборки по жарну");

			//Берем выборку по фильму И по году
			Film[] filmsByYearAndGenres = getPopularFilmsByYearAndGenre(10, 2010, 1);
			assertEquals(filmsByYear.length, filmsByYearAndGenres.length, "Выборка по году отличается от " +
					"выборки по году И жанру");
		}
	}

	@Nested
	class FilmReviewTests {
		List<User> users;
		List<Film> films;

		@BeforeEach
		void setup() {

			users = new ArrayList<>();
			for (int i = 1; i <= 11; i++) {
				NewUserRequest newUserRequest = NewUserRequest.builder()
						.name("name" + i)
						.login("login" + i)
						.email("mail" + i + "@mail.com")
						.birthday(LocalDate.parse("2000-01-01"))
						.build();

				users.add(createUser(newUserRequest));
			}

			films = new ArrayList<>();
			for (int i = 1; i <= 11; i++) {
				NewFilmRequest newFilmRequest = NewFilmRequest.builder()
						.name("film name " + i)
						.description("film desc " + i)
						.releaseDate(LocalDate.parse("2010-01-01"))
						.duration(10 * i + 10)
						.mpa(mpaDto("G"))
						.genres(genreDto("Комедия"))
						.build();

				films.add(createFilm(newFilmRequest));
			}
		}

		@Test
		@DisplayName("Сохранение отзывов")
		void givenNewReview_whenSave_gotSaved() {
			User user = createUser();
			Film film = createFilm();
			FilmReview review = createReview(film, user);

			assertNotNull(review.getReviewId());

			FilmReview actReview = getFilmReviewById(review.getReviewId());

			assertReviewEquals(review, actReview);
		}

		@Test
		@DisplayName("Попытка сохранить отзыв без содержимого вызывает ошибку 400 или 500")
		void givenNewReviewWithoutContent_whenSave_gotError() {
			User user = createUser();
			Film film = createFilm();

			Map<String, Object> request = Map.of("filmId", film.getId(), "userId", user.getId(),
					"isPositive", true);

			assertThrows(HttpClientErrorException.BadRequest.class,
					() -> createReview(request));
		}

		@Test
		@DisplayName("Попытка сохранить отзыв c несуществующим пользователем вызывает ошибку 404")
		void givenNewReviewWithWrongUser_whenSave_gotError() {
			Film film = createFilm();

			Map<String, Object> request = Map.of("filmId", film.getId(), "userId", -1,
					"content", "good movie", "isPositive", true);

			assertThrows(HttpClientErrorException.NotFound.class,
					() -> createReview(request));
		}

		@Test
		@DisplayName("Попытка сохранить отзыв без пользователя вызывает ошибку 400 или 500")
		void givenNewReviewWithoutUser_whenSave_gotError() {
			Film film = createFilm();

			Map<String, Object> request = Map.of("filmId", film.getId(), "content", "good movie",
					"isPositive", true);

			assertThrows(HttpClientErrorException.BadRequest.class,
					() -> createReview(request));
		}

		@Test
		@DisplayName("Попытка сохранить отзыв на несуществующий фильм вызывает ошибку 404")
		void givenNewReviewWithWrongFilm_whenSave_gotError() {
			User user = createUser();

			Map<String, Object> request = Map.of("filmId", -1, "userId", user.getId(),
					"content", "good movie", "isPositive", true);

			assertThrows(HttpClientErrorException.NotFound.class,
					() -> createReview(request));
		}

		@Test
		@DisplayName("Попытка сохранить отзыв на несуществующий фильм вызывает ошибку 400 ли 500")
		void givenNewReviewWithoutFilm_whenSave_gotError() {
			User user = createUser();

			Map<String, Object> request = Map.of("userId", user.getId(),
					"content", "good movie", "isPositive", true);

			assertThrows(HttpClientErrorException.BadRequest.class,
					() -> createReview(request));
		}

		@Test
		@DisplayName("Попытка сохранить отзыв без типа вызывает ошибку 400 или 500")
		void givenNewReviewWithoutType_whenSave_gotError() {
			User user = createUser();
			Film film = createFilm();

			Map<String, Object> request = Map.of("filmId", film.getId(), "userId", user.getId(),
					"content", "good movie");

			assertThrows(HttpClientErrorException.BadRequest.class,
					() -> createReview(request));
		}

		@Test
		@DisplayName("Обновление отзывов")
		void givenExistingReview_whenSave_gotUpdated() {
			User user = createUser();
			Film film = createFilm();
			FilmReview review = createReview(film, user);

			FilmReview updatedReview = FilmReview.builder()
					.reviewId(review.getReviewId())
					.userId(review.getUserId())
					.filmId(review.getFilmId())
					.content("Some new content")
					.isPositive(!review.isPositive())
					.build();

			updateReview(updatedReview);

			FilmReview actReview = getFilmReviewById(review.getReviewId());

			assertReviewEquals(updatedReview, actReview);
		}

		@Test
		@DisplayName("Получение отзывов для определенного фильма")
		void givenReviews_whenGetByFilmAndCount_gotIt() {
			Film film1 = createFilm();
			Film film2 = createFilm();

			createReviews(4, film1, createUser());
			createReviews(20, film2, createUser());

			List<FilmReview> reviews = getFilmReviewsByCountAndFilm(3, film1);
			assertEquals(3, reviews.size());

			reviews.forEach(review ->
					assertEquals(film1.getId(), review.getFilmId()));
		}

		@Test
		@DisplayName("Получение отзывов")
		void givenReviews_whenGetByCount_gotIt() {
			Film film1 = createFilm();
			Film film2 = createFilm();

			createReviews(10, film1, createUser());
			createReviews(10, film2, createUser());

			List<FilmReview> filmReviews = getFilmReviewsByCount(15);
			assertEquals(15, filmReviews.size());
		}

		@Test
		@DisplayName("Удаление отзыва")
		void givenExistingReviews_whenDeleteOne_gotDeleted() {
			Film film1 = createFilm();
			Film film2 = createFilm();

			List<FilmReview> reviews1 = createReviews(2, film1, createUser());
			List<FilmReview> reviews2 = createReviews(1, film2, createUser());

			deleteFilmReviewById(reviews1.get(0).getReviewId());

			List<FilmReview> actFilmReviews = getFilmReviewsByCount(10);
			Set<Long> actReviewIds = actFilmReviews.stream().map(FilmReview::getReviewId).collect(Collectors.toSet());
			Set<Long> expReviewIds = Set.of(reviews1.get(1).getReviewId(), reviews2.get(0).getReviewId());

			assertEquals(expReviewIds, actReviewIds);
		}

		@Test
		@DisplayName("Удаление всех отзывов")
		void givenExistingReviews_whenDeleteAll_gotDeleted() {
			createReviews(2, createFilm(), createUser());
			createReviews(1, createFilm(), createUser());

			int deleted = deleteAllReviews();

			List<FilmReview> actFilmReviews = getFilmReviewsByCount(10);

			assertEquals(3, deleted);
			assertTrue(actFilmReviews.isEmpty());
		}

		@Test
		@DisplayName("После удаления отзыва попытка его получить вызывает 404 ошибку")
		void givenDeletedReview_whenRequestIt_gotNotFound() {
			Film film1 = createFilm();
			Film film2 = createFilm();

			List<FilmReview> reviews1 = createReviews(2, film1, createUser());
			List<FilmReview> reviews2 = createReviews(1, film2, createUser());

			deleteFilmReviewById(reviews1.get(0).getReviewId());

			assertThrows(HttpClientErrorException.NotFound.class, () -> {
				getFilmReviewById(reviews1.get(0).getReviewId());
			});
		}

		@Test
		@DisplayName("Лайк увеличивает рейтинг отзыва на 1")
		void givenReview_whenAddLike_gotRateIncreased() {
			User reviewAuthor = createUser();
			Film film = createFilm();
			FilmReview filmReview1 = createReview(film, reviewAuthor);
			FilmReview filmReview2 = createReview(film, reviewAuthor);

			User user = createUser();

			addLikeToReview(filmReview1, user);

			FilmReview actFilmReview = getFilmReviewById(filmReview1.getReviewId());

			assertEquals(1, actFilmReview.getRate());
		}


		@Test
		@DisplayName("Повторный лайк от того же пользователя не изменяет рейтинг")
		void givenReview_whenAddLikeAgain_gotNoRateChange() {
			User reviewAuthor = createUser();
			Film film = createFilm();
			FilmReview filmReview = createReview(film, reviewAuthor);

			User user = createUser();

			addLikeToReview(filmReview, user);
			addLikeToReview(filmReview, user);

			FilmReview actFilmReview = getFilmReviewById(filmReview.getReviewId());

			assertEquals(1, actFilmReview.getRate());
		}

		@Test
		@DisplayName("Дизлайк уменьшает рейтинг отзыва на 1")
		void givenLike_whenDeleteIt_gotRateDecreased() {
			User reviewAuthor = createUser();
			Film film = createFilm();
			FilmReview filmReview1 = createReview(film, reviewAuthor);
			FilmReview filmReview2 = createReview(film, reviewAuthor);

			User user = createUser();

			addLikeToReview(filmReview1, user);
			deleteLikeToReview(filmReview1, user);

			FilmReview actFilmReview = getFilmReviewById(filmReview1.getReviewId());

			assertEquals(0, actFilmReview.getRate());
		}

		@Test
		@DisplayName("Удаление несуществующего лайка не уменьшает рейтинг отзыва")
		void givenNoLike_whenDelete_gotNoRateChange() {
			User reviewAuthor = createUser();
			Film film = createFilm();
			FilmReview filmReview1 = createReview(film, reviewAuthor);
			FilmReview filmReview2 = createReview(film, reviewAuthor);

			User user1 = createUser();
			User user2 = createUser();

			addLikeToReview(filmReview1, user1);
			deleteLikeToReview(filmReview1, user2);

			FilmReview actFilmReview = getFilmReviewById(filmReview1.getReviewId());

			assertEquals(1, actFilmReview.getRate());
		}

		@Test
		@DisplayName("Дизлайк уменьшает рейтинг отзыва на 1")
		void givenReview_whenDislike_gotRateDecreased() {
			User reviewAuthor = createUser();
			Film film = createFilm();
			FilmReview filmReview1 = createReview(film, reviewAuthor);
			FilmReview filmReview2 = createReview(film, reviewAuthor);

			User user = createUser();

			addDislikeToReview(filmReview1, user);

			FilmReview actFilmReview = getFilmReviewById(filmReview1.getReviewId());

			assertEquals(-1, actFilmReview.getRate());
		}

		@Test
		@DisplayName("Повторный дизлайк от того же пользователя не изменяет рейтинг")
		void givenReview_whenDislikeAgain_gotNoRateChange() {
			User reviewAuthor = createUser();
			Film film = createFilm();
			FilmReview filmReview = createReview(film, reviewAuthor);

			User user = createUser();

			addDislikeToReview(filmReview, user);
			addDislikeToReview(filmReview, user);

			FilmReview actFilmReview = getFilmReviewById(filmReview.getReviewId());

			assertEquals(-1, actFilmReview.getRate());
		}

		@Test
		@DisplayName("Удаление дизлайка увеличивает рейтинг на 1")
		void givenDislike_whenDeleteIt_gotRateIncreased() {
			User reviewAuthor = createUser();
			Film film = createFilm();
			FilmReview filmReview1 = createReview(film, reviewAuthor);
			FilmReview filmReview2 = createReview(film, reviewAuthor);

			User user = createUser();

			addDislikeToReview(filmReview1, user);
			deleteDislikeToReview(filmReview1, user);

			FilmReview actFilmReview = getFilmReviewById(filmReview1.getReviewId());

			assertEquals(0, actFilmReview.getRate());
		}

		@Test
		@DisplayName("Удаление несуществующего дизлайка не изменяет рейтинг отзыва")
		void givenNoDislike_whenDelete_gotNoRateChange() {
			User reviewAuthor = createUser();
			Film film = createFilm();
			FilmReview filmReview1 = createReview(film, reviewAuthor);
			FilmReview filmReview2 = createReview(film, reviewAuthor);

			User user1 = createUser();
			User user2 = createUser();

			addDislikeToReview(filmReview1, user1);
			deleteDislikeToReview(filmReview1, user2);

			FilmReview actFilmReview = getFilmReviewById(filmReview1.getReviewId());

			assertEquals(-1, actFilmReview.getRate());
		}

		@Test
		@DisplayName("Замена лайка на дизлайк уменьшает рейтинг отзыва на 2")
		void givenLike_whenDislike_gotRateDecreasedByTwo() {
			User reviewAuthor = createUser();
			Film film = createFilm();
			FilmReview filmReview = createReview(film, reviewAuthor);
			User user = createUser();

			addLikeToReview(filmReview, user); // рейтинг +1
			addDislikeToReview(filmReview, user);

			FilmReview actFilmReview = getFilmReviewById(filmReview.getReviewId());
			assertEquals(-1, actFilmReview.getRate());
		}

		@Test
		void givenLikedReviews_whenGet_gotReviewsRateOrdered() {
			List<Film> films = createFilms(2);
			List<User> users = createUsers(2);

			FilmReview filmReview1 = createReview(films.get(0), users.get(0));
			FilmReview filmReview2 = createReview(films.get(1), users.get(0));
			FilmReview filmReview3 = createReview(films.get(0), users.get(1));

			addLikeToReview(filmReview1, users.get(1));
			addLikeToReview(filmReview2, users.get(0));
			addLikeToReview(filmReview2, users.get(1));

			List<FilmReview> filmReviews = getFilmReviewsByCount(5);

			assertEquals(3, filmReviews.size());
			assertEquals(filmReview2.getReviewId(), filmReviews.get(0).getReviewId());
			assertEquals(filmReview1.getReviewId(), filmReviews.get(1).getReviewId());
			assertEquals(filmReview3.getReviewId(), filmReviews.get(2).getReviewId());
		}

		@Test
		void givenLikedReviews_whenGetByFilm_gotReviewsRateOrdered() {
			List<Film> films = createFilms(2);
			List<User> users = createUsers(2);

			FilmReview filmReview1 = createReview(films.get(0), users.get(0));
			FilmReview filmReview2 = createReview(films.get(1), users.get(0));
			FilmReview filmReview3 = createReview(films.get(0), users.get(1));

			addLikeToReview(filmReview1, users.get(1));
			addLikeToReview(filmReview2, users.get(0));
			addLikeToReview(filmReview2, users.get(1));

			List<FilmReview> filmReviews = getFilmReviewsByCountAndFilm(5, films.get(0));

			assertEquals(2, filmReviews.size());
			assertEquals(filmReview1.getReviewId(), filmReviews.get(0).getReviewId());
			assertEquals(filmReview3.getReviewId(), filmReviews.get(1).getReviewId());
		}
	}

	@Nested
	class FilmDirectorsTest {
		Director director;
		Director director2;

		@BeforeEach
		void setUp() {
			director = Director.builder().name(TestUtil.randomString(15)).build();
			director2 = Director.builder().name(TestUtil.randomString(25)).build();
		}

		@AfterEach
		void shutdown() {
			delete("/directors");
		}

		@Test
		void givenNewDirector_whenSave_gotSuccess() {
			director = createDirector(director);
			director2 = selectDirector(director.getId());
			assertEquals(director.getId(), director2.getId());
		}

		@Test
		void givenNewDirectorWithNoName_whenSave_gotBadRequestError() {
			director.setName(" ");
			assertThrows(HttpClientErrorException.BadRequest.class,
					() -> createDirector(director));
		}

		@Test
		void givenNewDirectorWithToLongName_whenSave_gotBadRequestError() {
			director.setName(TestUtil.randomString(51));
			assertThrows(HttpClientErrorException.BadRequest.class,
					() -> createDirector(director));
		}

		@Test
		void givenNewDirectorName_whenUpdate_gotSuccess() {
			director = createDirector(director);
			director.setName("UpdatedName");
			Director selectDirector = updateDirector(director);

			assertEquals(director.getName(), selectDirector.getName());
		}

		@Test
		void givenCorrectDirector_whenUpdate_gotSuccess() {
			director = createDirector(director);
			director2 = createDirector(director2);
			delete("/directors/" + director2.getId(), Director.class).getBody();

			assertThrows(HttpClientErrorException.NotFound.class,
					() -> selectDirector(director2.getId()));

			assertEquals(director.getId(), selectDirector(director.getId()).getId());
		}

		@Test
		void givenIncorrectDirector_whenUpdate_gotNotFound() {
			assertThrows(HttpClientErrorException.NotFound.class,
					() -> selectDirector(Integer.MAX_VALUE));
		}

		@Test
		void givenFilmWithDirector_whenCreate_gotSuccess() {
			director = createDirector(director);
			List<Director> directors = List.of(director);

			NewFilmRequest filmRequest = parseNewFilmRequest("name;desc;2024-01-01;120;G;Комедия,Драма");
			filmRequest.setDirectors(directors);

			Film film = createFilm(filmRequest);

			assertEquals(film.getDirectors().get(0).getName(), directors.get(0).getName());

		}

		@Test
		void givenFilmWithDirectorNotInBd_whenCreate_gotBadRequest() {

			Director failedDirector = Director.builder()
					.id(Integer.MAX_VALUE / 2)
					.build();
			List<Director> directors = List.of(failedDirector);

			NewFilmRequest filmRequest = parseNewFilmRequest("FailedDirectorFilm;desc;2024-01-01;120;G;Комедия,Драма");
			filmRequest.setDirectors(directors);

			assertThrows(HttpClientErrorException.BadRequest.class,
					() -> createFilm(filmRequest));
		}

		@Test
		void givenFilmWithDirector_whenUpdate_gotSuccess() {
			director = createDirector(director);
			director2 = createDirector(director2);

			NewFilmRequest filmRequest = parseNewFilmRequest("name;desc;2024-01-01;120;G;Комедия,Драма");
			Film film = createFilm(filmRequest);

			UpdateFilmRequest updateFilmRequest = UpdateFilmRequest.builder()
					.id(film.getId())
					.name("UpdateFilm")
					.description(film.getDescription())
					.mpa(mpaDto("PG"))
					.genres(genreDto("Комедия,Триллер"))
					.releaseDate(LocalDate.parse("2024-05-01"))
					.directors(List.of(director, director2))
					.build();

			updateFilm(updateFilmRequest);

			Film updatedFilm = getFilmById(film.getId());


			assertEquals(updateFilmRequest.getDirectors().get(0).getId(), updatedFilm.getDirectors().get(0).getId());
			assertEquals(updateFilmRequest.getDirectors().get(1).getId(), updatedFilm.getDirectors().get(1).getId());
		}

		private Director selectDirector(int id) {
			return get("/directors/" + id, Director.class).getBody();
		}

		private Director createDirector(Director director) {
			return post("/directors", director, Director.class).getBody();
		}

		private Director updateDirector(Director director) {
			return put("/directors", director, Director.class).getBody();
		}

		private ResponseEntity<Film> updateFilm(UpdateFilmRequest updateFilmRequest) {
			return put("/films", updateFilmRequest, Film.class);
		}
	}

	private List<User> createUsers(int count) {
		return IntStream.range(0, count).mapToObj(i -> createUser()).toList();
	}

	private User createUser() {
		User user = TestUtil.getRandomUser();
		NewUserRequest request = NewUserRequest.builder()
				.name(user.getName())
				.login(user.getLogin())
				.email(user.getEmail())
				.birthday(user.getBirthday())
				.build();

		return createUser(request);
	}

	private User createUser(String userString) {
		NewUserRequest newUserRequest = parseNewUserRequest(userString);
		return createUser(newUserRequest);
	}

	private User createUser(NewUserRequest newUserRequest) {
		return post("/users", newUserRequest, User.class).getBody();
	}

	private List<Film> createFilms(int count) {
		return IntStream.range(0, count).mapToObj(i -> createFilm()).toList();
	}

	private Film createFilm() {
		Film film = TestUtil.getRandomFilm();
		film.setGenres(List.of(allGenres.get(0), allGenres.get(1)));
		film.setMpa(allMpa.get(0));
		NewFilmRequest newFilmRequest = FilmMapper.mapToNewFilmRequest(film);
		return createFilm(newFilmRequest);
	}

	private Film createFilm(String filmString) {
		NewFilmRequest newFilmRequest = parseNewFilmRequest(filmString);
		return createFilm(newFilmRequest);
	}

	private Film createFilm(NewFilmRequest newFilmRequest) {
		FilmDto filmDto = post("/films", newFilmRequest, FilmDto.class).getBody();
		return toFilm(filmDto);
	}

	private Film toFilm(FilmDto filmDto) {

		if (filmDto == null) {
			return null;
		}

		return Film.builder()
				.id(filmDto.getId())
				.name(filmDto.getName())
				.description((filmDto.getDescription()))
				.releaseDate(filmDto.getReleaseDate())
				.duration(filmDto.getDuration())
				.rate(filmDto.getRate())
				.mpa(filmDto.getMpa())
				.genres(filmDto.getGenres())
				.directors(filmDto.getDirectors())
				.build();
	}

	private List<FilmGenre> getAllGenres() {
		return Arrays.stream(Objects.requireNonNull(get("/genres", FilmGenre[].class).getBody())).toList();
	}

	private List<FilmMpa> getAllMpa() {
		return Arrays.stream(Objects.requireNonNull(get("/mpa", FilmMpa[].class).getBody())).toList();
	}

	private Film getFilmById(long id) {
		FilmDto filmDto = get("/films/" + id, FilmDto.class).getBody();
		return toFilm(filmDto);
	}

	private Integer deleteAllUsers() {
		return delete("/users", Integer.class).getBody();
	}

	private Integer deleteAllFilms() {
		return delete("/films", Integer.class).getBody();
	}

	private ResponseDto like(Film film, User user) {
		return put("/films/" + film.getId() + "/like/" + user.getId(), ResponseDto.class).getBody();
	}

	private ResponseDto dislike(Film film, User user) {
		return delete("/films/" + film.getId() + "/like/" + user.getId(), ResponseDto.class).getBody();
	}

	private Film[] getPopularFilms() {
		return get("/films/popular", Film[].class).getBody();
	}

	private Film[] getPopularFilmsByYear(int count, int year) {
		return get("/films/popular?count=" + count + "&year=" + year, Film[].class).getBody();
	}

	private Film[] getPopularFilmsByGenre(int count, int genreId) {
		return get("/films/popular?count=" + count + "&genreId=" + genreId, Film[].class).getBody();
	}

	private Film[] getPopularFilmsByYearAndGenre(int count, int year, int genreId) {
		return get("/films/popular?count=" + count + "&year=" + year + "&genreId=" + genreId, Film[].class).getBody();
	}

	private Film[] getPopularFilms(int count) {
		return get("/films/popular?count=" + count, Film[].class).getBody();
	}

	private NewUserRequest parseNewUserRequest(String userString) {

		String[] chunks = userString.split(";");
		return NewUserRequest.builder()
				.email(chunks[0].equals("NULL") ? null : chunks[0])
				.login(chunks[1].equals("NULL") ? null : chunks[1])
				.name(chunks[2].equals("NULL") ? null : chunks[2])
				.birthday(chunks[3].equals("NULL") ? null : LocalDate.parse(chunks[3]))
				.build();
	}

	private NewFilmRequest parseNewFilmRequest(String requestString) {
		String[] chunks = requestString.split(";");

		String mpaName = chunks[4].equals("NULL") ? null : chunks[4];
		String genreNames = chunks[5].equals("NULL") ? null : chunks[5];

		return NewFilmRequest.builder()
				.name(chunks[0].equals("NULL") ? null : chunks[0])
				.description(chunks[1].equals("NULL") ? null : chunks[1])
				.releaseDate(chunks[2].equals("NULL") ? null : LocalDate.parse(chunks[2]))
				.duration(Integer.parseInt(chunks[3]))
				.mpa(mpaDto(mpaName))
				.genres(genreDto(genreNames))
				.build();
	}

	private FilmMpaDto mpaDto(String mpaName) {
		if (mpaName == null) {
			return null;
		}

		FilmMpa filmMpa = mpaByName.get(mpaName);
		return FilmMpaMapper.mapToDto(filmMpa);
	}

	private List<FilmGenreDto> genreDto(String genreNames) {

		if (genreNames == null) {
			return null;
		}

		return Arrays.stream(genreNames.split(","))
				.map(genreName -> genreByName.get(genreName))
				.map(FilmGenreMapper::mapToDto)
				.toList();
	}

	private Set<String> getGenreNames(Film film) {
		return film.getGenres().stream().map(FilmGenre::getName).collect(Collectors.toSet());
	}

	private FilmReview createReview(Film film, User author) {
		FilmReview review = TestUtil.getRandomReview(film, author);
		NewFilmReviewRequest request = NewFilmReviewRequest.builder()
				.filmId(review.getFilmId())
				.userId(review.getUserId())
				.content(review.getContent())
				.isPositive(review.isPositive())
				.build();

		return createReview(request);
	}

	private FilmReview createReview(NewFilmReviewRequest request) {
		return post("/reviews", request, FilmReview.class).getBody();
	}

	private FilmReview createReview(Map<String, Object> request) {
		return post("/reviews", request, FilmReview.class).getBody();
	}

	private FilmReview updateReview(FilmReview review) {
		UpdateFilmReviewRequest request = UpdateFilmReviewRequest.builder()
				.reviewId(review.getReviewId())
				.filmId(review.getFilmId())
				.userId(review.getUserId())
				.content(review.getContent())
				.isPositive(review.isPositive())
				.build();
		return updateReview(request);
	}

	private FilmReview updateReview(UpdateFilmReviewRequest request) {
		return put("/reviews", request, FilmReview.class).getBody();
	}

	private FilmReview getFilmReviewById(long id) {
		return get("/reviews/" + id, FilmReview.class).getBody();
	}

	private Void deleteFilmReviewById(long id) {
		return delete("/reviews/" + id).getBody();
	}

	private List<FilmReview> getFilmReviewsByCountAndFilm(int count, Film film) {
		return Arrays.stream(get("/reviews?filmId=" + film.getId() + "&count=" + count, FilmReview[].class).getBody())
				.toList();
	}

	private List<FilmReview> getFilmReviewsByCount(int count) {
		return Arrays.stream(get("/reviews?&count=" + count, FilmReview[].class).getBody())
				.toList();
	}

	private Integer deleteAllReviews() {
		return delete("/reviews", Integer.class).getBody();
	}

	private List<FilmReview> createReviews(int count, Film film, User user) {
		return IntStream.range(0, count).mapToObj(i -> createReview(film, user)).toList();
	}

	private void addLikeToReview(FilmReview review, User user) {
		put("/reviews/" + review.getReviewId() + "/like/" + user.getId());
	}

	private void deleteLikeToReview(FilmReview review, User user) {
		delete("/reviews/" + review.getReviewId() + "/like/" + user.getId());
	}

	private void addDislikeToReview(FilmReview review, User user) {
		put("/reviews/" + review.getReviewId() + "/dislike/" + user.getId());
	}

	private void deleteDislikeToReview(FilmReview review, User user) {
		delete("/reviews/" + review.getReviewId() + "/dislike/" + user.getId());
	}

	private <T> ResponseEntity<T> get(String uri, Class<T> clazz) {
		log.info("get {}", uri);
		return client.get().uri(uri).retrieve().toEntity(clazz);
	}

	private <T> ResponseEntity<T> post(String uri, Object body, Class<T> clazz) {
		log.info("post {}", uri);
		return client.post().uri(uri).body(body).retrieve().toEntity(clazz);
	}

	private <T> ResponseEntity<T> put(String uri, Object body, Class<T> clazz) {
		log.info("put {}", uri);
		return client.put().uri(uri).body(body).retrieve().toEntity(clazz);
	}

	private <T> ResponseEntity<T> put(String uri, Class<T> clazz) {
		log.info("put {}", uri);
		return client.put().uri(uri).retrieve().toEntity(clazz);
	}

	private ResponseEntity<Void> put(String uri) {
		log.info("put {}", uri);
		return client.put().uri(uri).retrieve().toBodilessEntity();
	}

	private <T> ResponseEntity<T> delete(String uri, Class<T> clazz) {
		log.info("delete {}", uri);
		return client.delete().uri(uri).retrieve().toEntity(clazz);
	}

	private ResponseEntity<Void> delete(String uri) {
		log.info("delete {}", uri);
		return client.delete().uri(uri).retrieve().toBodilessEntity();
	}
}
