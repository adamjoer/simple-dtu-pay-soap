package dtu.fm22.facade.exceptions;

import jakarta.ws.rs.*;
import messaging.implementations.RabbitMQResponse;

/**
 * @author s200718
 */
public class ExceptionFactory {
    public static WebApplicationException fromRabbitMqResponse(RabbitMQResponse<?> response) {
        return switch (response.statusCode()) {
            case 400 -> new BadRequestException(response.getErrorMessage());
            case 401 -> new NotAuthorizedException(response.getErrorMessage());
            case 403 -> new ForbiddenException(response.getErrorMessage());
            case 404 -> new NotFoundException(response.getErrorMessage());
            case 500 -> new InternalServerErrorException(response.getErrorMessage());
            default -> new WebApplicationException(response.getErrorMessage(), response.statusCode());
        };
    }
}
