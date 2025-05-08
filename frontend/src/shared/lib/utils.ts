/**
 * 공통 유틸리티 함수 모음
 */

/**
 * 날짜 문자열 또는 Date 객체를 포맷팅하는 함수
 * @param dateInput - 포맷팅할 날짜 (문자열 또는 Date 객체)
 * @param options - Intl.DateTimeFormat 옵션 또는 간단한 형식 문자열
 * @returns 포맷팅된 날짜 문자열
 */
export function formatDate(
  dateInput: string | Date | undefined | null,
  options: Intl.DateTimeFormatOptions | string = 'YYYY-MM-DD'
): string {
  if (!dateInput) return '';

  // 문자열을 Date 객체로 변환
  const date = typeof dateInput === 'string' ? new Date(dateInput) : dateInput;

  // 유효하지 않은 날짜인 경우
  if (isNaN(date.getTime())) return '';

  // 옵션이 문자열인 경우 (간단한 형식 지정)
  if (typeof options === 'string') {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');

    let result = options;
    result = result.replace('YYYY', year.toString());
    result = result.replace('MM', month);
    result = result.replace('DD', day);
    result = result.replace('HH', hours);
    result = result.replace('mm', minutes);
    result = result.replace('ss', seconds);

    return result;
  }

  // Intl.DateTimeFormat 사용 (더 복잡한 형식 지정)
  return new Intl.DateTimeFormat('ko-KR', options).format(date);
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
  return new Intl.NumberFormat('ko-KR').format(value);
}

/**
 * 가격을 통화 형식으로 포맷팅하는 함수
 * @param price - 포맷팅할 가격 (숫자 또는 객체)
 * @param options - Intl.NumberFormat 옵션 (기본값: 한국 원화, 소수점 없음)
 * @returns 포맷팅된 가격 문자열
 */
export function formatPrice(
  price: number | { amount: number } | any,
  options: Intl.NumberFormatOptions = {
    style: 'currency',
    currency: 'KRW',
    maximumFractionDigits: 0
  }
): string {
  // price가 숫자인 경우
  if (typeof price === 'number') {
    if (isNaN(price)) {
      console.error('가격 데이터 오류: NaN 값이 감지되었습니다', price);
      return '₩0';
    }
    return new Intl.NumberFormat('ko-KR', options).format(price);
  }

  // price가 객체이고 amount 속성이 있는 경우
  if (price && typeof price === 'object' && 'amount' in price) {
    const amount = price.amount;
    if (typeof amount === 'number' && !isNaN(amount)) {
      return new Intl.NumberFormat('ko-KR', options).format(amount);
    }
  }

  // NaN 값인 경우
  if (price === 'NaN' || price === 'WNaN' ||
      (typeof price === 'number' && isNaN(price))) {
    console.error('가격 데이터 오류: NaN 값이 감지되었습니다', price);
    return '₩0';
  }

  // 오류 방지를 위한 기본값
  return '₩0';
}
