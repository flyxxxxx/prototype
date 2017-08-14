package org.prototype.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
class ConcurrentController {

	/**
	 * 给前端提供的并发
	 * @return
	 */
	@RequestMapping("/prototype/fork")
	public String fork(){
		return null;
	}

}
