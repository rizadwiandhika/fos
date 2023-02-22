package com.food.ordering.system.order.service.dataaccess.outbox.restaurantapproval.adapter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.food.ordering.system.order.service.dataaccess.outbox.restaurantapproval.exception.ApprovalOutboxNotFoundException;
import com.food.ordering.system.order.service.dataaccess.outbox.restaurantapproval.mapper.ApprovalOutboxDataMapper;
import com.food.ordering.system.order.service.dataaccess.outbox.restaurantapproval.repository.ApprovalOutboxJpaRepository;
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.output.repository.ApprovalOutboxRepository;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;

@Component
public class ApprovalOutboxRepositoryImpl implements ApprovalOutboxRepository {

	private final ApprovalOutboxJpaRepository jpaRepo;
	private final ApprovalOutboxDataMapper mapper;

	public ApprovalOutboxRepositoryImpl(ApprovalOutboxJpaRepository approvalOutboxJpaRepository,
			ApprovalOutboxDataMapper approvalOutboxDataMapper) {
		this.jpaRepo = approvalOutboxJpaRepository;
		this.mapper = approvalOutboxDataMapper;
	}

	@Override
	public void deleteByTypeAndOutboxStatusAndSagaStatus(String type, OutboxStatus outboxStatus,
			SagaStatus... sagaStatus) {
		jpaRepo.deleteByTypeAndOutboxStatusAndSagaStatusIn(type, outboxStatus, Arrays.asList(sagaStatus));
	}

	@Override
	public Optional<List<OrderApprovalOutboxMessage>> findByTypeAndOutboxStatusAndSagaStatus(String type,
			OutboxStatus outboxStatus, SagaStatus... sagaStatus) {
		List<SagaStatus> statusList = Arrays.asList(sagaStatus);
		String err = String.format("Approval outbox not found for type: %s, outboxStatus: %s, and sagaStatus: %s", type,
				outboxStatus.name(),
				String.join(",", statusList.stream().map((s) -> s.toString()).collect(Collectors.toList())));

		return Optional.of(jpaRepo.findByTypeAndOutboxStatusAndSagaStatusIn(type, outboxStatus, statusList)
				.orElseThrow(() -> new ApprovalOutboxNotFoundException(err))
				.stream()
				.map(mapper::outboxEntityToOrderCreateOutboxMessage)
				.collect(Collectors.toList()));
	}

	@Override
	public Optional<OrderApprovalOutboxMessage> findByTypeAndSagaIdAndSagaStatus(String type, UUID sagaId,
			SagaStatus... sagaStatus) {
		List<SagaStatus> statusList = Arrays.asList(sagaStatus);
		return jpaRepo.findByTypeAndSagaIdAndSagaStatusIn(type, sagaId, statusList)
				.map(mapper::outboxEntityToOrderCreateOutboxMessage);
	}

	@Override
	public OrderApprovalOutboxMessage save(OrderApprovalOutboxMessage orderApprovalOutboxMessage) {
		return mapper.outboxEntityToOrderCreateOutboxMessage(
				jpaRepo.save(mapper.orderCreateOutboxMessageToOutboxEntity(orderApprovalOutboxMessage)));
	}

}
