package dtu.fm22.facade.exceptions;

import jakarta.ws.rs.*;
import messaging.implementations.RabbitMqResponse;

/**
 * @author s200718
 */
public class ExceptionFactory {
    public static WebApplicationException fromRabbitMqResponse(RabbitMqResponse<?> response) {
        return switch (response.getStatusCode()) {
            case 400 -> new BadRequestException(response.getErrorMessage());
            case 401 -> new NotAuthorizedException(response.getErrorMessage());
            case 403 -> new ForbiddenException(response.getErrorMessage());
            case 404 -> new NotFoundException(response.getErrorMessage());
            case 500 -> new InternalServerErrorException(response.getErrorMessage());
            default -> new WebApplicationException(response.getErrorMessage(), response.getStatusCode());
        };
    }
}
