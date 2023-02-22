package com.food.ordering.system.order.service.application.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.food.ordering.system.handler.ErrorDTO;
import com.food.ordering.system.handler.GlobalExceptionHandler;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.exception.OrderNotFoundException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class OrderGlobalExceptionHandler extends GlobalExceptionHandler {

	@ResponseBody // Return of this method will be written to the response body
	@ExceptionHandler(value = { OrderDomainException.class }) // This methode will be called when OrderDomainException is
																														// thrown
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorDTO handleException(OrderDomainException orderDomainException) {
		log.error(orderDomainException.getMessage());

		return ErrorDTO.builder()
				.code(HttpStatus.BAD_REQUEST.getReasonPhrase())
				.message(orderDomainException.getMessage())
				.build();
	}

	@ResponseBody // Return of this method will be written to the response body
	@ExceptionHandler(value = { OrderNotFoundException.class }) // This methode will be called when OrderNotFoundException
																															// is thrown
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorDTO handleException(OrderNotFoundException orderDomainException) {
		log.error(orderDomainException.getMessage());

		return ErrorDTO.builder()
				.code(HttpStatus.NOT_FOUND.getReasonPhrase())
				.message(orderDomainException.getMessage())
				.build();
	}

}
