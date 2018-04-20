/*
 * Created on 06.04.2018
 *
 */
package de.swingempire.fx.concurrency;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.*;

import javafx.concurrent.Task;

/**
 * Basic watcher for a single immutable dir.
 * TODO unregister on not running!
 * @author Jeanette Winzenburg, Berlin
 */
public class FileTask extends Task<List<WatchEvent<Path>>>{

    private static List<WatchEvent<Path>> EMPTY = Collections.emptyList();
    private Path directory;
    private WatchService watcher;

    // PENDING use a path to start with, then unregister (?)can be done from anywhere
    public FileTask(File file) throws IOException {
        directory = file.toPath();
        watcher = FileSystems.getDefault().newWatchService();
        directory.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
    }

    @Override
    protected List<WatchEvent<Path>> call() throws Exception {
        while (!isCancelled()) {
            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return EMPTY;
            }

            List<WatchEvent<Path>> events = new ArrayList<>();
            events.addAll((List<? extends WatchEvent<Path>>) key.pollEvents());
            updateValue(events);
            // copied from swing fileworker (isbn)
//            for (WatchEvent<?> event : key.pollEvents()) {
//                WatchEvent.Kind<?> kind = event.kind();
//                // TBD - provide example of how OVERFLOW event is handled
////                if (kind == OVERFLOW) {
////                    continue;
////                }
////                publish(createChangeEvent((WatchEvent<Path>) event, key));
//            }

            // reset key return if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                break;
            }
        }
        return EMPTY;
    }

    @Override
    protected void cancelled() {
        // TODO Auto-generated method stub
    }

    @Override
    protected void succeeded() {
        // TODO Auto-generated method stub
    }

    
}
