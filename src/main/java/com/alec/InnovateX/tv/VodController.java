package com.alec.InnovateX.tv;


import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vod")
public class VodController {

    private final VodService vodService;

    public VodController(VodService vodService) {
        this.vodService = vodService;
    }

    @GetMapping("/search")
    public VodResponse searchVod(
            @RequestParam(value = "wd", required = false) String wd,
            @RequestParam(value = "page") Integer page,
            @RequestParam(value = "category",required = false) String category) {
        return vodService.searchVod(wd, page);
    }

    @GetMapping("/detail")
    public VodDetailResponse detailVod(@RequestParam(value = "ids") Integer ids) {
        return vodService.detailVod(ids);
    }

    @GetMapping("/play")
    public String play(
            @RequestParam("ids") Integer ids
    ) {
        VodDetailResponse vodDetailResponse = vodService.detailVod(ids);
        VodInfo vodInfo = vodDetailResponse.getList().get(0);
        String rawPlay = vodInfo.getVod_play_url();
        return rawPlay.split("\\$")[1];
    }
}