/**
 * 파일 업로드 응답 인터페이스
 */
export interface FileUploadResponse {
  fileName: string;
  fileUrl: string;
  contentType: string;
  size: number;
}
