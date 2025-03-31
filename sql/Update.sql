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
