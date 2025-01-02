package data.network;

public interface ICallback {
    void onSuccess(Object result);
    void onError(String error);
    void onAuthFailure(String message);
}
