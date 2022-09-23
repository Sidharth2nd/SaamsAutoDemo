package com.spintly.base.support.cucumberEvents;

import cucumber.api.PickleStepTestStep;
import cucumber.api.event.EventPublisher;
import cucumber.api.event.TestStepStarted;
import cucumber.api.formatter.Formatter;

public class EventsHandler implements Formatter {

    private cucumber.api.event.EventHandler<TestStepStarted> stepStartedHandler = new cucumber.api.event.EventHandler<TestStepStarted>() {
        @Override
        public void receive(TestStepStarted event) {
            handleTestStepStarted(event);
        }
    };

    public EventsHandler() {
    }
    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestStepStarted.class, stepStartedHandler);
    }

    private void handleTestStepStarted(TestStepStarted event) {
        if (event.testStep instanceof PickleStepTestStep) {
            PickleStepTestStep testStep = (PickleStepTestStep) event.testStep;
            StepsStore.set(testStep.getStepText());
        }

    }
}