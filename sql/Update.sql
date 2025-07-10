/*
 * v0.3.0 table update sql
 * - 변경 사유: 주관식 문제를 예상 답변 길이에 맞춰 답변하기 어려우며, 예상 답변 길이를 제한하는 것이 적절하지 않다고 판단하여 problems 테이블의 expected_answer_length 컬럼을 삭제
 */
ALTER TABLE problems DROP COLUMN expected_answer_length;

/*
 * v0.2.1 table update sql
 * - 변경 사유: 채점 기준별 평가 점수의 평균값을 저장하기 위해 submissions 테이블의 score 컬럼 타입을 int에서 double로 변경
 */

ALTER TABLE submissions MODIFY COLUMN score DOUBLE NULL;

/*
 * v0.4.0 performance optimization - Database Indexing
 * - 변경 사유: 문제 목록 조회 API 성능 최적화를 위한 인덱스 추가
 * - 목표: 15초 응답 시간을 100-300ms로 단축
 */

-- ===== SUBMISSIONS 테이블 인덱스 =====

-- 1. 사용자별 정답 조회 최적화 (가장 중요한 인덱스)
-- 쿼리: SELECT DISTINCT s.problem_id FROM submissions s WHERE s.user_id = ? AND s.is_correct = 1
CREATE INDEX idx_submissions_user_correct_problem ON submissions(user_id, is_correct, problem_id);

-- 2. 문제별 사용자 제출 조회 최적화
-- 쿼리: SELECT ... FROM submissions WHERE problem_id = ? AND user_id = ? AND is_correct = ?
CREATE INDEX idx_submissions_problem_user_correct ON submissions(problem_id, user_id, is_correct);

-- 3. 사용자별 제출 이력 조회 최적화 (시간순 정렬)
-- 쿼리: SELECT ... FROM submissions WHERE user_id = ? ORDER BY submitted_at DESC
CREATE INDEX idx_submissions_user_submitted_at ON submissions(user_id, submitted_at DESC);

-- 4. 정답 여부별 통계 조회 최적화
-- 쿼리: SELECT ... FROM submissions WHERE is_correct = ? ORDER BY submitted_at
CREATE INDEX idx_submissions_correct_submitted_at ON submissions(is_correct, submitted_at DESC);

-- 5. 문제별 제출 통계 조회 최적화
-- 쿼리: SELECT ... FROM submissions WHERE problem_id = ? ORDER BY submitted_at
CREATE INDEX idx_submissions_problem_submitted_at ON submissions(problem_id, submitted_at DESC);

-- ===== PROBLEMS 테이블 인덱스 =====

-- 6. 복합 필터링 조회 최적화 (category + difficulty + type)
-- 쿼리: SELECT ... FROM problems WHERE category = ? AND difficulty = ? AND type = ?
CREATE INDEX idx_problems_category_difficulty_type ON problems(category, difficulty, type);

-- 7. 상태별 solved_count 정렬 최적화 (기본 정렬)
-- 쿼리: SELECT ... FROM problems WHERE status = 'ACTIVE' ORDER BY solved_count DESC
CREATE INDEX idx_problems_status_solved_count ON problems(status, solved_count DESC);

-- 8. 카테고리별 최신순 정렬 최적화
-- 쿼리: SELECT ... FROM problems WHERE category = ? ORDER BY created_at DESC
CREATE INDEX idx_problems_category_created_at ON problems(category, created_at DESC);

-- 9. 난이도별 최신순 정렬 최적화
-- 쿼리: SELECT ... FROM problems WHERE difficulty = ? ORDER BY created_at DESC
CREATE INDEX idx_problems_difficulty_created_at ON problems(difficulty, created_at DESC);

-- 10. 타입별 최신순 정렬 최적화
-- 쿼리: SELECT ... FROM problems WHERE type = ? ORDER BY created_at DESC
CREATE INDEX idx_problems_type_created_at ON problems(type, created_at DESC);

-- 11. solved_count 정렬 전용 인덱스 (MOST_SOLVED, LEAST_SOLVED)
-- 쿼리: SELECT ... FROM problems ORDER BY solved_count DESC
CREATE INDEX idx_problems_solved_count_desc ON problems(solved_count DESC);

-- 12. created_at 정렬 전용 인덱스 (LATEST, OLDEST)
-- 쿼리: SELECT ... FROM problems ORDER BY created_at DESC
CREATE INDEX idx_problems_created_at_desc ON problems(created_at DESC);

-- ===== 인덱스 통계 업데이트 =====
-- 새로 생성된 인덱스의 통계 정보를 업데이트하여 옵티마이저가 올바른 실행 계획을 세우도록 함
ANALYZE TABLE submissions;
ANALYZE TABLE problems;
