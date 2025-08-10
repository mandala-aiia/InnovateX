package com.alec.InnovateX.tv;


import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/vod")
public class VodController {

    private final VodService vodService;

    public VodController(VodService vodService) {
        this.vodService = vodService;
    }

    @GetMapping("/front")
    public VodResponse vod() {
        return vodService.vod();
    }

    @GetMapping("/video/list")
    public VodResponse vodVideoList(
            @RequestParam("type_id") Integer type_id,
            @RequestParam("page") Integer page,
            @RequestParam(value = "vod_id",required = false) Integer vod_id
    ) {
        return vodService.vodVideoList(type_id, page,vod_id);
    }
}