package com.reborn.golf.service;


import com.reborn.golf.dto.common.PageRequestDto;
import com.reborn.golf.dto.common.PageResultDto;
import com.reborn.golf.dto.customerservice.QnaDto;
import com.reborn.golf.entity.Member;
import com.reborn.golf.entity.Qna;


public interface QnaService {

    PageResultDto<Object[], QnaDto> getList(PageRequestDto pageRequestDto);

    QnaDto read(Long qnaIdx);

    Long register(Integer writerIdx, QnaDto qnaDto);

    Long modify(Integer writerIdx, QnaDto qnaDto);

    Long remove(Integer memberIdx, Long qnaIdx);

    default QnaDto entityToDto(Qna qna){
        return QnaDto.builder()
                .idx(qna.getIdx())
                .title(qna.getTitle())
                .content(qna.getContent())
                .modDate(qna.getModDate())
                .regDate(qna.getRegDate())
                .views(qna.getViews())
                .email(qna.getWriter().getEmail())
                .name(qna.getWriter().getName())
                .build();
    }

    default QnaDto entityToDto(Qna qna, Member member){
        Long pidx = null;
        if(qna.getParent() != null)
            pidx = qna.getParent().getIdx();

        return QnaDto.builder()
                .idx(qna.getIdx())
                .title(qna.getTitle())
                .content(qna.getContent())
                .modDate(qna.getModDate())
                .regDate(qna.getRegDate())
                .views(qna.getViews())
                .email(member.getEmail())
                .name(member.getName())
                .pidx(pidx)
                .build();
    }

    default Qna dtoToEntity(QnaDto qnaDto, Integer writerIdx){
        return Qna.builder()
                .idx(qnaDto.getIdx())
                .title(qnaDto.getTitle())
                .content(qnaDto.getContent())
                .writer(Member.builder().idx(writerIdx).build())
                .writer(Member.builder().idx(writerIdx).build())
                .views(qnaDto.getViews())
                .build();
    }

}
