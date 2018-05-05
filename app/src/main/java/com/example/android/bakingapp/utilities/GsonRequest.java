package com.example.android.bakingapp.utilities;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by john on 10/03/18.
 */

public class GsonRequest<T> extends Request<T> {

    /** Default charset for JSON request. */
    protected static final String PROTOCOL_CHARSET = "utf-8";

    /** Content type for request. */
    private static final String PROTOCOL_CONTENT_TYPE =
            String.format("application/json; charset=%s", PROTOCOL_CHARSET);

    private final Gson gson = new Gson();
    private final Class<T> clazz;
    private final Map<String, String> headers;
    /**
     * Lock to guard mListener as it is cleared on cancel() and read on delivery.
     */
    private final Object lock = new Object();

    // @GuardedBy("lock")
    private Response.Listener<T> listener;
    private final String requestBody;

    /**
     * Make a GET request and return a parsed object from JSON.
     *
     * @param method      Request HTTP method
     * @param url         URL of the request to make
     * @param requestBody Request body
     * @param clazz       Relevant class object, for Gson's reflection
     * @param headers     Map of request headers
     */
    public GsonRequest(int method, String url, String requestBody, Class<T> clazz, Map<String,
            String> headers, Response.Listener<T> listener, Response.ErrorListener errorListener) {

        // Creates a new request with the given method (one of the values from {@link Method}),
        // URL, and error listener.  Note that the normal response listener is not provided here as
        // delivery of responses is provided by subclasses, who have a better idea of how to deliver
        // an already-parsed response.
        super(method, url, errorListener);

        this.requestBody = requestBody;
        this.clazz = clazz;
        this.headers = headers;
        this.listener = listener;
    }

    /**
     * Subclasses must implement this to parse the raw network response
     * and return an appropriate response type. This method will be
     * called from a worker thread.  The response will not be delivered
     * if you return null.
     *
     * @param response Response from the network
     * @return The parsed response, or null in the case of an error
     */
    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(
                    response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            return Response.success(
                    gson.fromJson(json, clazz),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        }
    }

    /**
     * Subclasses must implement this to perform delivery of the parsed
     * response to their listeners.  The given response is guaranteed to
     * be non-null; responses that fail to parse are not delivered.
     *
     * @param response The parsed response returned by
     *                 {@link #parseNetworkResponse(NetworkResponse)}
     */
    @Override
    protected void deliverResponse(T response) {
        Response.Listener<T> listener;
        synchronized (lock) {
            listener = this.listener;
        }
        if (listener != null) {
            listener.onResponse(response);
        }
    }

    /**
     * Mark this request as canceled.
     * <p>
     * <p>No callback will be delivered as long as either:
     * <ul>
     * <li>This method is called on the same thread as the {@link ResponseDelivery} is running
     * on. By default, this is the main thread.
     * <li>The request subclass being used overrides cancel() and ensures that it does not
     * invoke the listener in {@link #deliverResponse} after cancel() has been called in a
     * thread-safe manner.
     * </ul>
     * <p>
     * <p>There are no guarantees if both of these conditions aren't met.
     */
    @Override
    public void cancel() {
        super.cancel();
        synchronized (lock) {
            listener = null;
        }
    }

    /**
     * Returns a list of extra HTTP headers to go along with this request. Can
     * throw {@link AuthFailureError} as authentication may be required to
     * provide these values.
     *
     * @throws AuthFailureError In the event of auth failure
     */
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers != null ? headers : super.getHeaders();
    }

    /**
     * Returns the content type of the POST or PUT body.
     */
    @Override
    public String getBodyContentType() {
        return PROTOCOL_CONTENT_TYPE;
    }

    /**
     * Returns the raw POST or PUT body to be sent.
     * <p>
     * <p>By default, the body consists of the request parameters in
     * application/x-www-form-urlencoded format. When overriding this method, consider overriding
     * {@link #getBodyContentType()} as well to match the new body format.
     *
     * @throws AuthFailureError in the event of auth failure
     */
    @Override
    public byte[] getBody() throws AuthFailureError {
        try {
            return requestBody == null ? null : requestBody.getBytes(PROTOCOL_CHARSET);
        } catch (UnsupportedEncodingException uee) {
            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s",
                    requestBody, PROTOCOL_CHARSET);
            return null;
        }
    }
}
