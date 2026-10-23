package com.mobipic.io.dto;

import java.util.List;

/**
 * Root of {@code project.json}. The first layer is the base image.
 */
public record ProjectDto(int formatVersion, String application, double width, double height,
                         List<LayerDto> layers) {
}
