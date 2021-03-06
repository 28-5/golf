package com.reborn.golf.repository;

import com.reborn.golf.entity.Enum.OrderStatus;
import com.reborn.golf.entity.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface OrderRepository extends JpaRepository<Orders, Long> {
    Page<Orders> getOrdersByMemberIdxAndRemovedFalse(Integer memberIdx, Pageable pageable);
    Page<Orders> getOrdersByOrderStateAndRemovedFalse(OrderStatus orderStatus, Pageable pageable);
//
//    Orders getOrdersByPartnerOrderId(String partnerOrderId);
//
//    void deleteByPartnerOrderId(String getOrdersByPartnerOrderId);
}
