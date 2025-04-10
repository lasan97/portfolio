/**
 * 외부 링크 인터페이스
 */
export interface ExternalLink {
  name: string;
  url: string;
  logoUrl?: string;
}

/**
 * 대시보드 데이터 전송 객체
 */
export interface DashboardDto {
  id: number;
  title: string;
  content: string;
  externalLinks: ExternalLink[];
  updatedAt: string;
}

/**
 * 대시보드 업데이트 요청 DTO
 */
export interface DashboardUpdateRequest {
  title: string;
  content: string;
  externalLinks: ExternalLink[];
}

/**
 * 대시보드 생성 요청 DTO
 */
export interface DashboardCreateRequest {
  title: string;
  content: string;
  externalLinks: ExternalLink[];
}
