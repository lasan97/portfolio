/**
 * 공통 유틸리티 함수 모음
 */

/**
 * 날짜를 포맷팅하는 함수
 * @param date - 포맷팅할 날짜
 * @param format - 출력 형식 (기본값: 'YYYY-MM-DD')
 * @returns 포맷팅된 날짜 문자열
 */
export function formatDate(date: Date, format = 'YYYY-MM-DD'): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  
  let result = format;
  result = result.replace('YYYY', year.toString());
  result = result.replace('MM', month);
  result = result.replace('DD', day);
  
  return result;
}

/**
 * 객체의 빈 값(null, undefined, '')을 필터링하는 함수
 * @param obj - 필터링할 객체
 * @returns 빈 값이 제거된 새 객체
 */
export function removeEmptyValues<T extends Record<string, unknown>>(obj: T): Partial<T> {
  return Object.fromEntries(
    Object.entries(obj).filter(([, value]) => {
      return value !== null && value !== undefined && value !== '';
    })
  ) as Partial<T>;
}

/**
 * 문자열이 비어있는지 확인하는 함수
 * @param value - 확인할 문자열
 * @returns 비어있으면 true, 아니면 false
 */
export function isEmpty(value: string | null | undefined): boolean {
  return value === null || value === undefined || value.trim() === '';
}

/**
 * 숫자에 천 단위 구분자를 추가하는 함수
 * @param value - 포맷팅할 숫자
 * @returns 천 단위 구분자가 추가된 문자열
 */
export function formatNumber(value: number): string {
  return value.toLocaleString('ko-KR');
}
