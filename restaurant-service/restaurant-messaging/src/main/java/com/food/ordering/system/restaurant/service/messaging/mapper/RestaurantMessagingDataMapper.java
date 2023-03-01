package com.food.ordering.system.restaurant.service.messaging.mapper;

import com.food.ordering.system.domain.valueObject.ProductId;
import com.food.ordering.system.domain.valueObject.RestaurantOrderStatus;
import com.food.ordering.system.kafka.order.avro.model.OrderApprovalStatus;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalResponseAvroModel;
import com.food.ordering.system.restaurant.service.domain.dto.RestaurantApprovalRequest;
import com.food.ordering.system.restaurant.service.domain.entity.Product;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderEventPayload;

import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RestaurantMessagingDataMapper {
        public RestaurantApprovalResponseAvroModel orderEventPayloadToRestaurantApprovalResponseAvroModel(String sagaId,
                        OrderEventPayload payload) {
                return RestaurantApprovalResponseAvroModel.newBuilder()
                                .setId(UUID.randomUUID().toString())
                                .setSagaId(sagaId)
                                .setRestaurantId(payload.getRestaurantId())
                                .setOrderId(payload.getOrderId())
                                .setCreatedAt(payload.getCreatedAt().toInstant())
                                .setOrderApprovalStatus(OrderApprovalStatus.valueOf(payload.getOrderApprovalStatus()))
                                .setFailureMessages(payload.getFailureMessages())
                                .build();
        }

        public RestaurantApprovalRequest restaurantApprovalRequestAvroModelToRestaurantApproval(
                        RestaurantApprovalRequestAvroModel restaurantApprovalRequestAvroModel) {
                return RestaurantApprovalRequest.builder()
                                .id(restaurantApprovalRequestAvroModel.getId())
                                .sagaId(restaurantApprovalRequestAvroModel.getSagaId())
                                .restaurantId(restaurantApprovalRequestAvroModel.getRestaurantId())
                                .orderId(restaurantApprovalRequestAvroModel.getOrderId())
                                .restaurantOrderStatus(RestaurantOrderStatus.valueOf(restaurantApprovalRequestAvroModel
                                                .getRestaurantOrderStatus().name()))
                                .products(restaurantApprovalRequestAvroModel.getProducts()
                                                .stream().map(avroModel -> Product.builder()
                                                                .setProductId(new ProductId(
                                                                                UUID.fromString(avroModel.getId())))
                                                                .setQuantity(avroModel.getQuantity())
                                                                .build())
                                                .collect(Collectors.toList()))
                                .price(restaurantApprovalRequestAvroModel.getPrice())
                                .createdAt(restaurantApprovalRequestAvroModel.getCreatedAt())
                                .build();
        }
}
