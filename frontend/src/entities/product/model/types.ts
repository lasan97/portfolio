export interface Product {
  id: number;
  name: string;
  originalPrice: number; // 원가
  price: number; // 판매가
  description: string;
  imageUrl: string;
  category: string;
  inStock: boolean;
}
