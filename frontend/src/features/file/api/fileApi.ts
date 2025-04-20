import { apiInstance } from '@shared/api';
import type { AxiosResponse } from 'axios';
import type { FileUploadResponse } from '../model/types';

/**
 * 파일 업로드 API
 */
export const uploadFile = async (file: File): Promise<FileUploadResponse> => {
  const formData = new FormData();
  formData.append('file', file);

  // FormData를 전송할 때 Content-Type을 자동으로 설정하도록 합니다
  const response: AxiosResponse<FileUploadResponse> = await apiInstance.post('api/files/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });

  return response.data;
};

/**
 * 다중 파일 업로드 API
 */
export const uploadMultipleFiles = async (files: File[]): Promise<FileUploadResponse[]> => {
  const formData = new FormData();
  files.forEach(file => {
    formData.append('files', file);
  });

  const response: AxiosResponse<FileUploadResponse[]> = await apiInstance.post('api/files/upload-multiple', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });

  return response.data;
};

/**
 * 파일 삭제 API
 */
export const deleteFile = async (fileUrl: string): Promise<boolean> => {
  await apiInstance.delete('api/files', {
    params: { fileUrl }
  });
  
  return true;
};
