package com.reborn.golf.service;


import com.reborn.golf.dto.customerservice.NoticeDto;
import com.reborn.golf.dto.common.PageRequestDto;
import com.reborn.golf.dto.common.PageResultDto;
import com.reborn.golf.entity.Member;
import com.reborn.golf.entity.Notice;


public interface NoticeService {

    PageResultDto<Object[],NoticeDto> getList(PageRequestDto pageRequestDto);

    NoticeDto read(Long noticeIdx);

    Long register(Integer writerIdx, NoticeDto noticeDto);

    Long modify(Integer writerIdx, NoticeDto noticeDto);

    void remove(Integer memberIdx, Long noticeIdx);

    default NoticeDto entityToDto(Notice notice){
        return NoticeDto.builder()
                .idx(notice.getIdx())
                .title(notice.getTitle())
                .content(notice.getContent())
                .modDate(notice.getModDate())
                .regDate(notice.getRegDate())
                .views(notice.getViews())
                .email(notice.getWriter().getEmail())
                .name(notice.getWriter().getName())
                .build();
    }

    default NoticeDto entityToDto(Notice notice, Member member){
        return NoticeDto.builder()
                .idx(notice.getIdx())
                .title(notice.getTitle())
                .content(notice.getContent())
                .modDate(notice.getModDate())
                .regDate(notice.getRegDate())
                .views(notice.getViews())
                .email(member.getEmail())
                .name(member.getName())
                .build();
    }

    default Notice dtoToEntity(NoticeDto noticeDto, Integer writerIdx){
        return Notice.builder()
                .idx(noticeDto.getIdx())
                .title(noticeDto.getTitle())
                .content(noticeDto.getContent())
                .writer(Member.builder().idx(writerIdx).build())
                .writer(Member.builder().idx(writerIdx).build())
                .views(noticeDto.getViews())
                .build();
    }

}
