/**
 * 외부 링크 인터페이스
 */
export interface ExternalLink {
  name: string;
  url: string;
  logoUrl?: string;
}

/**
 * 자기소개 데이터 전송 객체
 */
export interface IntroductionDto {
  id: number;
  title: string;
  content: string;
  externalLinks: ExternalLink[];
  updatedAt: string;
}

/**
 * 자기소개 업데이트 요청 DTO
 */
export interface IntroductionUpdateRequest {
  title: string;
  content: string;
  externalLinks: ExternalLink[];
}

/**
 * 자기소개 생성 요청 DTO
 */
export interface IntroductionCreateRequest {
  title: string;
  content: string;
  externalLinks: ExternalLink[];
}
