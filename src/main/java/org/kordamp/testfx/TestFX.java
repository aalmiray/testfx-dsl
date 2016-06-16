/*
 * Copyright 2016 Andres Almiray
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the
 * European Commission - subsequent versions of the EUPL (the "Licence"); You may
 * not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the Licence for the
 * specific language governing permissions and limitations under the Licence.
 */
package org.kordamp.testfx;

import com.google.common.base.Predicate;
import javafx.geometry.HorizontalDirection;
import javafx.scene.Node;
import org.hamcrest.Matcher;
import org.testfx.api.FxRobot;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.testfx.util.WaitForAsyncUtils.waitFor;

public class TestFX extends FxRobot {
    //---------------------------------------------------------------------------------------------
    // Copied from org.testfx.service.support.WaitUntilSupport
    //---------------------------------------------------------------------------------------------

    public <T extends Node> void waitUntil(final T node,
                                           final Predicate<T> condition,
                                           int timeoutInSeconds) {
        awaitCondition(() -> condition.apply(node), timeoutInSeconds);
    }

    public <T extends Node> void waitUntil(final String message,
                                           final T node,
                                           final Predicate<T> condition,
                                           int timeoutInSeconds) {
        awaitCondition(message, () -> condition.apply(node), timeoutInSeconds);
    }

    public void waitUntil(final Node node,
                          final Matcher<Object> condition,
                          int timeoutInSeconds) {
        awaitCondition(() -> condition.matches(node), timeoutInSeconds);
    }

    public void waitUntil(final String message, final Node node,
                          final Matcher<Object> condition,
                          int timeoutInSeconds) {
        awaitCondition(message, () -> condition.matches(node), timeoutInSeconds);
    }

    public <T> void waitUntil(final T value,
                              final Matcher<? super T> condition,
                              int timeoutInSeconds) {
        awaitCondition(() -> condition.matches(value), timeoutInSeconds);
    }

    public <T> void waitUntil(final String message, final T value,
                              final Matcher<? super T> condition,
                              int timeoutInSeconds) {
        awaitCondition(message, () -> condition.matches(value), timeoutInSeconds);
    }

    public <T> void waitUntil(final Callable<T> callable,
                              final Matcher<? super T> condition,
                              int timeoutInSeconds) {
        awaitCondition(() -> condition.matches(callable.call()), timeoutInSeconds);
    }

    public <T> void waitUntil(final String message, final Callable<T> callable,
                              final Matcher<? super T> condition,
                              int timeoutInSeconds) {
        awaitCondition(message, () -> condition.matches(callable.call()), timeoutInSeconds);
    }

    //---------------------------------------------------------------------------------------------
    // https://github.com/TestFX/TestFX/pull/285
    //---------------------------------------------------------------------------------------------

    public void scroll(int positiveAmount,
                       HorizontalDirection direction) {
        if (direction == HorizontalDirection.RIGHT) {
            scrollRight(positiveAmount);
        } else if (direction == HorizontalDirection.LEFT) {
            scrollLeft(positiveAmount);
        }
    }

    //---------------------------------------------------------------------------------------------
    // PRIVATE METHODS.
    //---------------------------------------------------------------------------------------------

    private static final int SCROLL_ONE_UP_OR_LEFT = -1;
    private static final int SCROLL_ONE_DOWN_OR_RIGHT = 1;

    private void scrollRight(int positiveAmount) {
        for (int scrollTick = 0; scrollTick < positiveAmount; scrollTick++) {
            robotContext().getMouseRobot().scroll(SCROLL_ONE_DOWN_OR_RIGHT);
        }
    }

    private void scrollLeft(int positiveAmount) {
        for (int scrollTick = 0; scrollTick < positiveAmount; scrollTick++) {
            robotContext().getMouseRobot().scroll(SCROLL_ONE_UP_OR_LEFT);
        }
    }

    private void awaitCondition(Callable<Boolean> condition, int timeoutInSeconds) {
        awaitCondition(null, condition, timeoutInSeconds);
    }

    private void awaitCondition(String message, Callable<Boolean> condition, int timeoutInSeconds) {
        try {
            waitFor(timeoutInSeconds, TimeUnit.SECONDS, condition);
        } catch (Exception exception) {
            throw new RuntimeException(message, exception);
        }
    }
}
