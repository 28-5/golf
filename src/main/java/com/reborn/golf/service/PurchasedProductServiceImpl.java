package com.reborn.golf.service;

import com.reborn.golf.api.CoinExchange;
import com.reborn.golf.api.ContractService;
import com.reborn.golf.dto.common.PageRequestDto;
import com.reborn.golf.dto.common.PageResultDto;
import com.reborn.golf.dto.exception.AlreadyFinishedException;
import com.reborn.golf.dto.exception.NotExistEntityException;
import com.reborn.golf.dto.exception.TokenTransactionException;
import com.reborn.golf.dto.exception.WrongStepException;
import com.reborn.golf.dto.shop.ProductDto;
import com.reborn.golf.dto.shop.ProductImageDto;
import com.reborn.golf.dto.shop.PurchasedProductDto;
import com.reborn.golf.entity.*;
import com.reborn.golf.entity.Enum.PurchasedProductStep;
import com.reborn.golf.entity.Enum.Role;
import com.reborn.golf.repository.CategoryRepository;
import com.reborn.golf.repository.MemberRepository;
import com.reborn.golf.repository.PurchasedProductImageRepository;
import com.reborn.golf.repository.PurchasedProductRepository;
import jnr.a64asm.Mem;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.protocol.exceptions.TransactionException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Log4j2
@RequiredArgsConstructor
@Service
public class PurchasedProductServiceImpl implements PurchasedProductService {
    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;
    private final PurchasedProductRepository purchasedProductRepository;
    private final PurchasedProductImageRepository purchasedProductImageRepository;
    private final CoinExchange coinExchange;
    private final Double tokenAmountRatePerCost = 0.1;
    private final ContractService contractService;

    @Override
    @Transactional
    public PageResultDto<Object[], PurchasedProductDto> getListWithUser(Integer memberIdx, PageRequestDto requestDto) {
        Page<Object[]> result = purchasedProductRepository.getPurchasedItemsbyMemberIdx(memberIdx, requestDto.getPageable(Sort.by("regDate").descending()));

//        Function<Object[], PurchasedProductDto> fn = (arr -> entitiesToDto((PurchasedProduct) arr[0], List.of((PurchasedProductImage) arr[1]), (String) arr[2]));
        Function<Object[], PurchasedProductDto> fn = (arr -> {
            PurchasedProduct items = (PurchasedProduct) arr[0];
            List<PurchasedProductImage> purchasedProductImageList = List.of((PurchasedProductImage) arr[1]);
            String categoryName = (String) arr[2];
            //????????? ??????
            List<ProductImageDto> productImageDtoList = purchasedProductImageList.stream().map(productImage ->
                    ProductImageDto.builder()
                            .imgName(productImage.getImgName())
                            .path(productImage.getPath())
                            .uuid(productImage.getUuid())
                            .build()).collect(Collectors.toList());
            //????????????????????? ??????
            Integer expectedPrice = items.getPrice();
            //????????????????????? ?????? ??????
            Long expectedPointAmount = (long) (items.getPrice() * tokenAmountRatePerCost / coinExchange.getTokenPrice());
            //?????? ????????? ??????
            Integer possiblePrice = items.getPossiblePrice();
            //?????? ????????? ?????? ??????
            Long possiblePointAmount = (long) (items.getPossiblePrice() * tokenAmountRatePerCost / coinExchange.getTokenPrice());


            return PurchasedProductDto.builder()
                    .idx(items.getIdx())
                    .catagory(categoryName)
                    .brand(items.getBrand())
                    .name(items.getName())
                    .state(items.getState())
                    .price(items.getPrice())
                    .quantity(items.getQuantity())
                    .address(items.getAddress())
                    .details(items.getDetails())
                    .expectedPrice(expectedPrice)
                    .expectedPointAmount(expectedPointAmount)
                    .proposalPrice(possiblePrice)
                    .proposalTokenAmount(possiblePointAmount)
                    .acceptedPrice(items.getAcceptedPrice())
                    .acceptedTokenPrice(items.getAcceptedTokenPrice())
                    .acceptedTokenAmount(items.getAcceptedTokenAmount())
                    .canceled(items.isCanceled())
                    .step(items.getPurchasedProductStep().name())
                    .imageDtoList(productImageDtoList)
                    .regDate(items.getRegDate())
                    .modDate(items.getModDate())
                    .build();
        });

        return new PageResultDto<>(result, fn);
    }

    @Override
    public PurchasedProductDto read(Long idx) {
        try {
            List<Object[]> result = purchasedProductRepository.getItembyIdxWithImage(idx);
            //?????? ?????? ??????
            PurchasedProduct purchasedProduct = (PurchasedProduct) result.get(0)[0];
            //?????? ?????? ?????????
            List<PurchasedProductImage> productImageList = new ArrayList<>();
            result.forEach(arr -> {
                PurchasedProductImage purchasedProductImage = (PurchasedProductImage) arr[1];
                productImageList.add(purchasedProductImage);
            });
            //?????????
            Member member = (Member) result.get(0)[2];

            PurchasedProductDto purchasedProductDto = entitiesToDto(purchasedProduct, productImageList, member);
            //????????????????????? ??????
            Integer expectedPrice = purchasedProduct.getPrice();
            //????????????????????? ?????? ??????
            Long expectedPointAmount = (long) (purchasedProduct.getPrice() * tokenAmountRatePerCost / coinExchange.getTokenPrice());
            //?????? ????????? ??????
            Integer possiblePrice = purchasedProduct.getPossiblePrice();
            //?????? ????????? ?????? ??????
            Long possiblePointAmount = (long) (purchasedProduct.getPossiblePrice() * tokenAmountRatePerCost / coinExchange.getTokenPrice());

            purchasedProductDto.setExpectedPrice(expectedPrice);
            purchasedProductDto.setExpectedPointAmount(expectedPointAmount);
            purchasedProductDto.setProposalPrice(possiblePrice);
            purchasedProductDto.setProposalTokenAmount(possiblePointAmount);

            return purchasedProductDto;
        } catch (IndexOutOfBoundsException e) {
            log.info(e.getMessage());
            throw new NotExistEntityException("IDX??? ?????? ?????? DB????????? ????????????");
        }
    }

    @Override
    @Transactional
    public PurchasedProductDto register(Integer memberIdx, Integer categoryIdx, PurchasedProductDto purchasedProductDto) {
        Category category = categoryRepository.findById(categoryIdx)
                .orElseThrow(() -> new NotExistEntityException("???????????? ??????????????? ????????????"));
        Member member = memberRepository.getMemberByIdxAndRemovedFalse(memberIdx)
                .orElseThrow(() -> new NotExistEntityException("???????????? ??????????????? ????????????"));

        Map<String, Object> entityMap = dtoToEntities(member, category, purchasedProductDto);

        PurchasedProduct purchasedProduct = (PurchasedProduct) entityMap.get("purchasedProduct");
        purchasedProduct.setStep(PurchasedProductStep.RESERVATION);

        List<PurchasedProductImage> imgList = (List<PurchasedProductImage>) entityMap.get("imgList");

        purchasedProductRepository.save(purchasedProduct);

        imgList.forEach(img -> {
            purchasedProductImageRepository.save(img);
        });
        //????????????????????? ??????
        Integer expectedPrice = purchasedProduct.getPrice();
        //????????????????????? ?????? ??????
        Long expectedPointAmount = (long) (purchasedProduct.getPrice() * tokenAmountRatePerCost / coinExchange.getTokenPrice());

        //Response??? ?????? ????????? ??????
        purchasedProductDto.setIdx(purchasedProduct.getIdx());
        purchasedProductDto.setCatagory(category.getName());
        purchasedProductDto.setMemberEmail(member.getEmail());
        purchasedProductDto.setMemberName(member.getName());
        purchasedProductDto.setStep(purchasedProduct.getPurchasedProductStep().name());
        purchasedProductDto.setRegDate(purchasedProduct.getRegDate());
        purchasedProductDto.setModDate(purchasedProduct.getModDate());
        purchasedProductDto.setExpectedPrice(expectedPrice);
        purchasedProductDto.setExpectedPointAmount(expectedPointAmount);

        return purchasedProductDto;

    }

    @Override
    @Transactional
    public Long modify(Integer memberIdx, Integer categoryIdx, PurchasedProductDto purchasedProductDto) {
        PurchasedProduct purchasedProduct = purchasedProductRepository.getPurchasedItembyIdxAndMemberIdx(memberIdx, purchasedProductDto.getIdx())
                .orElseThrow(() -> new NotExistEntityException("IDX??? ???????????? DB????????? ????????????"));

        //PurchasedProduct ?????? ??????
        purchasedProduct.changeCatagory(categoryIdx);
        purchasedProduct.changeBrand(purchasedProductDto.getBrand());
        purchasedProduct.changeName(purchasedProductDto.getName());
        purchasedProduct.changeQuantity(purchasedProductDto.getQuantity());
        purchasedProduct.changeState(purchasedProductDto.getState());
        purchasedProduct.changePrice(purchasedProductDto.getPrice());
        purchasedProduct.changeDetails(purchasedProductDto.getDetails());
        purchasedProduct.changeAddress(purchasedProductDto.getAddress());
        purchasedProduct.setStep(PurchasedProductStep.PROPOSAL);
        purchasedProductRepository.save(purchasedProduct);

        //PurchasedProduct??? ????????? ???????????? ???????????? ?????? ????????? ?????????, ????????? ????????? ??????
        List<ProductImageDto> imageDtoList = purchasedProductDto.getImageDtoList();
        if (imageDtoList != null && imageDtoList.size() > 0) {
            purchasedProductImageRepository.deleteAllByPurchasedProductIdx(purchasedProduct.getIdx());

            List<PurchasedProductImage> purchasedProductImageList = imageDtoList.stream().map(imgDto -> PurchasedProductImage.builder()
                    .path(imgDto.getPath())
                    .imgName(imgDto.getImgName())
                    .uuid(imgDto.getUuid())
                    .purchasedProduct(purchasedProduct)
                    .build()
            ).collect(Collectors.toList());

            purchasedProductImageList.forEach(img -> {
                purchasedProductImageRepository.save(img);
            });
        }
        return purchasedProduct.getIdx();
    }

    @Override
    public void remove(Integer memberIdx, Long idx) {
        PurchasedProduct purchasedProduct = purchasedProductRepository.getPurchasedItembyIdxAndMemberIdx(memberIdx, idx)
                .orElseThrow(() -> new NotExistEntityException("IDX??? ???????????? DB????????? ????????????"));

        if (purchasedProduct.getPurchasedProductStep().ordinal() == PurchasedProductStep.FINISH.ordinal()) {
            throw new AlreadyFinishedException("?????? ?????? ????????? ???????????????");
        }

        purchasedProduct.setStep(PurchasedProductStep.CANCELED);

        if (purchasedProduct.getPurchasedProductStep().ordinal() == PurchasedProductStep.ACCEPTANCE.ordinal()) {
            try {
                contractService.transferFrom(purchasedProduct.getMember().getWallet().getAddress(), purchasedProduct.getAcceptedTokenAmount() * 1000L);
            } catch (IOException | ClassNotFoundException | InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException | TransactionException e) {
                log.debug(e.getMessage());
                throw new TokenTransactionException("?????? ???????????? ?????? ??????");
            }
        }

        purchasedProduct.changeRemoved(true);
        purchasedProductRepository.save(purchasedProduct);
    }

    @Override
    public Map<String, Object> modifyStep(Long purchasedProductIdx, Set<String> roleSet, Integer cost) {
        PurchasedProduct purchasedProduct = purchasedProductRepository.getPurchasedProductByIdxAndCanceledFalse(purchasedProductIdx)
                .orElseThrow(() -> new NotExistEntityException("IDX??? ???????????? DB????????? ????????????"));

        Map<String, Object> map = new HashMap<>();

        if (purchasedProduct.getPurchasedProductStep().ordinal() == PurchasedProductStep.RESERVATION.ordinal()) { //PROPOSAL steop?????? ????????????

            if (!roleSet.contains(Role.ROLE_MANAGER.name())) {
                throw new WrongStepException("????????? ????????????");
            } else if (cost == null) {
                throw new WrongStepException("Cost is NULL");
            }
            //????????? ????????? ????????????
            purchasedProduct.setStep(PurchasedProductStep.PROPOSAL);
            purchasedProduct.setPossiblePrice(cost);
            //?????? ????????? ??????
            Integer possiblePrice = (int) (cost * (1 - tokenAmountRatePerCost));
            //?????? ????????? ?????? ??????
            Long possiblePointAmount = (long) (cost * tokenAmountRatePerCost / coinExchange.getTokenPrice());
            map.put("proposalPrice", possiblePrice);
            map.put("proposalTokenAmount", possiblePointAmount);

            //????????? ????????? ???????????? ???????????? ????????????
        } else if (purchasedProduct.getPurchasedProductStep().ordinal() == PurchasedProductStep.PROPOSAL.ordinal()) { //ACCEPTANCE step?????? ????????????

            if (!roleSet.contains(Role.ROLE_USER.name())) {
                throw new WrongStepException("????????? ????????????");
            }
            purchasedProduct.setStep(PurchasedProductStep.ACCEPTANCE);
            //?????? ????????? ??????
            Integer possiblePrice = purchasedProduct.getPossiblePrice();
            //?????? ????????? ?????? ??????
            Long possiblePointAmount = (long) (purchasedProduct.getPossiblePrice() * tokenAmountRatePerCost / coinExchange.getTokenPrice());

            try {
                contractService.transfer(purchasedProduct.getMember().getWallet().getAddress(), possiblePointAmount * 1000L);
            } catch (IOException | ClassNotFoundException | InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException | TransactionException e) {
                log.debug(e.getMessage());
                throw new TokenTransactionException("?????? ???????????? ?????? ??????");
            }

            purchasedProduct.setAcceptedPrice(possiblePrice);
            purchasedProduct.setAcceptedTokenAmount(possiblePointAmount);
            purchasedProduct.setAcceptedTokenPrice(possiblePrice.toString());
            map.put("proposalPrice", possiblePrice);
            map.put("proposalTokenAmount", possiblePointAmount);

            //????????? ?????? ??? Finish????????? ??????
        } else if (purchasedProduct.getPurchasedProductStep().ordinal() == PurchasedProductStep.ACCEPTANCE.ordinal()) {

            if (!roleSet.contains(Role.ROLE_MANAGER.name())) {
                throw new WrongStepException("????????? ????????????");
            }
            purchasedProduct.setStep(PurchasedProductStep.FINISH);
        } else {
            throw new WrongStepException("FINISH ???????????? ??????");
        }

        purchasedProductRepository.save(purchasedProduct);
        map.put("step", purchasedProduct.getPurchasedProductStep().name());
        return map;
    }

    @Override
    public PageResultDto<Object[], PurchasedProductDto> getList(PageRequestDto requestDto) {
        Page<Object[]> result = purchasedProductRepository.getPurchasedProductByIdx(requestDto.getPageable(Sort.by("regDate").descending()));

//        Function<Object[], PurchasedProductDto> fn = (arr -> entitiesToDto((PurchasedProduct) arr[0], List.of((PurchasedProductImage) arr[1]), (String) arr[2]));
        Function<Object[], PurchasedProductDto> fn = (arr -> {
            PurchasedProduct items = (PurchasedProduct) arr[0];
            List<PurchasedProductImage> purchasedProductImageList = List.of((PurchasedProductImage) arr[1]);
            Member member = (Member) arr[2];
            String categoryName = (String) arr[3];
            //????????? ??????
            List<ProductImageDto> productImageDtoList = purchasedProductImageList.stream().map(productImage ->
                    ProductImageDto.builder()
                            .imgName(productImage.getImgName())
                            .path(productImage.getPath())
                            .uuid(productImage.getUuid())
                            .build()).collect(Collectors.toList());

            //????????????????????? ??????
            Integer expectedPrice = items.getPrice();
            //????????????????????? ?????? ??????
            Long expectedPointAmount = (long) (items.getPrice() * tokenAmountRatePerCost / coinExchange.getTokenPrice());
            //?????? ????????? ??????
            Integer possiblePrice = items.getPossiblePrice();
            //?????? ????????? ?????? ??????
            Long possiblePointAmount = (long) (items.getPossiblePrice() * tokenAmountRatePerCost / coinExchange.getTokenPrice());


            return PurchasedProductDto.builder()
                    .idx(items.getIdx())
                    .catagory(categoryName)
                    .brand(items.getBrand())
                    .name(items.getName())
                    .state(items.getState())
                    .price(items.getPrice())
                    .quantity(items.getQuantity())
                    .address(items.getAddress())
                    .details(items.getDetails())
                    .memberEmail(member.getEmail())
                    .memberName(member.getName())
                    .expectedPrice(expectedPrice)
                    .expectedPointAmount(expectedPointAmount)
                    .proposalPrice(possiblePrice)
                    .proposalTokenAmount(possiblePointAmount)
                    .canceled(items.isCanceled())
                    .step(items.getPurchasedProductStep().name())
                    .imageDtoList(productImageDtoList)
                    .regDate(items.getRegDate())
                    .modDate(items.getModDate())
                    .build();
        });

        return new PageResultDto<>(result, fn);
    }
}
