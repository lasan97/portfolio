// 도메인 모델
export type { User, UserState } from './model/types';
export { UserRole } from './model/constants';

// UI 모델
export type { UserCardProps, UserDisplayProps } from './model/ui-types';

// 스토어
export { useUserStore } from './model/userStore';

// UI 컴포넌트
export { default as UserCard } from './ui/UserCard.vue';
