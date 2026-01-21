package messaging.implementations;

/**
 * @author s200718
 */
public class RabbitMQResponse<T> {

    private final boolean success;
    private final int statusCode;
    private final String errorMessage;

    private final T data;

    public RabbitMQResponse(String errorMessage) {
        this.success = false;
        this.errorMessage = errorMessage;
        this.statusCode = 500;
        this.data = null;
    }
    public RabbitMQResponse(int statusCode, String errorMessage) {
        this.success = false;
        this.errorMessage = errorMessage;
        this.statusCode = statusCode;
        this.data = null;
    }

    public RabbitMQResponse(T data) {
        this.success = true;
        this.statusCode = 200;
        this.errorMessage = null;
        this.data = data;
    }

    public boolean isError() {
        return !success;
    }

    public int statusCode() {
        return statusCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public T getData() {
        return data;
    }
}
