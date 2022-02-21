package org.example;

import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class VertxEventStore {
    private static final int QUERY_TIMEOUT = 5;

    public static final String STREAM_ASC = "SELECT event_id,event_type,event,timestamp FROM events WHERE stream_name = $1 ORDER BY id ASC";
    public static final String INSERT_EVENT = "INSERT INTO events(stream_name,stream_version,event_id,event_type,event,timestamp) VALUES ($1,$2,$3,$4,$5::JSON,$6)";
    public static final String EVENT_ID_COLUMN = "event_id";
    public static final String EVENT_TYPE_COLUMN = "event_type";
    public static final String EVENT_COLUMN = "event";
    public static final String TIMESTAMP_COLUMN = "timestamp";

    @Inject
    PgPool pgPool;

    public List<Event> readStream(String streamName) {
        return pgPool.preparedQuery(STREAM_ASC)
                .execute(Tuple.of(streamName))
                .onItem().transform(rows -> {
                    List<Event> list = new ArrayList<>();
                    for (final Row row : rows) {
                        list.add(from(row, streamName));
                    }
                    return list;
                })
                .await().atMost(Duration.ofSeconds(QUERY_TIMEOUT));
    }

    public void appendToStream(String streamName, List<Event> list, long expectedVersion) {
        List<Tuple> batch = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Event event = list.get(i);
            batch.add(Tuple.of(streamName, expectedVersion + (i + 1), event.getId(), event.getName(), new JsonObject(new String(event.getPayload(), StandardCharsets.UTF_8)), event.getStamp().toLocalDateTime()));
        }
        pgPool.getConnection()
                .onItem().transform(c -> c.preparedQuery(INSERT_EVENT).executeBatch(batch)
                        .ifNoItem().after(Duration.ofSeconds(QUERY_TIMEOUT)).fail())
                .await().indefinitely();
    }

    private Event from(Row row, String streamName) {
        final String eventId = row.getString(EVENT_ID_COLUMN);
        final String eventType = row.getString(EVENT_TYPE_COLUMN);
        final byte[] payload = row.getJsonObject(EVENT_COLUMN).toBuffer().getBytes();
        final ZonedDateTime timestamp = ZonedDateTime.from(row.getLocalDateTime(TIMESTAMP_COLUMN).atZone(ZoneOffset.UTC));
        return new Event(eventId, eventType, streamName, timestamp, payload);
    }
}
