package ru.yandex.practicum.filmorate.dal.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserRowMapper implements RowMapper<User> {
    private static final String FIND_FRIENDS_QUERY = """
            SELECT friend_id, status 
            FROM friends 
            WHERE user_id = ?""";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("user_id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());

        Map<Long, User.FriendshipStatus> friends = new HashMap<>();
        jdbcTemplate.query(FIND_FRIENDS_QUERY, (friendRs) -> {
            Long friendId = friendRs.getLong("friend_id");
            String status = friendRs.getString("status");
            friends.put(friendId, User.FriendshipStatus.valueOf(status));
        }, user.getId());
        user.setFriends(friends);

        return user;
    }
}