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

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import javafx.geometry.Bounds;
import javafx.geometry.HorizontalDirection;
import javafx.geometry.Point2D;
import javafx.geometry.VerticalDirection;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.MouseButton;
import javafx.stage.Window;
import org.hamcrest.Matcher;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobotException;
import org.testfx.service.query.NodeQuery;
import org.testfx.service.query.PointQuery;
import org.testfx.util.WaitForAsyncUtils;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static org.testfx.util.NodeQueryUtils.isVisible;
import static org.testfx.util.WaitForAsyncUtils.asyncFx;
import static org.testfx.util.WaitForAsyncUtils.waitFor;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

/**
 * @author Andres Almiray
 */
public abstract class QueryChain {
    private static final String ERROR_CONDITION_NULL = "Argument 'condition' must not be null";
    private static final String ERROR_MATCHER_NULL = "Argument 'matcher' must not be null";
    private static final String ERROR_PREDICATE_NULL = "Argument 'predicate' must not be null";
    private static final String ERROR_TYPE_NULL = "Argument 'type' must not be null";

    private final ThreadLocal<TestFX> testfx = new ThreadLocal<TestFX>() {
        @Override
        protected TestFX initialValue() {
            return new TestFX();
        }
    };

    protected NodeQuery nodeQuery;

    public static QueryChain $(String query) {
        return new StringQueryChain(query);
    }

    public static <T extends Node> QueryChain $(Matcher<T> query) {
        return new MatcherQueryChain<>(query);
    }

    public static <T extends Node> QueryChain $(Predicate<T> query) {
        return new PredicateQueryChain<>(query);
    }

    protected abstract String description();

    public TestFX testfx() {
        return testfx.get();
    }

    public <T extends Node> T node() {
        return nodeQuery.query();
    }

    public QueryChain $$(String query) {
        return new StringQueryChain(query);
    }

    public <T extends Node> QueryChain $$(Matcher<T> query) {
        return new MatcherQueryChain<>(query);
    }

    public <T extends Node> QueryChain $$(Predicate<T> query) {
        return new PredicateQueryChain<>(query);
    }

    //---------------------------------------------------------------------------------------------
    // METHODS FOR SCREEN CAPTURING.
    //---------------------------------------------------------------------------------------------

    public Image capture() {
        return testfx().capture(node());
    }

    //---------------------------------------------------------------------------------------------
    // METHODS FOR INTERACTION AND INTERRUPTION.
    //---------------------------------------------------------------------------------------------

    public QueryChain interact(Runnable runnable) {
        waitFor(asyncFx(requireNonNull(runnable, "Argument 'runnable' must not be null")));
        waitForFxEvents();
        return this;
    }

    public <T> QueryChain interact(Callable<T> callable) {
        waitFor(asyncFx(requireNonNull(callable, "Argument 'callable' must not be null")));
        waitForFxEvents();
        return this;
    }

    public <T extends Node, R> QueryChain interact(Class<T> type, Function<T, R> function) {
        requireNonNull(type, ERROR_TYPE_NULL);
        requireNonNull(function, "Argument 'function' must not be null");
        waitFor(asyncFx(() -> function.apply(type.cast(node()))));
        waitForFxEvents();
        return this;
    }

    public <T extends Node> QueryChain interact(Class<T> type, Consumer<T> consumer) {
        requireNonNull(type, ERROR_TYPE_NULL);
        requireNonNull(consumer, "Argument 'consumer' must not be null");
        waitFor(asyncFx(() -> consumer.accept(type.cast(node()))));
        waitForFxEvents();
        return this;
    }

    public QueryChain interrupt() {
        waitForFxEvents();
        return this;
    }

    public QueryChain interrupt(int attemptsCount) {
        waitForFxEvents(attemptsCount);
        return this;
    }

    //---------------------------------------------------------------------------------------------
    // METHODS for TYPING.
    //---------------------------------------------------------------------------------------------

    public QueryChain push(KeyCode... combination) {
        testfx().push(combination);
        return this;
    }

    public QueryChain push(KeyCodeCombination combination) {
        testfx().push(combination);
        return this;
    }

    public QueryChain type(KeyCode... keyCodes) {
        testfx().type(keyCodes);
        return this;
    }

    public QueryChain type(KeyCode keyCode,
                           int times) {
        testfx().type(keyCode, times);
        return this;
    }

    public QueryChain eraseText(int amount) {
        return type(KeyCode.BACK_SPACE, amount);
    }

    public QueryChain closeCurrentWindow() {
        return push(KeyCode.ALT, KeyCode.F4).sleep(100);
    }

    //---------------------------------------------------------------------------------------------
    // METHODS FOR WRITE.
    //---------------------------------------------------------------------------------------------

    public QueryChain write(char character) {
        testfx().write(character);
        return this;
    }

    public QueryChain write(String text) {
        testfx().write(text);
        return this;
    }

    //---------------------------------------------------------------------------------------------
    // METHODS FOR SLEEP.
    //---------------------------------------------------------------------------------------------

    public QueryChain sleep(long milliseconds) {
        testfx().sleep(milliseconds);
        return this;
    }

    public QueryChain sleep(long duration, TimeUnit timeUnit) {
        testfx().sleep(duration, timeUnit);
        return this;
    }

    //---------------------------------------------------------------------------------------------
    // METHODS FOR SCROLL.
    //---------------------------------------------------------------------------------------------

    public QueryChain scroll(int amount, VerticalDirection direction) {
        testfx().scroll(amount, direction);
        return this;
    }

    public QueryChain scroll(VerticalDirection direction) {
        scroll(1, direction);
        return this;
    }

    public QueryChain scroll(int amount, HorizontalDirection direction) {
        testfx().scroll(amount, direction);
        return this;
    }

    public QueryChain scroll(HorizontalDirection direction) {
        scroll(1, direction);
        return this;
    }

    //---------------------------------------------------------------------------------------------
    // METHODS FOR KEYBOARD.
    //---------------------------------------------------------------------------------------------

    public QueryChain press(KeyCode... keys) {
        testfx().press(keys);
        return this;
    }

    public QueryChain release(KeyCode... keys) {
        testfx().release(keys);
        return this;
    }

    //---------------------------------------------------------------------------------------------
    // METHODS FOR MOUSE.
    //---------------------------------------------------------------------------------------------

    public QueryChain press(MouseButton... buttons) {
        testfx().press(buttons);
        return this;
    }

    public QueryChain release(MouseButton... buttons) {
        testfx().release(buttons);
        return this;
    }

    //---------------------------------------------------------------------------------------------
    // METHODS FOR CLICKING.
    //---------------------------------------------------------------------------------------------

    public QueryChain click(MouseButton... buttons) {
        testfx().clickOn(pointOfVisibleNode(nodeQuery), buttons);
        return this;
    }

    public QueryChain doubleClick(MouseButton... buttons) {
        testfx().doubleClickOn(pointOfVisibleNode(nodeQuery), buttons);
        return this;
    }

    public QueryChain rightClick() {
        testfx().rightClickOn(pointOfVisibleNode(nodeQuery));
        return this;
    }

    //---------------------------------------------------------------------------------------------
    // METHODS FOR DRAG & DROP.
    //---------------------------------------------------------------------------------------------

    public QueryChain drag(MouseButton... buttons) {
        testfx().drag(pointOfVisibleNode(nodeQuery), buttons);
        return this;
    }

    public QueryChain drop() {
        testfx().drop();
        return this;
    }

    public QueryChain dropTo(PointQuery pointQuery) {
        testfx().dropTo(pointQuery);
        return this;
    }

    public QueryChain dropBy(double x, double y) {
        testfx().dropBy(x, y);
        return this;
    }

    public QueryChain dropTo(double x, double y) {
        return dropTo(testfx().point(x, y));
    }

    public QueryChain dropTo(Point2D point) {
        return dropTo(testfx().point(point));
    }

    public QueryChain dropTo(Bounds bounds) {
        return dropTo(testfx().point(bounds));
    }

    public QueryChain dropTo(Node node) {
        return dropTo(testfx().point(node));
    }

    public QueryChain dropTo(Scene scene) {
        return dropTo(testfx().point(scene));
    }

    public QueryChain dropTo(Window window) {
        return dropTo(testfx().point(window));
    }

    public QueryChain dropTo(String query) {
        return dropTo(pointOfVisibleNode(query));
    }

    public <T extends Node> QueryChain dropTo(Matcher<T> matcher) {
        return dropTo(pointOfVisibleNode(requireNonNull(matcher, ERROR_MATCHER_NULL)));
    }

    public <T extends Node> QueryChain dropTo(Predicate<T> predicate) {
        return dropTo(pointOfVisibleNode(requireNonNull(predicate, ERROR_PREDICATE_NULL)));
    }

    //---------------------------------------------------------------------------------------------
    // METHODS FOR MOVEMENT.
    //---------------------------------------------------------------------------------------------

    public QueryChain moveTo(PointQuery pointQuery) {
        testfx().moveTo(pointQuery);
        return this;
    }

    public QueryChain moveBy(double x,
                             double y) {
        testfx().moveBy(x, y);
        return this;
    }

    public QueryChain moveTo(double x,
                             double y) {
        return moveTo(testfx().point(new Point2D(x, y)));
    }

    public QueryChain moveTo(Point2D point) {
        return moveTo(testfx().point(point));
    }

    public QueryChain moveTo(Bounds bounds) {
        return moveTo(testfx().point(bounds));
    }

    public QueryChain moveTo(Node node) {
        return moveTo(testfx().point(node));
    }

    public QueryChain moveTo(Scene scene) {
        return moveTo(testfx().point(scene));
    }

    public QueryChain moveTo(Window window) {
        return moveTo(testfx().point(window));
    }

    public QueryChain moveTo(String query) {
        return moveTo(pointOfVisibleNode(query));
    }

    public <T extends Node> QueryChain moveTo(Matcher<T> matcher) {
        return moveTo(pointOfVisibleNode(requireNonNull(matcher, ERROR_MATCHER_NULL)));
    }

    public <T extends Node> QueryChain moveTo(Predicate<T> predicate) {
        return moveTo(pointOfVisibleNode(requireNonNull(predicate, ERROR_PREDICATE_NULL)));
    }

    //---------------------------------------------------------------------------------------------
    // METHODS FOR WAITING.
    //---------------------------------------------------------------------------------------------

    public <T extends Node> QueryChain waitUntil(final Predicate<T> condition,
                                                 int timeoutInSeconds) {
        requireNonNull(condition, ERROR_CONDITION_NULL);
        awaitCondition(() -> condition.apply(node()), timeoutInSeconds);
        return this;
    }

    public <T extends Node> QueryChain waitUntil(final String message,
                                                 final Predicate<T> condition,
                                                 int timeoutInSeconds) {
        requireNonNull(condition, ERROR_CONDITION_NULL);
        awaitCondition(message, () -> condition.apply(node()), timeoutInSeconds);
        return this;
    }

    public <T extends Node> QueryChain waitUntil(final Matcher<T> condition,
                                                 int timeoutInSeconds) {
        requireNonNull(condition, ERROR_CONDITION_NULL);
        awaitCondition(() -> condition.matches(node()), timeoutInSeconds);
        return this;
    }

    public <T extends Node> QueryChain waitUntil(final String message,
                                                 final Matcher<T> condition,
                                                 int timeoutInSeconds) {
        requireNonNull(condition, ERROR_CONDITION_NULL);
        awaitCondition(message, () -> condition.matches(node()), timeoutInSeconds);
        return this;
    }

    //---------------------------------------------------------------------------------------------
    // METHODS FOR VERIFYING STATE.
    //---------------------------------------------------------------------------------------------

    public <T extends Node> QueryChain verifyThat(Matcher<T> matcher) {
        FxAssert.verifyThat(nodeQuery, requireNonNull(matcher, ERROR_MATCHER_NULL));
        return this;
    }

    public <T extends Node> QueryChain verifyThat(Predicate<T> predicate) {
        FxAssert.verifyThat(nodeQuery, requireNonNull(predicate, ERROR_PREDICATE_NULL));
        return this;
    }

    //---------------------------------------------------------------------------------------------
    // PRIVATE MEMBERS.
    //---------------------------------------------------------------------------------------------

    private static class StringQueryChain extends QueryChain {
        private final String query;

        public StringQueryChain(String query) {
            this.query = query;
            this.nodeQuery = testfx().lookup(query);
        }

        @Override
        protected String description() {
            return "the query \"" + query + "\"";
        }
    }

    private static class MatcherQueryChain<T extends Node> extends QueryChain {
        private final Matcher<T> matcher;

        public MatcherQueryChain(Matcher<T> matcher) {
            this.matcher = requireNonNull(matcher, ERROR_MATCHER_NULL);
            this.nodeQuery = testfx().lookup(matcher);
        }

        @Override
        protected String description() {
            return "the matcher \"" + matcher.toString() + "\"";
        }
    }

    private static class PredicateQueryChain<T extends Node> extends QueryChain {
        public PredicateQueryChain(Predicate<T> predicate) {
            this.nodeQuery = testfx().lookup(requireNonNull(predicate, ERROR_PREDICATE_NULL));
        }

        @Override
        protected String description() {
            return "the predicate";
        }
    }

    //---------------------------------------------------------------------------------------------
    // PRIVATE METHODS.
    //---------------------------------------------------------------------------------------------

    private PointQuery pointOfVisibleNode(NodeQuery nodeQuery) {
        Node node = queryVisibleNode(nodeQuery, description());
        return testfx().point(node);
    }

    private PointQuery pointOfVisibleNode(String query) {
        NodeQuery nodeQuery = testfx().lookup(query);
        Node node = queryVisibleNode(nodeQuery, "the query \"" + query + "\"");
        return testfx().point(node);
    }

    private <T extends Node> PointQuery pointOfVisibleNode(Matcher<T> matcher) {
        NodeQuery nodeQuery = testfx().lookup(matcher);
        Node node = queryVisibleNode(nodeQuery, "the matcher \"" + matcher.toString() + "\"");
        return testfx().point(node);
    }

    private <T extends Node> PointQuery pointOfVisibleNode(Predicate<T> predicate) {
        NodeQuery nodeQuery = testfx().lookup(predicate);
        Node node = queryVisibleNode(nodeQuery, "the predicate");
        return testfx().point(node);
    }

    private Node queryVisibleNode(NodeQuery nodeQuery,
                                  String queryDescription) {
        Set<Node> resultNodes = nodeQuery.queryAll();
        if (resultNodes.isEmpty()) {
            String message = queryDescription + " returned no nodes.";
            throw new FxRobotException(message);
        }
        Optional<Node> resultNode = testfx().from(resultNodes).match(isVisible()).tryQuery();
        if (!resultNode.isPresent()) {
            String message = queryDescription + " returned " + resultNodes.size() + " nodes" +
                ", but no nodes were visible.";
            throw new FxRobotException(message);
        }
        return resultNode.get();
    }

    private void awaitCondition(Callable<Boolean> condition, int timeoutInSeconds) {
        awaitCondition(null, condition, timeoutInSeconds);
    }

    private void awaitCondition(String message, Callable<Boolean> condition, int timeoutInSeconds) {
        try {
            WaitForAsyncUtils.waitFor(timeoutInSeconds, TimeUnit.SECONDS, condition);
        } catch (Exception exception) {
            throw new RuntimeException(message, exception);
        }
    }
}
