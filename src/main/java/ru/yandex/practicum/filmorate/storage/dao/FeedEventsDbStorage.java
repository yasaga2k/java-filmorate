package ru.yandex.practicum.filmorate.storage.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.FeedEvents;

import java.util.List;

@Component
@Slf4j
public class FeedEventsDbStorage extends BaseRepository<FeedEvents> {
    private static final String FIND_BY_USER_ID = """
            SELECT f.event_id,
                   f.event_time,
                   f.user_id,
                   f.entity_id,
                   et.name AS event_type,
                   o.name AS operation
            FROM feed_events f
            JOIN event_types et ON f.event_type_id = et.id
            JOIN operations o ON f.operation_id = o.id
            WHERE f.user_id = ?
            """;
    private static final String SAVE = """
            INSERT INTO feed_events
            (event_time, user_id, event_type_id, operation_id, entity_id)
            VALUES (?, ?, ?, ?, ?)
            """;
    private static final String GET_ID_OF_EVENT = "SELECT id FROM event_types WHERE name = ?";
    private static final String GET_ID_OF_OPERATION = "SELECT id FROM operations WHERE name = ?";

    public FeedEventsDbStorage(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, (rs, rowNum) -> {
            FeedEvents event = new FeedEvents();
            event.setEventId(rs.getInt("event_id"));
            event.setTimestamp(rs.getLong("event_time"));
            event.setUserId(rs.getInt("user_id"));
            event.setEntityId(rs.getInt("entity_id"));
            event.setEventType(rs.getString("event_type"));
            event.setOperation(rs.getString("operation"));
            return event;
        });
    }

    public void save(FeedEvents event) {
        Integer eventTypeId = jdbc.queryForObject(
                GET_ID_OF_EVENT,
                Integer.class,
                event.getEventType()
        );

        Integer operationId = jdbc.queryForObject(
                GET_ID_OF_OPERATION,
                Integer.class,
                event.getOperation()
        );

        jdbc.update(SAVE,
                event.getTimestamp(),
                event.getUserId(),
                eventTypeId,
                operationId,
                event.getEntityId()
        );
    }

    public List<FeedEvents> findByUserId(long userId) {
        return findMany(FIND_BY_USER_ID, userId);
    }
}