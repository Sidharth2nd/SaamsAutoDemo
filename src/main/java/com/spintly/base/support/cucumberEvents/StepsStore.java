package com.spintly.base.support.cucumberEvents;

public class StepsStore {

    private static final ThreadLocal<String> threadStepDefMatch = new InheritableThreadLocal<String>();
    private static int numberOfSteps;

    private StepsStore() {
    }

    public static String get() {
        return threadStepDefMatch.get();
    }

    public static void set(String stepText) {
        if (get() != null) {
            if (!get().equals(stepText)) {
                numberOfSteps++;
            }
        }else{
            numberOfSteps++;
        }
        threadStepDefMatch.set(stepText);
    }

    public static void resetNumberOfSteps() {
        numberOfSteps = 0;
    }

    public static void remove() {
        threadStepDefMatch.remove();
    }
}