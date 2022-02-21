package org.example;

import org.jdbi.v3.core.statement.PreparedBatch;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

@ApplicationScoped
public class JdbiEventStore {
    @Inject
    JdbiWrapper jdbi;

    public List<Event> readStream(String streamName) {
        return jdbi.withHandle(handle ->
                handle.createQuery("SELECT event_id,event_type,event,timestamp FROM events WHERE stream_name=:streamName ORDER BY id ASC")
                        .bind("streamName", streamName)
                        .map((rs, ctx) -> from(rs, streamName))
                        .list());
    }

    public void appendToStream(String streamName, List<Event> list, long expectedVersion) {
        jdbi.withHandle(handle -> {
            PreparedBatch preparedBatch = handle.prepareBatch("INSERT INTO events(stream_name,stream_version,event_id,event_type,event,timestamp) VALUES (:streamName,:streamVersion,:eventId,:eventType,:event::JSON,:timestamp)");
            for (int i = 0; i < list.size(); i++) {
                Event event = list.get(i);
                preparedBatch
                        .bind("streamName", streamName)
                        .bind("streamVersion", expectedVersion + (i + 1))
                        .bind("eventId", event.getId())
                        .bind("eventType", event.getName())
                        .bind("event", new String(event.getPayload(), StandardCharsets.UTF_8))
                        .bind("timestamp", event.getStamp())
                        .add();
            }
            preparedBatch.execute();
            return null;
        });
    }

    private Event from(ResultSet rs, String streamName) throws SQLException {
        final String eventId = rs.getString("event_id");
        final String eventType = rs.getString("event_type");
        final byte[] payload = rs.getString("event").getBytes(StandardCharsets.UTF_8);
        final ZonedDateTime timestamp = rs.getTimestamp("timestamp").toInstant().atZone(ZoneOffset.UTC);
        return new Event(eventId, eventType, streamName, timestamp, payload);
    }
}
