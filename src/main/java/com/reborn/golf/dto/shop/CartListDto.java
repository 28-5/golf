package com.reborn.golf.dto.shop;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartListDto {

    private Integer totalPrice;
    List<CartDto> cartDtos;
}
