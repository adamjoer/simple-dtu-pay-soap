package messaging.implementations;

/**
 * @author s200718
 */
public class RabbitMqResponse<T> {

    private final boolean success;
    private final int statusCode;
    private final String errorMessage;

    private final T data;

    public RabbitMqResponse(int statusCode, String errorMessage) {
        this.success = false;
        this.errorMessage = errorMessage;
        this.statusCode = statusCode;
        this.data = null;
    }

    public RabbitMqResponse(T data) {
        this.success = true;
        this.statusCode = 200;
        this.errorMessage = null;
        this.data = data;
    }

    public boolean isError() {
        return !success;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public T getData() {
        return data;
    }
}
