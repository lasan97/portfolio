import { ProductCategory } from './constants';

// UI 모델 - 폼 데이터 및 UI 컴포넌트용 모델
export interface ProductDisplayProps {
  id: number;
  name: string;
  price: number;
  originalPrice: number;
  thumbnailImageUrl?: string;
  category: ProductCategory;
  inStock: boolean;
}
