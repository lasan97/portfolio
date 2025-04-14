export interface CreditTransaction {
  id: number;
  userId: number;
  amount: number;
  type: 'CHARGE' | 'USE'; // 충전 또는 사용
  description: string;
  createdAt: string;
}
