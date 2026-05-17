package com.sabtok.process.dto;

import java.util.Set;

public record SearchItem(String component,
                         Set<String> searchData) {
}
