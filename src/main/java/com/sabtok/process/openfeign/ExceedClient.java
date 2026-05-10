package com.sabtok.process.openfeign;

import com.sabtok.process.dto.UserStoryRecord;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Set;

@FeignClient(name = "sab-exceed-service", url = "${sab.exceed.service.url}")
public interface ExceedClient {

    @GetMapping("/user/list/ALL")
    Set<UserStoryRecord> getAllUserStories();
}
