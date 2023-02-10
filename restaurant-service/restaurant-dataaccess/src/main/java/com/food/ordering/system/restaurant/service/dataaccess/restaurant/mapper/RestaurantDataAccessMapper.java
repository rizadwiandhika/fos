package com.food.ordering.system.restaurant.service.dataaccess.restaurant.mapper;

import com.food.ordering.system.dataaccess.restaurant.entity.RestaurantEntity;
import com.food.ordering.system.dataaccess.restaurant.exception.RestaurantDataAccessException;
import com.food.ordering.system.domain.valueObject.Money;
import com.food.ordering.system.domain.valueObject.OrderId;
import com.food.ordering.system.domain.valueObject.ProductId;
import com.food.ordering.system.domain.valueObject.RestaurantId;
import com.food.ordering.system.restaurant.service.dataaccess.restaurant.entity.OrderApprovalEntity;
import com.food.ordering.system.restaurant.service.domain.entity.OrderApproval;
import com.food.ordering.system.restaurant.service.domain.entity.OrderDetail;
import com.food.ordering.system.restaurant.service.domain.entity.Product;
import com.food.ordering.system.restaurant.service.domain.entity.Restaurant;
import com.food.ordering.system.restaurant.service.domain.valueobject.OrderApprovalId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RestaurantDataAccessMapper {

        public List<UUID> restaurantToRestaurantProducts(Restaurant restaurant) {
                return restaurant.getOrderDetail().getProducts().stream()
                                .map(product -> product.getId().getValue())
                                .collect(Collectors.toList());
        }

        public Restaurant restaurantEntityToRestaurant(List<RestaurantEntity> restaurantEntities) {
                RestaurantEntity restaurantEntity = restaurantEntities.stream().findFirst()
                                .orElseThrow(() -> new RestaurantDataAccessException("No restaurants found!"));

                List<Product> restaurantProducts = restaurantEntities.stream().map(entity -> Product.builder()
                                .setProductId(new ProductId(entity.getProductId()))
                                .setName(entity.getProductName())
                                .setPrice(new Money(entity.getProductPrice()))
                                .setAvailable(entity.getProductAvailable())
                                .build())
                                .collect(Collectors.toList());

                return Restaurant.builder()
                                .setRestaurantId(new RestaurantId(restaurantEntity.getRestaurantId()))
                                .setOrderDetail(OrderDetail.builder()
                                                .setProducts(restaurantProducts)
                                                .build())
                                .setActive(restaurantEntity.getRestaurantActive())
                                .build();
        }

        public OrderApprovalEntity orderApprovalToOrderApprovalEntity(OrderApproval orderApproval) {
                return OrderApprovalEntity.builder()
                                .id(orderApproval.getId().getValue())
                                .restaurantId(orderApproval.getRestaurantId().getValue())
                                .orderId(orderApproval.getOrderId().getValue())
                                .status(orderApproval.getOrderApprovalStatus())
                                .build();
        }

        public OrderApproval orderApprovalEntityToOrderApproval(OrderApprovalEntity orderApprovalEntity) {
                return OrderApproval.builder()
                                .setOrderApprovalId(new OrderApprovalId(orderApprovalEntity.getId()))
                                .setRestaurantId(new RestaurantId(orderApprovalEntity.getRestaurantId()))
                                .setOrderId(new OrderId(orderApprovalEntity.getOrderId()))
                                .setOrderApprovalStatus(orderApprovalEntity.getStatus())
                                .build();
        }

}
