package ru.shemplo.tbs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.shemplo.tbs.entity.ITBSProfile;
import ru.tinkoff.invest.openapi.OpenApi;
import ru.tinkoff.invest.openapi.model.rest.SandboxRegisterRequest;
import ru.tinkoff.invest.openapi.okhttp.OkHttpOpenApi;

@Slf4j
@NoArgsConstructor (access = AccessLevel.PRIVATE)
public class TBSClient implements AutoCloseable {
    
    private static volatile TBSClient instance;
    
    public static TBSClient getInstance () {
        if (instance == null) {
            synchronized (TBSClient.class) {
                if (instance == null) {
                    instance = new TBSClient ();
                }
            }
        }
        
        return instance;
    }
    
    private volatile OpenApi connection;
    
    private String readToken (ITBSProfile profile) throws IOException {
        log.info ("Reading token from file...");
        return Files.readString (Paths.get (profile.getTokenFilename ()));
    }
    
    public OpenApi getConnection (ITBSProfile profile) throws IOException {
        if (connection == null) {
            synchronized (this) {
                if (connection == null) {
                    final var token = readToken (profile);
                    if (token == null) { return null; }
                    
                    log.info ("Connecting to Tinkoff API...");
                    log.info ("Profile: {}", profile);
                    connection = new OkHttpOpenApi (token, !profile.isHighResponsible ());
                    log.info ("Perform registration in Tinkoff API...");
                    if (connection.isSandboxMode ()) {
                        connection.getSandboxContext ().performRegistration (
                            new SandboxRegisterRequest ()
                        ).join ();
                    }
                }
            }
        }
        
        return connection;
    }
    
    public void close () throws IOException {
        if (connection != null) {
            log.info ("Closing connection to Tinkoff API...");
            connection.close ();
        }
    }
    
}
