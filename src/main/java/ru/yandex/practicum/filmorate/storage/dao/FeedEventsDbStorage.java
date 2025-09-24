package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.FeedEvents;
import ru.yandex.practicum.filmorate.model.Operation;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeedEventsDbStorage {
    private static final String FIND_BY_USER_ID = """
            SELECT f.event_id, f.event_time, f.user_id, f.enti5ty_id,
                   et.name AS event_type,
                   o.name AS operation
            FROM feed_events f
            JOIN event_types et ON f.event_type_id = et.id
            JOIN operations o ON f.operation_id = o.id
            WHERE f.user_id = ?
            ORDER BY f.event_time DESC
            """;
    private static final String SAVE = """
            INSERT INTO feed_events (event_time, user_id, event_type_id, operation_id, entity_id)
            VALUES (?, ?, 
                   (SELECT id FROM event_types WHERE name = ?),
                   (SELECT id FROM operations WHERE name = ?),
                   ?)
            """;
    private final JdbcTemplate jdbcTemplate;

    public void save(FeedEvents event) {

        jdbcTemplate.update(SAVE,
                event.getTimestamp(),
                event.getUserId(),
                event.getEventType().name(),
                event.getOperation().name(),
                event.getEntityId()
        );
    }

    public List<FeedEvents> findByUserId(long userId) {


        return jdbcTemplate.query(FIND_BY_USER_ID, (rs, rowNum) -> {
            FeedEvents event = new FeedEvents();
            event.setEventId(rs.getInt("event_id"));
            event.setTimestamp(rs.getLong("timestamp"));
            event.setUserId(rs.getInt("user_id"));
            event.setEntityId(rs.getInt("entity_id"));
            event.setEventType(new EventType(rs.getString("event_type")));
            event.setOperation(new Operation(rs.getString("operation")));
            return event;
        }, userId);
    }
}