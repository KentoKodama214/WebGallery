package com.web.gallary.controller;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ModelAndView;

import com.web.gallary.constant.ApiRoutes;
import com.web.gallary.constant.MessageConst;
import com.web.gallary.controller.request.ErrorRequest;

import lombok.RequiredArgsConstructor;

/**
 * システム共通のページを扱うControllerクラス
 * @author	Kento Kodama
 * @version	1.0.0
 * @since	1.0.0
*/
@Controller
@RequiredArgsConstructor
public class BaseController {
	/**
	 * フッターページ
	 * 
	 * @return	String	"footer"
	 */
	@GetMapping(ApiRoutes.FOOTER)
	public String footer() {
		return "footer";
	}
	
	/**
	 * デフォルトのエラーページ
	 * 
	 * @return	String	"error"
	 */
	@GetMapping(ApiRoutes.ERROR)
	public String error() {
		return "error";
	}
	
	/**
	 * 独自のエラーページ
	 * 
	 * @param	errorRequest	{@link ErrorRequest}
	 * @return	ModelAndView	エラーページ。Modelとして以下を返す<p>
	 * 							goBackPageUrl:	遷移先URL<p>
	 * 							errorMessage:	メッセージ<p>
	 * 							errorCode:		エラーコード<p>
	 * 							httpStatus:		HTTPステータス
	 */
	@GetMapping(ApiRoutes.ERROR_PAGE)
	public ModelAndView error_page(@ModelAttribute ErrorRequest errorRequest) {
		ModelAndView mv = new ModelAndView();
		mv.setViewName("error_page");
		mv.addObject("goBackPageUrl", Optional.ofNullable(errorRequest.getGoBackPageUrl()).orElse("/"));
		mv.addObject("errorMessage", Optional.ofNullable(errorRequest.getErrorMessage()).orElse(MessageConst.ERR_EXCEPTION));
		mv.addObject("errorCode", errorRequest.getErrorCode());
		mv.addObject("httpStatus", errorRequest.getHttpStatus());
		
		return mv;
	}
}