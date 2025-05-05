package com.portfolio.backend.controller.order;

import com.portfolio.backend.common.security.UserDetailsImpl;
import com.portfolio.backend.service.order.OrderService;
import com.portfolio.backend.service.order.dto.OrderServiceRequest;
import com.portfolio.backend.service.order.dto.OrderServiceResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public void createOrder(@Valid @RequestBody OrderServiceRequest.Create request,
                            @AuthenticationPrincipal UserDetailsImpl userDetails){
        orderService.createOrder(userDetails.getId(), request);
    }

    @GetMapping("/page")
    @PreAuthorize("isAuthenticated()")
    public Page<OrderServiceResponse.Get> getPage(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                  @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return orderService.getPage(userDetails.getId(), pageable);
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public OrderServiceResponse.Get get(@PathVariable String orderId,
                                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return orderService.get(userDetails.getId(), orderId);
    }
}
