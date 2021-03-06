package com.reborn.golf.dto.shop;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {

    private Long idx;

    private Integer categoryIdx;

    private String title;

    private String brand;

    private Integer quantity;

    private Integer price;

    private String content;

    private Integer salesVolume;

    // 이미지 생성
    private List<ProductImageDto> imageDtoList;

    private Double avg;

    private Integer reviewCnt;

    private LocalDateTime regDate;

    private LocalDateTime modDate;

}
