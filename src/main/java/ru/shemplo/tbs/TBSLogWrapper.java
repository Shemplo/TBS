package ru.shemplo.tbs;

import org.slf4j.helpers.MessageFormatter;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TBSLogWrapper {
    
    @Getter
    private final ObservableList <String> lines = FXCollections.observableArrayList ();
    
    public void info (String pattern, Object ... values) {
        Platform.runLater (() -> lines.add (format (pattern, values)));
        log.info (pattern, values);
    }
    
    public void error (String message, Throwable e) {
        Platform.runLater (() -> lines.add (message + " (message: " + e.getMessage () + ")"));
        log.error (message, e);
    }
    
    private String format (String pattern, Object ... values) {
        return MessageFormatter.arrayFormat (pattern, values).getMessage ();
    }
    
}
