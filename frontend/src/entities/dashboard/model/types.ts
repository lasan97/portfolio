/**
 * 대시보드 데이터 전송 객체
 */
export interface DashboardDto {
  id: number;
  title: string;
  content: string;
  githubUrl?: string;
  velogUrl?: string;
  linkedinUrl?: string;
  otherUrl?: string;
  createdAt: string;
  updatedAt: string;
  createdBy?: string;
  updatedBy?: string;
}

/**
 * 대시보드 업데이트 요청 DTO
 */
export interface DashboardUpdateRequest {
  title: string;
  content: string;
  githubUrl?: string;
  velogUrl?: string;
  linkedinUrl?: string;
  otherUrl?: string;
}
