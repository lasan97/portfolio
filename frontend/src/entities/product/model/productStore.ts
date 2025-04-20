import { defineStore } from 'pinia';
import { Product, ProductCategory } from './types';
import { productApi } from '@shared/api';

export const useProductStore = defineStore('product', {
  state: () => ({
    products: [] as Product[],
    isLoading: false,
    error: null as string | null,
  }),
  getters: {
    getProducts: (state) => state.products,
    getProductById: (state) => (id: number) => 
      state.products.find(product => product.id === id),
    getProductsByCategory: (state) => (category: string) => 
      state.products.filter(product => product.category === category),
    getInStockProducts: (state) => 
      state.products.filter(product => product.inStock),
  },
  actions: {
    // 실제 API 호출로 상품 목록 가져오기
    async fetchProducts() {
      this.isLoading = true;
      this.error = null;
      
      try {
        const productsData = await productApi.getProducts();
        
        // API 응답을 애플리케이션 모델에 맞게 변환
        this.products = productsData.map(item => ({
          id: item.id,
          name: item.name,
          originalPrice: item.originalPrice,
          price: item.price,
          description: '', // 리스트에서는 설명이 제공되지 않으므로 빈 문자열로 설정
          imageUrl: item.thumbnailImageUrl, // 백엔드의 필드명을 프론트엔드 모델에 맞게 매핑
          category: item.category,
          inStock: item.status !== 'SOLD_OUT', // 품절 상태가 아니면 재고 있음
        }));
      } catch (err: any) {
        this.error = err.message || '상품을 불러오는 중 오류가 발생했습니다.';
        console.error('Error fetching products:', err);
      } finally {
        this.isLoading = false;
      }
    },
    
    // 단일 상품 상세 정보 가져오기
    async fetchProductById(id: number) {
      // 이미 상품 목록에 있는 경우 API 호출 스킵
      const existingProduct = this.getProductById(id);
      if (existingProduct && existingProduct.description) {
        return existingProduct;
      }
      
      this.isLoading = true;
      this.error = null;
      
      try {
        const productData = await productApi.getProduct(id);
        
        // 상품 상세 정보를 애플리케이션 모델에 맞게 변환
        const product: Product = {
          id: productData.id,
          name: productData.name,
          originalPrice: productData.originalPrice,
          price: productData.price,
          description: productData.description,
          imageUrl: productData.thumbnailImageUrl, // 백엔드의 필드명을 프론트엔드 모델에 맞게 매핑
          category: productData.category,
          inStock: productData.stock > 0 && productData.status !== 'SOLD_OUT',
        };
        
        // 상품 목록에 이미 존재하는 상품인지 확인하고 업데이트 또는 추가
        const index = this.products.findIndex(p => p.id === id);
        if (index !== -1) {
          this.products[index] = product;
        } else {
          this.products.push(product);
        }
        
        return product;
      } catch (err: any) {
        this.error = err.message || '상품 정보를 불러오는 중 오류가 발생했습니다.';
        console.error('Error fetching product details:', err);
        throw err;
      } finally {
        this.isLoading = false;
      }
    }
  }
});
