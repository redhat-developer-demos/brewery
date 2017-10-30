package io.spring.cloud.samples.brewery.reporting;

import io.opentracing.ActiveSpan;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.spring.cloud.samples.brewery.common.events.Event;
import io.spring.cloud.samples.brewery.common.events.EventSink;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Headers;

import java.util.Map;

@MessageEndpoint
@Slf4j
class EventListener {

    private final ReportingRepository reportingRepository;
    private final Tracer tracer;

    @Autowired
    public EventListener(ReportingRepository reportingRepository, Tracer tracer) {
        this.reportingRepository = reportingRepository;
        this.tracer = tracer;
    }

    @ServiceActivator(inputChannel = EventSink.INPUT)
    public void handleEvents(Event event, @Headers Map<String, Object> headers) throws InterruptedException {
        log.info("Received the following message with headers [{}] and body [{}]", headers, event);
        //FIXME - need to send parent span automatically - right now this wil be disconnected
        ActiveSpan newSpan = tracer.buildSpan("inside_reporting")
            .startActive();
        reportingRepository.createOrUpdate(event);
        newSpan.log("savedEvent");
        log.info("Saved event to the db", headers, event);
        newSpan.close();
    }
}
