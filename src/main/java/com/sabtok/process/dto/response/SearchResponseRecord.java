package com.sabtok.process.dto.response;

import com.sabtok.process.dto.CombinedResponse;
import com.sabtok.process.dto.SearchItem;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
public class SearchResponseRecord {
    private String responseTime;
    private String threadName;
    private Set<SearchItem> searchItems;
    private Set<String> threads;
}
