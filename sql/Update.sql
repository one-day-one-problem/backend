/*
 * v0.2.1 table update sql
 * - 변경 사유: 채점 기준별 평가 점수의 평균값을 저장하기 위해 submissions 테이블의 score 컬럼 타입을 int에서 double로 변경
 */

ALTER TABLE submissions MODIFY COLUMN score DOUBLE NULL;

/*
 * v0.3.0 table update sql
 * - 변경 사유: Submission 테이블에 상속 구조 적용을 위한 discriminator column 추가
 */

-- submission_type 컬럼이 존재하지 않는 경우에만 추가
ALTER TABLE submissions ADD COLUMN IF NOT EXISTS submission_type VARCHAR(50);

-- 기존 데이터에 대해 문제 유형에 따라 submission_type 컬럼 값을 업데이트
UPDATE submissions s
JOIN problems p ON s.problem_id = p.id
SET s.submission_type = CASE
    WHEN p.problem_type = 'MULTIPLE_CHOICE' THEN 'MULTIPLE_CHOICE'
    WHEN p.problem_type = 'SUBJECTIVE' THEN 'SUBJECTIVE'
    ELSE 'MULTIPLE_CHOICE' -- 기본값 설정
END;

-- NOT NULL 제약 조건 추가
ALTER TABLE submissions MODIFY COLUMN submission_type VARCHAR(50) NOT NULL;
