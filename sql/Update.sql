/*
 * v0.2.1 table update sql
 * - 변경 사유: 채점 기준별 평가 점수의 평균값을 저장하기 위해 submissions 테이블의 score 컬럼 타입을 int에서 double로 변경
 */

ALTER TABLE submissions MODIFY COLUMN score DOUBLE NULL;
