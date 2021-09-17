package com.reborn.golf.service;

import com.reborn.golf.dto.NoticeDto;
import com.reborn.golf.dto.PageRequestDto;
import com.reborn.golf.dto.PageResultDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.IntStream;

@SpringBootTest
public class NoticeServiceTests {

    @Autowired
    NoticeService noticeService;

    @Test
    public void registerTest() {
        IntStream.rangeClosed(1, 100).forEach(i -> {
            NoticeDto noticeDto = NoticeDto.builder()
                    .title("title.........." + i)
                    .content("content.........." + i)
                    .email("user" + ((i % 10) + 1) +"@naver.com")
                    .views(0)
                    .build();
            System.out.println(noticeDto);
        });

    }

    @Test
    public void getListTest(){
        PageRequestDto pageRequestDto = PageRequestDto.builder().page(1).size(10).build();
        var resultDto= noticeService.getList(pageRequestDto);
        for (var list: resultDto.getDtoList()) {
            System.out.println(list);
        }
    }

    @Test
    public void readTest(){
        System.out.println(noticeService.read(1L));
    }

    @Test
    public void removeTest(){
        noticeService.remove(1L);

    }
}
