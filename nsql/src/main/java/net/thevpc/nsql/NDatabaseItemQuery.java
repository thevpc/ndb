package net.thevpc.nsql;

import java.util.function.Predicate;
import java.util.stream.Stream;

public interface NDatabaseItemQuery {
    NDatabaseItemQuery whereContentText(Predicate<String> test);
    NDatabaseItemQuery whereStructureText(Predicate<String> test);
    NDatabaseItemQuery whereText(Predicate<String> test);
    NDatabaseItemQuery whereContent(Predicate<NSqlColumnValue> test);
    Stream<NSqlSearchItem> run();
}
