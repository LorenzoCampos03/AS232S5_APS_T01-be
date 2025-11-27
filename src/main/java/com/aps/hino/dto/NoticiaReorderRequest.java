package com.aps.hino.dto;

import lombok.Data;

import java.util.List;

@Data
public class NoticiaReorderRequest {
    private List<Long> imageIds;
}
