package site.haruhana.www.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import site.haruhana.www.feign.dto.gemini.GeminiRequest;
import site.haruhana.www.feign.dto.gemini.GeminiResponse;

/**
 * Google Gemini AI API와 통신하기 위한 Feign 클라이언트 인터페이스
 *
 * @see <a href="https://ai.google.dev/gemini-api/docs?hl=ko#rest">Gemini API 공식 문서</a>
 */
@FeignClient(name = "gemini", url = "https://generativelanguage.googleapis.com/v1beta")
public interface GeminiFeignClient {

    /**
     * Gemini AI 모델을 사용하여 콘텐츠를 생성하는 메서드
     * <p>
     * 구조화된 JSON 형식의 응답을 받을 수 있도록 설정할 수 있다.
     *
     * @param apiKey  Gemini 서비스 인증을 위한 API 키
     * @param request 프롬프트와 설정을 포함한 콘텐츠 생성 요청 객체
     * @return 생성된 콘텐츠와 관련 메타데이터를 포함한 응답 객체
     * @see <a href="https://ai.google.dev/gemini-api/docs/structured-output?hl=ko&lang=rest">Gemini API로 구조화된 출력 생성</a>
     */
    @PostMapping("/models/gemini-2.0-flash:generateContent")
    GeminiResponse generateContent(
            @RequestParam("key") String apiKey,
            @RequestBody GeminiRequest request
    );
}
