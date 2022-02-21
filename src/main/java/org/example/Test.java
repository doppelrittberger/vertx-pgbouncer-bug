package org.example;

import io.quarkus.runtime.StartupEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class Test {
    @Inject
    VertxEventStore vertxEventStore;
    @Inject
    JdbiEventStore jdbiEventStore;

    void onStart(@Observes StartupEvent e) {
        List<Event> events = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Event event = new Event(UUID.randomUUID().toString(), "test", "test" + i, ZonedDateTime.now(), "{}".getBytes(StandardCharsets.UTF_8));
            events.add(event);
        }
        try {
            vertxEventStore.appendToStream("test", events, 0L);
            final List<Event> readEvents = vertxEventStore.readStream("test");
            System.out.println(readEvents);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            System.err.println("First case fails");
            // fails
        }
        try {
            jdbiEventStore.appendToStream("test", events, 0L);
            final List<Event> readEvents = jdbiEventStore.readStream("test");
            System.out.println("Second case succeeds: " + readEvents);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            System.err.println("Second case fails");
            // fails as well
        }
    }

}
