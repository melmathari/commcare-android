package org.commcare.tasks;

import android.content.Context;
import android.support.v4.util.Pair;

import org.commcare.interfaces.HttpResponseProcessor;
import org.commcare.network.ModernHttpRequester;
import org.commcare.tasks.templates.CommCareTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * @author Phillip Mates (pmates@dimagi.com)
 */
public class SimpleHttpTask
        extends CommCareTask<URL, Void, Void, HttpResponseProcessor>
        implements HttpResponseProcessor {

    public static final int SIMPLE_HTTP_TASK_ID = 11;

    private final ModernHttpRequester requestor;
    private int responseCode;
    private InputStream responseDataStream;
    private IOException ioException;

    public SimpleHttpTask(Context context, URL url,
                          List<Pair<String, String>> params,
                          boolean isPostRequest) {
        taskId = SIMPLE_HTTP_TASK_ID;
        requestor = new ModernHttpRequester(context, url, this, params, true, isPostRequest);
    }

    @Override
    protected Void doTaskBackground(URL... urls) {
        requestor.request();
        return null;
    }

    @Override
    protected void deliverResult(HttpResponseProcessor httpResponseProcessor,
                                 Void result) {
        if (ioException != null) {
            httpResponseProcessor.handleIOException(ioException);
        } else {
            ModernHttpRequester.processResponse(
                    httpResponseProcessor,
                    responseCode,
                    responseDataStream);
        }
    }

    @Override
    protected void deliverUpdate(HttpResponseProcessor httpResponseProcessor, Void... update) {

    }

    @Override
    protected void deliverError(HttpResponseProcessor httpResponseProcessor, Exception e) {

    }

    @Override
    public void processSuccess(int responseCode, InputStream responseData) {
        this.responseCode = responseCode;
        responseDataStream = responseData;
    }

    @Override
    public void processRedirection(int responseCode) {
        this.responseCode = responseCode;
    }

    @Override
    public void processClientError(int responseCode) {
        this.responseCode = responseCode;
    }

    @Override
    public void processServerError(int responseCode) {
        this.responseCode = responseCode;
    }

    @Override
    public void processOther(int responseCode) {
        this.responseCode = responseCode;
    }

    @Override
    public void handleIOException(IOException exception) {
        this.ioException = exception;
    }
}
