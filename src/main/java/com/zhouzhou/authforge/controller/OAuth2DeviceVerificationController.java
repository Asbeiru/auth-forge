package com.zhouzhou.authforge.controller;

import com.zhouzhou.authforge.exception.OAuth2DeviceAuthorizationException;
import com.zhouzhou.authforge.model.DeviceAuthorizationStatus;
import com.zhouzhou.authforge.service.OAuth2DeviceAuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * OAuth 2.0 设备验证控制器。
 * 处理设备授权流程中的用户验证页面和授权确认。
 * 遵循 RFC 8628 规范实现设备授权流程。
 * 
 * @author zhouzhou
 * @since 1.0.0
 */
@Controller
@RequestMapping("/oauth2/device")
@RequiredArgsConstructor
@Slf4j
public class OAuth2DeviceVerificationController {

    private final OAuth2DeviceAuthorizationService deviceAuthorizationService;

    @Value("${auth.device.interval:5}")
    private Integer interval;

    @Value("${auth.device.auto-submit-delay:2000}")
    private Integer autoSubmitDelay;

    /**
     * 显示设备验证页面。
     * 如果提供了 user_code，则自动填充并准备自动提交。
     * 实现 RFC 8628 中的 verification_uri_complete 功能。
     *
     * @param userCode 用户验证码（可选）
     * @param model Spring MVC Model
     * @return 设备验证页面
     */
    @GetMapping("/verify")
    public String showVerificationPage(@RequestParam(required = false) String user_code, Model model) {
        if (user_code != null) {
            try {
                // 检查验证码状态
                DeviceAuthorizationStatus status = deviceAuthorizationService.checkUserCodeStatus(user_code, interval);
                if (status != DeviceAuthorizationStatus.PENDING) {
                    model.addAttribute("error", "该验证码已被处理或已过期");
                    return "device_verification";
                }
                // 设置自动提交标志和延迟时间
                model.addAttribute("autoSubmit", true);
                model.addAttribute("autoSubmitDelay", autoSubmitDelay);
                log.debug("Preparing auto-submit for user_code: {}", user_code);
            } catch (OAuth2DeviceAuthorizationException e) {
                log.warn("Failed to check user code status: {}, error: {}", user_code, e.getError());
                model.addAttribute("error", e.getErrorDescription());
                return "device_verification";
            }
        }
        model.addAttribute("userCode", user_code);
        return "device_verification";
    }

    /**
     * 处理设备验证请求。
     * 验证用户提供的验证码，并更新设备授权状态。
     * 实现 RFC 8628 中的用户授权确认流程。
     *
     * @param userCode 用户验证码
     * @param action 用户操作（approve/deny）
     * @param redirectAttributes 重定向属性
     * @return 重定向到结果页面
     */
    @PostMapping("/verify")
    public String handleVerification(
            @RequestParam("user_code") String userCode,
            @RequestParam("action") String action,
            RedirectAttributes redirectAttributes) {
        
        try {
            // 处理验证请求，包含轮询限制检查
            DeviceAuthorizationStatus status = deviceAuthorizationService.verifyUserCode(
                userCode, 
                "approve".equals(action),
                interval
            );

            // 添加结果信息
            redirectAttributes.addFlashAttribute("status", status);
            redirectAttributes.addFlashAttribute("userCode", userCode);
            log.info("Device verification completed: userCode={}, status={}", userCode, status);

            return "redirect:/oauth2/device/result";
        } catch (OAuth2DeviceAuthorizationException e) {
            log.warn("Device verification failed: userCode={}, error={}", userCode, e.getError());
            redirectAttributes.addFlashAttribute("error", e.getErrorDescription());
            return "redirect:/oauth2/device/verify";
        }
    }

    /**
     * 显示设备验证结果页面。
     * 展示授权结果并指导用户返回设备。
     *
     * @param model Spring MVC Model
     * @return 结果页面
     */
    @GetMapping("/result")
    public String showResultPage(Model model) {
        if (!model.containsAttribute("status")) {
            return "redirect:/oauth2/device/verify";
        }
        return "device_verification_result";
    }
} 