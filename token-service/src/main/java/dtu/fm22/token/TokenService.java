package dtu.fm22.token;

import dtu.fm22.token.record.PaymentRequest;
import dtu.fm22.token.record.Token;
import dtu.fm22.token.record.TokenRequest;
import dtu.fm22.token.record.TokenValidationRequest;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import messaging.Event;
import messaging.MessageQueue;
import messaging.TopicNames;
import messaging.implementations.RabbitMqResponse;

/**
 * @author s242576
 */
public class TokenService {

    private static final int MAX_UNUSED_TOKENS = 6;
    private static final int MIN_TOKEN_REQUEST = 1;
    private static final int MAX_TOKEN_REQUEST = 5;
    private static final int TOKEN_LENGTH = 32;

    // Map: customerId -> List of tokens
    private final ConcurrentHashMap<UUID, List<Token>> customerTokens = new ConcurrentHashMap<>();

    // Map: tokenValue -> Token (for quick lookup)
    private final ConcurrentHashMap<String, Token> tokenLookup = new ConcurrentHashMap<>();

    private final MessageQueue queue;
    private final SecureRandom random = new SecureRandom();

    public TokenService(MessageQueue queue) {
        this.queue = queue;
        this.queue.addHandler(TopicNames.CUSTOMER_TOKEN_REPLENISH_REQUESTED, this::handleTokenReplenishRequested);
        this.queue.addHandler(TopicNames.CUSTOMER_TOKEN_REQUESTED, this::handleTokenRequested);
        this.queue.addHandler(TopicNames.TOKEN_VALIDATION_REQUESTED, this::handleTokenValidationRequested);
        this.queue.addHandler(TopicNames.PAYMENT_REQUESTED, this::handleTokenValidationRequested);
        this.queue.addHandler(TopicNames.TOKEN_MARK_USED_REQUESTED, this::handleTokenMarkUsedRequested);
    }

    /**
     * Generates a unique, non-guessable token
     *
     * @author s242576
     */
    private String generateToken() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder token = new StringBuilder(TOKEN_LENGTH);
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            token.append(characters.charAt(random.nextInt(characters.length())));
        }
        return token.toString();
    }

    /**
     * Handles token replenishment request (requesting new tokens)
     */

    /**
     * @author s242576
     */
    public void handleTokenReplenishRequested(Event event) {
        var tokenRequest = event.getArgument(0, TokenRequest.class);
        var correlationId = event.getArgument(1, UUID.class);

        try {
            var customerId = UUID.fromString(tokenRequest.customerId());
            var numberOfTokens = tokenRequest.numberOfTokens();

            // Validate request
            if (numberOfTokens < MIN_TOKEN_REQUEST || numberOfTokens > MAX_TOKEN_REQUEST) {
                var errorMessage = "Invalid number of tokens requested. Must be between "
                        + MIN_TOKEN_REQUEST + " and"
                        + " " + MAX_TOKEN_REQUEST;
                var errorResponse = new RabbitMqResponse<>(400, errorMessage);
                var errorEvent = new Event(TopicNames.CUSTOMER_TOKEN_REPLENISH_COMPLETED, errorResponse, correlationId);
                queue.publish(errorEvent);
                return;
            }

            var unusedTokens = getUnusedTokens(customerId);
            var unusedCount = unusedTokens.size();

            // Business rule: Can only request if:
            // 1. First request (no tokens exist)
            // 2. All tokens used (unusedCount == 0)
            // 3. Only one unused token left (unusedCount == 1)
            if (unusedCount > 1) {
                var errorMessage = "Cannot request tokens. Customer has "
                        + unusedCount
                        + " unused tokens. Can only "
                        + "request when having 0 or 1 unused token.";
                var errorResponse = new RabbitMqResponse<>(400, errorMessage);
                var errorEvent = new Event(TopicNames.CUSTOMER_TOKEN_REPLENISH_COMPLETED, errorResponse, correlationId);
                queue.publish(errorEvent);
                return;
            }

            // Check if adding tokens would exceed maximum
            if (unusedCount + numberOfTokens > MAX_UNUSED_TOKENS) {
                var errorMessage = "Cannot request "
                        + numberOfTokens
                        + " tokens. Would exceed maximum of "
                        + MAX_UNUSED_TOKENS
                        + " unused tokens.";
                var errorResponse = new RabbitMqResponse<>(400, errorMessage);
                var errorEvent = new Event(TopicNames.CUSTOMER_TOKEN_REPLENISH_COMPLETED, errorResponse, correlationId);
                queue.publish(errorEvent);
                return;
            }

            // Generate new tokens
            List<Token> newTokens = new ArrayList<>();
            for (int i = 0; i < numberOfTokens; i++) {
                String tokenValue;
                // Ensure uniqueness
                do {
                    tokenValue = generateToken();
                } while (tokenLookup.containsKey(tokenValue));

                var token = new Token(tokenValue, customerId, false);
                newTokens.add(token);
                tokenLookup.put(tokenValue, token);
            }

            // Add tokens to customer's token list
            customerTokens.compute(customerId, (key, existingTokens) -> {
                if (existingTokens == null) {
                    return new ArrayList<>(newTokens);
                } else {
                    existingTokens.addAll(newTokens);
                    return existingTokens;
                }
            });

            // Publish success event with list of token values
            List<String> tokenValues = newTokens.stream()
                    .map(Token::tokenValue)
                    .collect(Collectors.toList());

            var successEvent = new Event(
                    TopicNames.CUSTOMER_TOKEN_REPLENISH_COMPLETED,
                    new RabbitMqResponse<>(tokenValues),
                    correlationId
            );
            queue.publish(successEvent);

        } catch (IllegalArgumentException e) {
            var errorMessage = "Invalid customer ID format: " + tokenRequest.customerId();
            var errorResponse = new RabbitMqResponse<>(400, errorMessage);
            var errorEvent = new Event(TopicNames.CUSTOMER_TOKEN_REPLENISH_COMPLETED, errorResponse, correlationId);
            queue.publish(errorEvent);
        }
    }

    /**
     * Handles token request (get existing unused tokens)
     */


    /**
     * @author s242576
     */
    public void handleTokenRequested(Event event) {
        System.out.format("TokenService: Received CUSTOMER_TOKEN_REQUESTED event: %s%n", event);
        var customerIdStr = event.getArgument(0, String.class);
        var correlationId = event.getArgument(1, UUID.class);

        try {
            var customerId = UUID.fromString(customerIdStr);
            var unusedTokens = getUnusedTokens(customerId);

            List<String> tokenValues = unusedTokens.stream()
                    .map(Token::tokenValue)
                    .collect(Collectors.toList());

            var tokenProvidedEvent = new Event(
                    TopicNames.CUSTOMER_TOKEN_PROVIDED,
                    new RabbitMqResponse<>(tokenValues),
                    correlationId
            );
            queue.publish(tokenProvidedEvent);

        } catch (Exception e) {
            var errorResponse = new RabbitMqResponse<>(400, "Invalid customer ID format: " + customerIdStr);
            var errorEvent = new Event(TopicNames.CUSTOMER_TOKEN_PROVIDED, errorResponse, correlationId);
            queue.publish(errorEvent);
        }
    }

    /**
     * Handles token validation request (check if token is valid)
     * Returns the customerId associated with the token in the response
     *
     * @author s242576, s200718
     */
    public void handleTokenValidationRequested(Event event) {

        String tokenValue;
        switch (event.getTopic()) {
            case TopicNames.TOKEN_VALIDATION_REQUESTED:
                var tokenValidationRequest = event.getArgument(0, TokenValidationRequest.class);
                tokenValue = tokenValidationRequest.tokenValue();
                break;
            case TopicNames.PAYMENT_REQUESTED:
                var paymentRequest = event.getArgument(0, PaymentRequest.class);
                tokenValue = paymentRequest.token();
                break;
            default:
                // Unknown topic
                return;
        }

        var correlationId = event.getArgument(1, UUID.class);

        var token = tokenLookup.get(tokenValue);

        if (token == null) {
            // Token doesn't exist
            var errorResponse = new RabbitMqResponse<>(404, "Token not found");
            var errorEvent = new Event(TopicNames.TOKEN_VALIDATION_PROVIDED, errorResponse, correlationId);
            queue.publish(errorEvent);
            return;
        }

        if (token.used()) {
            // Token already used
            var errorResponse = new RabbitMqResponse<>(400, "Token has already been used");
            var errorEvent = new Event(TopicNames.TOKEN_VALIDATION_PROVIDED, errorResponse, correlationId);
            queue.publish(errorEvent);
            return;
        }

        // Token is valid - return the customerId associated with the token
        var successResponse = new RabbitMqResponse<>(token);
        var successEvent = new Event(TopicNames.TOKEN_VALIDATION_PROVIDED, successResponse, correlationId);
        queue.publish(successEvent);
    }

    /**
     * Handles request to mark a token as used
     *
     * @author s242576
     */
    public void handleTokenMarkUsedRequested(Event event) {
        var tokenValue = event.getArgument(0, String.class);
        var correlationId = event.getArgument(1, UUID.class);

        var token = tokenLookup.get(tokenValue);
        if (token == null) {
            var errorResponse = new RabbitMqResponse<>(404, "Token not found");
            var errorEvent = new Event(TopicNames.TOKEN_MARK_USED_COMPLETED, errorResponse, correlationId);
            queue.publish(errorEvent);
            return;
        }

        if (token.used()) {
            var errorResponse = new RabbitMqResponse<>(400, "Token already marked as used");
            var errorEvent = new Event(TopicNames.TOKEN_MARK_USED_COMPLETED, errorResponse, correlationId);
            queue.publish(errorEvent);
            return;
        }

        // Mark token as used
        var updatedToken = token.withUsed(true);
        tokenLookup.put(tokenValue, updatedToken);

        // Update in customer's token list
        var customerId = token.customerId();
        customerTokens.computeIfPresent(customerId, (key, tokens) -> {
            tokens.replaceAll(t -> t.tokenValue().equals(tokenValue) ? updatedToken : t);
            return tokens;
        });

        var successEvent = new Event(
                TopicNames.TOKEN_MARK_USED_COMPLETED,
                new RabbitMqResponse<>(true),
                correlationId
        );
        queue.publish(successEvent);
    }

    /**
     * Gets all unused tokens for a customer
     */
    private List<Token> getUnusedTokens(UUID customerId) {
        var tokens = customerTokens.get(customerId);
        if (tokens == null) {
            return Collections.emptyList();
        }
        return tokens.stream()
                .filter(token -> !token.used())
                .collect(Collectors.toList());
    }
}
