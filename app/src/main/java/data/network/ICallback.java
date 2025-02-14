package data.network;

//a callback interface is used to handle the response, error and auth failure from the backend and volley
public interface ICallback {
    void onSuccess(Object result);
    void onError(String error);
    void onAuthFailure(String message);
}
