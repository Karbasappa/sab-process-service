package com.sabtok.process.dto.response;

import com.sabtok.process.dto.CombinedResponse;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
public class SearchResponseRecord {
    private String responseTime;
    private String threadName;
    private Set<String> searchItems;
    private CombinedResponse combinedResponse;
}
