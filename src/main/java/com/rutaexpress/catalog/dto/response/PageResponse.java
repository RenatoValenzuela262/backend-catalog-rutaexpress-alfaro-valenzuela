package com.rutaexpress.catalog.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
@Schema(description = "Envoltorio de respuesta paginada")
public class PageResponse<T> {

    @Schema(description = "Elementos de la página actual")
    private List<T> content;

    @Schema(description = "Número de página actual (base 0)", example = "0")
    private int page;

    @Schema(description = "Tamaño de página", example = "20")
    private int size;

    @Schema(description = "Total de elementos", example = "42")
    private long totalElements;

    @Schema(description = "Total de páginas", example = "3")
    private int totalPages;

    @Schema(description = "Es la primera página", example = "true")
    private boolean first;

    @Schema(description = "Es la última página", example = "false")
    private boolean last;

    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}