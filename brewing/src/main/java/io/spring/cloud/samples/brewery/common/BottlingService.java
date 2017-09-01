package io.spring.cloud.samples.brewery.common;

import io.opentracing.SpanContext;
import io.spring.cloud.samples.brewery.common.model.Wort;

public interface BottlingService {
    void bottle(Wort wort, String processId, String testCommunicationType, SpanContext spanContext);
}
