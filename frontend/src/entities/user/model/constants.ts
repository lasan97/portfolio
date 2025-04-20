// 사용자 관련 상수

// 사용자 역할 Enum (기존에 @shared/config에서 가져오던 것을 여기로 이동)
export enum UserRole {
  ADMIN = 'ADMIN',
  USER = 'USER'
}

// 사용자 역할에 대한 설명을 제공하는 네임스페이스
export namespace UserRole {
  export const descriptions: Record<UserRole, string> = {
    [UserRole.ADMIN]: '관리자',
    [UserRole.USER]: '일반 사용자'
  };

  export function getDescription(role: UserRole): string {
    return descriptions[role] || role;
  }
}