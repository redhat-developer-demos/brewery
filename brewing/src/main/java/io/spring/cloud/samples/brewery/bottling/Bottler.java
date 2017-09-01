package io.spring.cloud.samples.brewery.bottling;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import io.opentracing.ActiveSpan;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.spring.cloud.samples.brewery.common.BottlingService;
import io.spring.cloud.samples.brewery.common.TestConfigurationHolder;
import io.spring.cloud.samples.brewery.common.model.Wort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
class Bottler implements BottlingService {

    private final BottlerService bottlerService;
    private final Tracer tracer;

    @Autowired
    public Bottler(BottlerService bottlerService, Tracer tracer) {
        this.bottlerService = bottlerService;
        this.tracer = tracer;
    }

    /**
     * [SLEUTH] TraceCommand
     */
    @Override
    public void bottle(Wort wort, String processId, String testCommunicationType, SpanContext spanContext) {
        log.info("I'm in the bottling service");
        log.info("Process ID from headers {}", processId);
        String groupKey = "bottling";
        String commandKey = "bottle";
        HystrixCommand.Setter setter = HystrixCommand.Setter
            .withGroupKey(HystrixCommandGroupKey.Factory.asKey(groupKey))
            .andCommandKey(HystrixCommandKey.Factory.asKey(commandKey));
        TestConfigurationHolder testConfigurationHolder = TestConfigurationHolder.TEST_CONFIG.get();
        new TraceCommand<Void>(tracer,spanContext, setter) {
            @Override
            public Void doRun() throws Exception {
                TestConfigurationHolder.TEST_CONFIG.set(testConfigurationHolder);
                log.info("Sending info to bottling service about process id [{}]", processId);
                bottlerService.bottle(wort, processId, spanContext);
                return null;
            }
        }.execute();
    }

    static abstract class TraceCommand<Void> extends HystrixCommand<Void> {

        private final SpanContext spanContext;
        private Span span;
        private final Tracer tracer;

        public TraceCommand(Tracer tracer,SpanContext spanContext, Setter setter) {
            super(setter);
            this.tracer = tracer;
            this.spanContext = spanContext;
        }

        @Override
        protected Void run() throws Exception {
            String commandKeyName = getCommandKey().name();

            this.span = this.tracer.buildSpan(commandKeyName)
                .asChildOf(this.spanContext)
                .withTag("commandKey", commandKeyName)
                .withTag("commandGroup", commandGroup.name())
                .withTag("threadPoolKey", threadPoolKey.name())
                .startManual();

            try {
                return doRun();
            } finally {
                this.span.finish();
            }
        }

        public abstract Void doRun() throws Exception;
    }
}
