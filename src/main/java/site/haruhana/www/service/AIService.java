package site.haruhana.www.service;

import site.haruhana.www.entity.problem.Category;
import site.haruhana.www.entity.problem.Difficulty;
import site.haruhana.www.entity.problem.Problem;

public interface AIService {

    String MULTIPLE_CHOICE_PROMPT = """
            당신은 프로그래밍 문제 출제 전문가입니다. %s 분야의 %s 난이도 객관식 문제를 출제해주세요.
            
            난이도별 출제 기준:
            1. EASY 난이도
               - 해당 분야의 기본 개념과 원리를 확인하는 문제
               - 실무자라면 반드시 알아야 하는 필수 지식
               - 복잡하지 않은 명확한 선지
            
            2. MEDIUM 난이도
               - 실무에서 자주 마주치는 상황 기반 문제
               - 여러 개념을 함께 이해해야 하는 문제
               - 실제 적용 사례와 관련된 선지
            
            3. HARD 난이도
               - 심화 개념과 응용 지식을 확인하는 문제
               - 다양한 상황과 제약사항을 고려해야 하는 문제
               - 세부적인 최적화나 성능 관련 선지
            
            문제 작성 요구사항:
            1. 문제는 명확하고 간단하게 작성 (100자 이내)
            2. 각 보기는 서로 명확히 구분되어야 함
            3. 보기는 정확히 5개로 작성
            4. 정답이 될 수 있는 보기는 1개 이상
            5. 모호한 표현이나 애매한 선지 사용 금지
            
            보기 작성 요구사항:
            1. 각 보기는 최소 10자 이상, 최대 100자 이내로 작성
            2. 각 보기는 다른 보기와 명확히 구분되는 내용으로 작성
            3. 난이도에 따른 정답 개수
               - EASY: 1개의 명확한 정답
               - MEDIUM: 2-3개의 연관된 정답
               - HARD: 2개 이상의 복합적인 정답
            4. 오답은 확실히 틀린 내용이되, 그럴듯해 보여야 함
            5. "모두 정답이다" 또는 "정답 없음" 같은 보기 사용 금지
            
            다음 JSON 형식으로 작성해주세요:
            {
                "title": "간단한 문제 제목",
                "question": "명확한 질문 내용",
                "options": [
                    {"content": "보기 내용", "isCorrect": true},
                    {"content": "보기 내용", "isCorrect": false},
                    {"content": "보기 내용", "isCorrect": false},
                    {"content": "보기 내용", "isCorrect": false},
                    {"content": "보기 내용", "isCorrect": false}
                ]
            }
            """;

    String SUBJECTIVE_PROMPT = """
            당신은 프로그래밍 문제 출제 전문가입니다. %s 분야의 %s 난이도 주관식 문제를 다음 기준에 맞게 출제해주세요.
            
            난이도별 출제 기준:
            1. EASY 난이도 (기본 개념 이해도 평가)
               - 주요 개념의 정의와 특징을 명확하게 설명하도록 유도
               - 다른 유사 개념과의 차이점을 구체적으로 비교하도록 요구
               - 실제 사용되는 간단한 예시를 들어 설명하도록 요구
               - 해당 개념의 장단점을 분석하도록 요구
               예상 답안 길이: 200-300자
            
            2. MEDIUM 난이도 (실무 적용력 평가)
               - 구체적인 실무 상황에서의 활용 방법 설명
               - 성능/확장성 측면에서의 최적화 방안 제시
               - 발생할 수 있는 문제점과 해결 방안 설명
               - 대안 기술과의 비교 분석 및 선택 기준 제시
               - 실제 구현 시 고려해야 할 제약사항 설명
               예상 답안 길이: 400-500자
            
            3. HARD 난이도 (문제 해결력 평가)
               - 복잡한 시스템 아키텍처 상황 제시
               - 구체적인 성능/장애 이슈 상황 설명
               - 시스템 메트릭/로그 데이터 상세 제공
                 (예: 응답시간, CPU 사용률, 메모리 사용량, 에러 로그 등)
               - 비즈니스 영향도 명시
                 (예: 사용자 영향, 매출 영향, SLA 위반 여부 등)
               - 시간/리소스 제약사항 제시
                 (예: 즉시 조치 필요, 계획된 유지보수 시간 내 해결 등)
               - 보안/규정 준수 요구사항 포함
               예상 답안 길이: 600-800자
            
            평가 요소 선정 기준:
            1. 지식 평가 요소
               - 관련 개념의 정확한 이해도
               - 기술적 원리의 파악 정도
               - 실무 적용 가능성
            
            2. 분석력 평가 요소
               - 문제 상황 분석의 정확성
               - 원인 파악의 논리성
               - 제약사항 고려 정도
            
            3. 해결력 평가 요소
               - 해결 방안의 실현 가능성
               - 제시된 방안의 효율성
               - 리스크 대응 방안
            
            4. 확장성 평가 요소
               - 장기적 관점의 개선 방안
               - 유사 문제 방지 대책
               - 확장 가능한 설계 제시
            
            답안 평가 기준 요구사항:
            1. 각 평가 항목은 구체적이고 측정 가능해야 함
            2. 난이도별 평가 항목 구성
               - EASY: 개념 이해도, 설명 명확성, 예시 적절성
               - MEDIUM: 실무 적용성, 구현 방법, 성능 고려사항
               - HARD: 문제 분석력, 해결 방안, 아키텍처 설계, 보안/확장성
            3. 각 평가 항목은 독립적이고 중복되지 않아야 함
            4. 채점 기준이 객관적이고 명확해야 함
            
            다음 JSON 형식으로 작성해주세요:
            {
                "title": "명확하고 간단한 문제 제목",
                "question": "난이도에 맞는 상세한 질문 내용 (HARD 난이도의 경우 모든 상황 정보 포함)",
                "evaluationPoints": [
                    "핵심 평가 요소 1 (가장 중요한 평가 기준)",
                    "핵심 평가 요소 2 (다음 중요 평가 기준)",
                    "핵심 평가 요소 3 (그 다음 중요 평가 기준)",
                    "핵심 평가 요소 4 (추가 평가 기준)"
                ],
                "expectedLength": "난이도별 예상 답안 길이",
                "sampleAnswer": "모범 답안 (평가 요소를 모두 충족하는 구체적인 예시 답변)"
            }
            
            주의사항:
            1. 문제는 실무에서 실제 발생할 수 있는 상황을 기반으로 출제
            2. 평가 요소는 답안 채점 시 명확히 구분될 수 있도록 작성
            3. 모범 답안은 모든 평가 요소를 포함하여 구체적으로 작성
            4. 난이도에 맞는 적절한 복잡도 유지
            """;

    /**
     * AI를 사용하여 객관식 문제를 생성하는 메소드
     *
     * @param category   생성할 문제의 카테고리
     * @param difficulty 생성할 문제의 난이도
     * @return 카테고리와 난이도에 맞게 AI가 생성한 객관식 문제
     */
    Problem generateMultipleChoiceQuestion(Category category, Difficulty difficulty);

    /**
     * AI를 사용하여 주관식 문제를 생성하는 메소드
     *
     * @param category   생성할 문제의 카테고리
     * @param difficulty 생성할 문제의 난이도
     * @return 카테고리와 난이도에 맞게 AI가 생성한 주관식 문제
     */
    Problem generateSubjectiveQuestion(Category category, Difficulty difficulty);

}