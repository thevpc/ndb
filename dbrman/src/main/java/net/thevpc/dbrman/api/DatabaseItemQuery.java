package net.thevpc.dbrman.api;

import java.util.function.Predicate;
import java.util.stream.Stream;

public interface DatabaseItemQuery {
    DatabaseItemQuery whereContentText(Predicate<String> test);
    DatabaseItemQuery whereStructureText(Predicate<String> test);
    DatabaseItemQuery whereText(Predicate<String> test);
    Stream<DbItem> run();
}
