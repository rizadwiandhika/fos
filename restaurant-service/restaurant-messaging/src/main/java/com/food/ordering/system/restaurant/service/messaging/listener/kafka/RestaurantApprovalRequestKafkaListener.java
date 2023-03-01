package com.food.ordering.system.restaurant.service.messaging.listener.kafka;

import com.food.ordering.system.restaurant.service.domain.exception.RestaurantApplicationServiceException;
import com.food.ordering.system.restaurant.service.domain.exception.RestaurantNotFoundException;
import com.food.ordering.system.restaurant.service.domain.ports.input.message.listener.RestaurantApprovalMessageRequestListener;
import com.food.ordering.system.kafka.consumer.KafkaConsumer;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import com.food.ordering.system.restaurant.service.messaging.mapper.RestaurantMessagingDataMapper;
import lombok.extern.slf4j.Slf4j;

import org.postgresql.util.PSQLState;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;

@Slf4j
@Component
public class RestaurantApprovalRequestKafkaListener implements KafkaConsumer<RestaurantApprovalRequestAvroModel> {

    private final RestaurantApprovalMessageRequestListener restaurantApprovalMessageRequestListener;
    private final RestaurantMessagingDataMapper restaurantMessagingDataMapper;

    public RestaurantApprovalRequestKafkaListener(
            RestaurantApprovalMessageRequestListener restaurantApprovalRequestMessageListener,
            RestaurantMessagingDataMapper restaurantMessagingDataMapper) {
        this.restaurantApprovalMessageRequestListener = restaurantApprovalRequestMessageListener;
        this.restaurantMessagingDataMapper = restaurantMessagingDataMapper;
    }

    @Override
    @KafkaListener(id = "${kafka-consumer-config.restaurant-approval-consumer-group-id}", topics = "${restaurant-service.restaurant-approval-request-topic-name}")
    public void recieve(@Payload List<RestaurantApprovalRequestAvroModel> messages,
            @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
            @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
        log.info("{} number of orders approval requests received with keys {}, partitions {} and offsets {}" +
                ", sending for restaurant approval",
                messages.size(),
                keys.toString(),
                partitions.toString(),
                offsets.toString());

        messages.forEach(restaurantApprovalRequestAvroModel -> {
            try {
                restaurantApprovalMessageRequestListener.approveOrder(restaurantMessagingDataMapper
                        .restaurantApprovalRequestAvroModelToRestaurantApproval(
                                restaurantApprovalRequestAvroModel));
            } catch (DataAccessException e) {
                SQLException sqlException = (SQLException) e.getRootCause();
                if (sqlException != null && sqlException.getSQLState() != null
                        && PSQLState.UNIQUE_VIOLATION.getState()
                                .equals(sqlException.getSQLState())) {
                    log.error(
                            "Caught UNIQUE_VIOLATION error with SQL state: {}, in RestaurantApprovalRequestKafkaListener for order id: {}",
                            sqlException.getSQLState(),
                            restaurantApprovalRequestAvroModel.getOrderId());
                    return;
                }

                // Throw to make this recieve method re-read again the message in kafka
                throw new RestaurantApplicationServiceException(
                        "Throwing DataAccessException in RestaurantApprovalRequestKafkaListener. Message: "
                                + e.getMessage(),
                        e);

            } catch (RestaurantNotFoundException e) {
                log.error("Restaurant not found with id: {}. order id: {}",
                        restaurantApprovalRequestAvroModel.getRestaurantId(),
                        restaurantApprovalRequestAvroModel.getOrderId());
            }
        });

        log.info("Finished processing");
    }

}
