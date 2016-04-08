package org.commcare.tasks;

import org.commcare.network.ModernHttpRequest;
import org.commcare.tasks.templates.CommCareTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @author Phillip Mates (pmates@dimagi.com)
 */
public class SimpleHttpTask<R>
        extends CommCareTask<URL, Integer, InputStream, R> {


    private final ModernHttpRequest requestor;

    public SimpleHttpTask(String username, String password) {
        requestor = new ModernHttpRequest(username, password);
    }

    @Override
    protected InputStream doTaskBackground(URL... urls) {
        try {
            return requestor.makeModernRequest(urls[0]);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    protected void deliverResult(R r, InputStream stream) {

    }

    @Override
    protected void deliverUpdate(R r, Integer... update) {

    }

    @Override
    protected void deliverError(R r, Exception e) {

    }
}
