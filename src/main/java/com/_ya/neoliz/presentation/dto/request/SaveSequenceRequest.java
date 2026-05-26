package com._ya.neoliz.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class SaveSequenceRequest {

    @NotBlank
    @Size(min = 1, max = 20)
    private String title;

    @NotNull
    @Size(min = 4, max = 4)
    private List<ItemRequest> items;

    @Getter
    public static class ItemRequest {
        @NotNull
        private Long emojiId;

        @NotNull
        private BigDecimal multiplier;
    }

}
