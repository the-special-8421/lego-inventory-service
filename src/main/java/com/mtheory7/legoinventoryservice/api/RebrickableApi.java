package com.mtheory7.legoinventoryservice.api;

import com.mtheory7.legoinventoryservice.entities.RebrickableResultDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "rebrickableService", url = "https://rebrickable.com", path = "/api/v3/lego")
public interface RebrickableApi {
    @RequestMapping(method = RequestMethod.GET, value = "/sets/")
    String searchSets(@RequestParam("key") String key, @RequestParam("search") String search);
    @RequestMapping(method = RequestMethod.GET, value = "/sets/{set_num}/")
    RebrickableResultDTO findSetBySetNumber(@RequestParam("key") String key, @PathVariable("set_num") String set_num);
}
